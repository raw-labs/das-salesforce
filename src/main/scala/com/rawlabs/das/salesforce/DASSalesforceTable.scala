/*
 * Copyright 2024 RAW Labs S.A.
 *
 * Use of this software is governed by the Business Source License
 * included in the file licenses/BSL.txt.
 *
 * As of the Change Date specified in that file, in accordance with
 * the Business Source License, use of this software will be governed
 * by the Apache License, Version 2.0, included in the file
 * licenses/APL.txt.
 */

package com.rawlabs.das.salesforce

import com.rawlabs.das.sdk.{DASExecuteResult, DASTable}
import com.rawlabs.protocol.das.{Qual, Row, SortKey, TableDefinition}
import com.rawlabs.protocol.raw.{
  Value,
  ValueBool,
  ValueDouble,
  ValueInt,
  ValueNull,
  ValueRecord,
  ValueRecordField,
  ValueString
}
import com.typesafe.scalalogging.StrictLogging

import scala.collection.JavaConverters._
import scala.collection.mutable

abstract class DASSalesforceTable(
    connector: DASSalesforceConnector,
    val tableName: String,
    salesforceObjectName: String
) extends DASTable
    with StrictLogging {

  assert(
    salesforceObjectName.capitalize == salesforceObjectName,
    "Salesforce object name must be capitalized as per Salesforce API"
  )

  def tableDefinition: TableDefinition

  protected def fieldsCannotBeUpdated: Seq[String] = Seq(uniqueColumn)

  override def getRelSize(quals: Seq[Qual], columns: Seq[String]): (Int, Int) = (100, 100)

  // We push down all sorts to Salesforce, so they are all supported
  override def canSort(sortKeys: Seq[SortKey]): Seq[SortKey] = sortKeys

  // Query by unique ID returns one row
  override def getPathKeys: Seq[(Seq[String], Int)] = Seq((Seq(uniqueColumn), 1))

  override def explain(
      quals: Seq[Qual],
      columns: Seq[String],
      maybeSortKeys: Option[Seq[SortKey]],
      maybeLimit: Option[Long],
      verbose: Boolean
  ): Seq[String] = Seq.empty

  override def execute(
      quals: Seq[Qual],
      columns: Seq[String],
      maybeSortKeys: Option[Seq[SortKey]],
      maybeLimit: Option[Long]
  ): DASExecuteResult = {
    val salesforceColumns = columns.map(renameToSalesforce)

    var soql = {
      if (salesforceColumns.isEmpty) {
        s"SELECT ${renameToSalesforce(tableDefinition.getColumns(0).getName)} FROM " + salesforceObjectName
      } else {
        "SELECT " + salesforceColumns.mkString(", ") + " FROM " + salesforceObjectName
      }
    }
    val simpleQuals = quals.filter(_.hasSimpleQual) // Only simple quals are supported
    if (simpleQuals.nonEmpty) {
      soql += " WHERE " + quals
        .map { q =>
          assert(q.hasSimpleQual, "Only simple quals are supported")
          val op = q.getSimpleQual.getOperator
          val soqlOp =
            if (op.hasEquals) "="
            else if (op.hasGreaterThan) ">"
            else if (op.hasGreaterThanOrEqual) ">="
            else if (op.hasLessThan) "<"
            else if (op.hasLessThanOrEqual) "<="
            else {
              assert(op.hasNotEquals)
              "<>"
            }
          q.getFieldName + " " + soqlOp + " " + rawValueToSOQLValue(q.getSimpleQual.getValue)
        }
        .mkString(" AND ")
    }
    if (maybeSortKeys.nonEmpty) {
      soql += " ORDER BY " + maybeSortKeys.get
        .map { sk =>
          val order = if (sk.getIsReversed) "DESC" else "ASC"
          val nulls = if (sk.getNullsFirst) "NULLS FIRST" else "NULLS LAST"
          sk.getName + " " + order + " " + nulls
        }
        .mkString(", ")
    }
    if (maybeLimit.nonEmpty) {
      soql += " LIMIT " + maybeLimit.get
    }
    logger.debug(s"Executing SOQL query: $soql")
    var query = connector.forceApi.query(soql)

    new DASExecuteResult {
      private val currentChunk: mutable.Buffer[Row] = mutable.Buffer.empty
      private var currentChunkIndex: Int = 0

      readChunk()

      private def readChunk(): Unit = {
        def convertAnyToValue(any: Any): Value = {
          any match {
            case null => Value.newBuilder().setNull(ValueNull.newBuilder()).build()
            case s: String => Value.newBuilder().setString(ValueString.newBuilder().setV(s).build()).build()
            case i: Int => Value.newBuilder().setInt(ValueInt.newBuilder().setV(i).build()).build()
            case d: Double => Value.newBuilder().setDouble(ValueDouble.newBuilder().setV(d).build()).build()
            case b: Boolean => Value.newBuilder().setBool(ValueBool.newBuilder().setV(b).build()).build()
            case m: Map[_, _] =>
              val record = ValueRecord.newBuilder()
              m.foreach {
                case (k: String, v) =>
                  record.addFields(ValueRecordField.newBuilder().setName(k).setValue(convertAnyToValue(v)).build())
              }
              Value.newBuilder().setRecord(record.build()).build()
            case t =>
              logger.error(s"Unsupported type: ${t.getClass} (type = ${t.getClass})")
              throw new IllegalArgumentException(s"Unsupported type: ${t.getClass}")
          }
        }

        currentChunk.clear()
        currentChunkIndex = 0
        query.getRecords.asScala.foreach { record =>
          val row = Row.newBuilder()
          salesforceColumns.zipWithIndex.foreach {
            case (salesforceColumn, idx) =>
              val salesforceValue = record.get(salesforceColumn)
              row.putData(columns(idx), convertAnyToValue(salesforceValue))
          }
          currentChunk += row.build()
        }

        if (!query.isDone) {
          query = connector.forceApi.queryMore(query.getNextRecordsUrl)
        }
      }

      override def close(): Unit = {}

      override def hasNext: Boolean = {
        currentChunkIndex < currentChunk.size || !query.isDone
      }

      override def next(): Row = {
        if (!hasNext) throw new NoSuchElementException("No more elements")

        if (currentChunkIndex == currentChunk.size) {
          readChunk()
        }

        val row = currentChunk(currentChunkIndex)
        currentChunkIndex += 1
        row
      }
    }
  }

  override def uniqueColumn: String = "id"

  override def insert(row: Row): Row = {
    val newData = row.getDataMap.asScala
      .map { case (k, v) => renameToSalesforce(k) -> rawValueToJavaValue(v) }
      .filter(_._2 != null)
      .toMap
    // Append new "Id" to the row
    val id = connector.forceApi.createSObject(salesforceObjectName, newData.asJava)
    row.toBuilder.putData("id", Value.newBuilder().setString(ValueString.newBuilder().setV(id).build()).build()).build()
  }

  override def update(rowId: Value, newValues: Row): Row = {
    val id = rowId.getString.getV
    val newData = newValues.getDataMap.asScala
      .filter(kv => !fieldsCannotBeUpdated.contains(kv._1))
      .map { case (k, v) => renameToSalesforce(k) -> rawValueToJavaValue(v) }
      .toMap
    logger.debug(s"Updating row with id $id and new values: $newData")
    connector.forceApi.updateSObject(salesforceObjectName, id, newData.asJava)
    newValues
  }

  override def delete(rowId: Value): Unit = {
    val id = rowId.getString.getV
    connector.forceApi.deleteSObject(salesforceObjectName, id)
  }

  // Convert e.g. account_number to AccountNumber
  // This only works because Salesforce native fields never have underscores
  protected def renameToSalesforce(name: String): String = {
    if (name.endsWith("__c")) name // Custom objects must be left as is
    else name.split("_").map(_.capitalize).mkString
  }

  // Convert e.g. Price2Book to price_2_book
  // This only works because Salesforce native fields never have underscores
  protected def renameFromSalesforce(name: String): String = {
    if (name.endsWith("__c")) name // Custom objects must be left as is
    else {
      val parts = name.split("(?=[A-Z0-9])")
      parts.mkString("_").toLowerCase
    }
  }

  private def rawValueToSOQLValue(v: Value): String = {
    if (v.hasInt) v.getInt.getV.toString
    else if (v.hasDouble) v.getDouble.getV.toString
    else if (v.hasString) {
      // Quote string safely for ', including escaping quotes
      "'" + v.getString.getV.replace("'", "\\'") + "'"
    } else if (v.hasBool) v.getBool.getV.toString
    else if (v.hasNull) "NULL"
    else if (v.hasTimestamp) {
      // Format timestamp as ISO 8601
      val year = v.getTimestamp.getYear
      val month = v.getTimestamp.getMonth
      val day = v.getTimestamp.getDay
      val hour = v.getTimestamp.getHour
      val minute = v.getTimestamp.getMinute
      val second = v.getTimestamp.getSecond
      val nano = v.getTimestamp.getNano
      // Salesforce expects the timestamp to be in UTC
      val formatted = f"'$year%04d-$month%02d-$day%02dT$hour%02d:$minute%02d:$second%02d.$nano%09dZ'"
      formatted
    } else {
      throw new IllegalArgumentException(s"Unsupported value: $v")
    }
  }

  private def rawValueToJavaValue(v: Value): Any = {
    if (v.hasInt) v.getInt.getV
    else if (v.hasDouble) v.getDouble.getV
    else if (v.hasString) v.getString.getV
    else if (v.hasBool) v.getBool.getV
    else if (v.hasNull) null
    else if (v.hasTimestamp) {
      val year = v.getTimestamp.getYear
      val month = v.getTimestamp.getMonth
      val day = v.getTimestamp.getDay
      val hour = v.getTimestamp.getHour
      val minute = v.getTimestamp.getMinute
      val second = v.getTimestamp.getSecond
      val nano = v.getTimestamp.getNano
      val formatted = f"$year%04d-$month%02d-$day%02dT$hour%02d:$minute%02d:$second%02d.$nano%09dZ"
      formatted
    } else {
      throw new IllegalArgumentException(s"Unsupported value: $v")
    }
  }

}
