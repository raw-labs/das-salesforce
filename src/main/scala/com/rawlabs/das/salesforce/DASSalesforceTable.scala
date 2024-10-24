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
import com.rawlabs.protocol.das.{ColumnDefinition, Qual, Row, SortKey, TableDefinition}
import com.rawlabs.protocol.raw.{
  AttrType,
  BinaryType,
  BoolType,
  DateType,
  DecimalType,
  DoubleType,
  IntType,
  LongType,
  RecordType,
  StringType,
  TimestampType,
  Type,
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

  import DASSalesforceUtils._

  assert(
    salesforceObjectName.capitalize == salesforceObjectName,
    "Salesforce object name must be capitalized as per Salesforce API"
  )

  def tableDefinition: TableDefinition

  protected def fieldsCannotBeUpdated: Seq[String] = Seq(
    "id",
    "is_deleted",
    "created_by_id",
    "created_date",
    "last_modified_by_id",
    "last_modified_date",
    "system_modstamp"
  )

  private def readOnlyFields: Seq[String] = {
    if (uniqueColumn.nonEmpty) {
      fieldsCannotBeUpdated :+ uniqueColumn
    } else {
      fieldsCannotBeUpdated
    }
  }

  // Remove hidden columns and add dynamic ones, based on the Salesforce schema.
  // The staticColumns list is coming from the table definition, where it's hardcoded with good documentation.
  // The schema returned by Salesforce permits us to discard hidden ones and add dynamic ones.
  def fixHiddenAndDynamicColumns(staticColumns: Seq[ColumnDefinition]): Seq[ColumnDefinition] = {
    val finalColumns = mutable.ArrayBuffer.empty[ColumnDefinition]
    // First filter the provided static columns to keep those _that are in the table schema returned by Salesforce_.
    val salesforceTableSchema = readColumnsFromTable()
    val schemaColumns = salesforceTableSchema.map(_.getName).toSet
    staticColumns
      .filter(c => schemaColumns.contains(c.getName))
      .foreach(finalColumns += _)
    if (connector.addDynamicColumns) {
      val knownColumns = staticColumns.map(_.getName).toSet
      // Second, if configured so, add the dynamic columns: columns that were returned in the Salesforce schema
      // but we haven't picked from the static columns list.
      salesforceTableSchema.filterNot(c => knownColumns.contains(c.getName)).foreach(finalColumns += _)
    }
    finalColumns
  }

  def readColumnsFromTable(): Seq[ColumnDefinition] = {
    val obj = connector.forceApi.describeSObject(salesforceObjectName)
    obj.getFields.asScala.map { f =>
      val rawType = f.getType match {
        case "string" =>
          Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build()
        case "id" => Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build()
        case "reference" =>
          Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build()
        case "date" => Type.newBuilder().setDate(DateType.newBuilder().setTriable(false).setNullable(true)).build()
        case "datetime" =>
          Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
        case "picklist" =>
          Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build()
        case "multipicklist" =>
          Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build()
        case "boolean" => Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build()
        case "textarea" =>
          Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build()
        case "combobox" =>
          Type.newBuilder().setDecimal(DecimalType.newBuilder().setTriable(false).setNullable(true)).build()
        case "currency" =>
          Type.newBuilder().setDecimal(DecimalType.newBuilder().setTriable(false).setNullable(true)).build()
        case "percent" =>
          Type.newBuilder().setDecimal(DecimalType.newBuilder().setTriable(false).setNullable(true)).build()
        case "double" =>
          Type.newBuilder().setDouble(DoubleType.newBuilder().setTriable(false).setNullable(true)).build()
        case "int" => Type.newBuilder().setInt(IntType.newBuilder().setTriable(false).setNullable(true)).build()
        case "long" => Type.newBuilder().setLong(LongType.newBuilder().setTriable(false).setNullable(true)).build()
        case "address" => Type.newBuilder().setRecord(RecordType.newBuilder()).build()
        case "base64" =>
          Type.newBuilder().setBinary(BinaryType.newBuilder().setTriable(false).setNullable(true)).build()
        case "time" =>
          Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
        case "phone" => Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build()
        case "url" => Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build()
        case "email" => Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build()
        case "encryptedstring" =>
          Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build()
        case "location" => Type
            .newBuilder()
            .setRecord(
              RecordType
                .newBuilder()
                .addAtts(
                  AttrType
                    .newBuilder()
                    .setIdn("latitude")
                    .setTipe(Type.newBuilder().setDouble(DoubleType.newBuilder().setTriable(false).setNullable(true)))
                )
                .addAtts(
                  AttrType
                    .newBuilder()
                    .setIdn("longitude")
                    .setTipe(Type.newBuilder().setDouble(DoubleType.newBuilder().setTriable(false).setNullable(true)))
                )
            )
            .build()
        case "anyType" =>
          Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build()
        case _ =>
          logger.warn(s"Unhandled Salesforce field type: ${f.getType}, defaulting to StringType.")
          Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build()
      }
      ColumnDefinition
        .newBuilder()
        .setName(renameFromSalesforce(f.getName))
        .setDescription(f.getLabel)
        .setType(rawType)
        .build()
    }
  }

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
    logger.debug(s"Executing query with columns: $columns, quals: $quals, sortKeys: $maybeSortKeys, limit: $maybeLimit")
    val salesforceColumns = columns.distinct.map(renameToSalesforce)

    var soql = {
      if (salesforceColumns.isEmpty) {
        s"SELECT ${renameToSalesforce(tableDefinition.getColumns(0).getName)} FROM " + salesforceObjectName
      } else {
        "SELECT " + salesforceColumns.mkString(", ") + " FROM " + salesforceObjectName
      }
    }
    val (supportedQuals, unsupportedQuals) = quals.partition(_.hasSimpleQual) // Only simple quals are supported
    if (supportedQuals.nonEmpty) {
      soql += " WHERE " + supportedQuals
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
          renameToSalesforce(q.getFieldName) + " " + soqlOp + " " + rawValueToSOQLValue(q.getSimpleQual.getValue)
        }
        .mkString(" AND ")
    }
    if (maybeSortKeys.nonEmpty) {
      soql += " ORDER BY " + maybeSortKeys.get
        .map { sk =>
          val order = if (sk.getIsReversed) "DESC" else "ASC"
          val nulls = if (sk.getNullsFirst) "NULLS FIRST" else "NULLS LAST"
          renameToSalesforce(sk.getName) + " " + order + " " + nulls
        }
        .mkString(", ")
    }
    if (unsupportedQuals.isEmpty) {
      // LIMIT can be pushed _only_ when there are no unsupported quals.
      if (maybeLimit.nonEmpty) {
        soql += " LIMIT " + maybeLimit.get
      }
    } else {
      maybeLimit.foreach(_ => logger.warn("Unsupported quals found, ignoring LIMIT"))
    }
    logger.debug(s"Executing SOQL query: $soql")
    var query = connector.forceApi.query(soql)

    new DASExecuteResult {
      private val currentChunk: mutable.Buffer[Row] = mutable.Buffer.empty
      private var currentChunkIndex: Int = 0

      readChunk()

      private def readChunk(): Unit = {
        currentChunk.clear()
        currentChunkIndex = 0
        query.getRecords.asScala.foreach { record =>
          val row = Row.newBuilder()
          salesforceColumns.zipWithIndex.foreach {
            case (salesforceColumn, idx) =>
              val salesforceValue = record.get(salesforceColumn)
              row.putData(columns(idx), soqlValueToRawValue(salesforceValue))
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
      .filter(kv => !readOnlyFields.contains(kv._1))
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

  private def soqlValueToRawValue(v: Any): Value = {
    v match {
      case null => Value.newBuilder().setNull(ValueNull.newBuilder()).build()
      case s: String => Value.newBuilder().setString(ValueString.newBuilder().setV(s).build()).build()
      case i: Int => Value.newBuilder().setInt(ValueInt.newBuilder().setV(i).build()).build()
      case d: Double => Value.newBuilder().setDouble(ValueDouble.newBuilder().setV(d).build()).build()
      case b: Boolean => Value.newBuilder().setBool(ValueBool.newBuilder().setV(b).build()).build()
      case m: Map[_, _] =>
        val record = ValueRecord.newBuilder()
        m.foreach {
          case (k: String, v) =>
            record.addFields(ValueRecordField.newBuilder().setName(k).setValue(soqlValueToRawValue(v)).build())
        }
        Value.newBuilder().setRecord(record.build()).build()
      case t =>
        logger.error(s"Unsupported type: ${t.getClass} (type = ${t.getClass})")
        throw new IllegalArgumentException(s"Unsupported type: ${t.getClass}")
    }
  }

  private def rawValueToSOQLValue(v: Value): String = {
    if (v.hasInt) {
      v.getInt.getV.toString
    } else if (v.hasLong) {
      v.getLong.getV.toString
    } else if (v.hasDouble) {
      v.getDouble.getV.toString
    } else if (v.hasDecimal) {
      v.getDecimal.getV.toString
    } else if (v.hasString) {
      // Quote string safely for ', including escaping quotes
      "'" + v.getString.getV.replace("'", "\\'") + "'"
    } else if (v.hasBool) {
      v.getBool.getV.toString
    } else if (v.hasNull) {
      "NULL"
    } else if (v.hasTime) {
      // Format time as ISO 8601
      val hour = v.getTime.getHour
      val minute = v.getTime.getMinute
      val second = v.getTime.getSecond
      val nano = v.getTime.getNano
      f"$hour%02d:$minute%02d:$second%02d.$nano%09dZ"
    } else if (v.hasDate) {
      // Format date as ISO 8601
      val year = v.getDate.getYear
      val month = v.getDate.getMonth
      val day = v.getDate.getDay
      f"$year%04d-$month%02d-$day%02d"
    } else if (v.hasTimestamp) {
      // Format timestamp as ISO 8601
      val year = v.getTimestamp.getYear
      val month = v.getTimestamp.getMonth
      val day = v.getTimestamp.getDay
      val hour = v.getTimestamp.getHour
      val minute = v.getTimestamp.getMinute
      val second = v.getTimestamp.getSecond
      val nano = v.getTimestamp.getNano
      f"$year%04d-$month%02d-$day%02dT$hour%02d:$minute%02d:$second%02d.$nano%09dZ"
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
