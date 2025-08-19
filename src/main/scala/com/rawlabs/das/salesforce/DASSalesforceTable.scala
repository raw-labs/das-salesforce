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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import scala.collection.mutable
import scala.jdk.CollectionConverters.CollectionHasAsScala

import com.rawlabs.das.sdk.DASExecuteResult
import com.rawlabs.das.sdk.scala.DASTable
import com.rawlabs.das.sdk.scala.DASTable.TableEstimate
import com.rawlabs.protocol.das.v1.query.{Operator, PathKey, Qual, SortKey}
import com.rawlabs.protocol.das.v1.tables.{Column, ColumnDefinition, Row, TableDefinition}
import com.rawlabs.protocol.das.v1.types._
import com.typesafe.scalalogging.StrictLogging

abstract class DASSalesforceTable(
    connector: DASSalesforceConnector,
    val tableName: String,
    val salesforceObjectName: String)
    extends DASTable
    with StrictLogging {

  import DASSalesforceUtils._

  assert(
    salesforceObjectName.capitalize == salesforceObjectName,
    "Salesforce object name must be capitalized as per Salesforce API")

  def tableDefinition: TableDefinition

  private val updatableFields = mutable.Set.empty[String]
  private val creatableFields = mutable.Set.empty[String]

  protected def markUpdatable(columnName: String): Unit = {
    updatableFields += columnName
  }

  protected def markCreatable(columnName: String): Unit = {
    creatableFields += columnName
  }

  // Remove hidden columns and add dynamic ones, based on the Salesforce schema.
  // The staticColumns list is coming from the table definition, where it's hardcoded with good documentation.
  // The schema returned by Salesforce permits us to discard hidden ones and add dynamic ones.
  def fixHiddenAndDynamicColumns(staticColumns: Seq[ColumnDefinition]): Seq[ColumnDefinition] = {
    val finalColumns = mutable.ArrayBuffer.empty[ColumnDefinition]
    // First filter the provided static columns to keep those _that are in the table schema returned by Salesforce_.
    val columns = readColumnsFromTable()
    val schemaColumns = columns.map(_.columnDefinition.getName).toSet
    staticColumns
      .filter(c => schemaColumns.contains(c.getName))
      .foreach(finalColumns += _)
    if (connector.addDynamicColumns) {
      val knownColumns = staticColumns.map(_.getName).toSet
      // Second, if configured so, add the dynamic columns: columns that were returned in the Salesforce schema
      // but we haven't picked from the static columns list.
      columns.map(_.columnDefinition).filterNot(c => knownColumns.contains(c.getName)).foreach(finalColumns += _)
    }
    columns.foreach { c =>
      if (c.updatable) markUpdatable(c.columnDefinition.getName)
      if (c.createable) markCreatable(c.columnDefinition.getName)
    }
    finalColumns.toSeq
  }

  case class SalesforceColumn(columnDefinition: ColumnDefinition, updatable: Boolean, createable: Boolean)

  def readColumnsFromTable(): Seq[SalesforceColumn] = {
    val obj = connector.describeSObject(salesforceObjectName)
    obj.getFields.asScala.map { f =>
      val rawType = f.getType match {
        case "string"        => Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build()
        case "id"            => Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build()
        case "reference"     => Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build()
        case "date"          => Type.newBuilder().setDate(DateType.newBuilder().setNullable(true)).build()
        case "datetime"      => Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build()
        case "picklist"      => Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build()
        case "multipicklist" => Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build()
        case "boolean"       => Type.newBuilder().setBool(BoolType.newBuilder().setNullable(true)).build()
        case "textarea"      => Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build()
        case "combobox"      => Type.newBuilder().setDecimal(DecimalType.newBuilder().setNullable(true)).build()
        case "currency"      => Type.newBuilder().setDecimal(DecimalType.newBuilder().setNullable(true)).build()
        case "percent"       => Type.newBuilder().setDecimal(DecimalType.newBuilder().setNullable(true)).build()
        case "double"        => Type.newBuilder().setDouble(DoubleType.newBuilder().setNullable(true)).build()
        case "int"           => Type.newBuilder().setInt(IntType.newBuilder().setNullable(true)).build()
        case "long"          => Type.newBuilder().setLong(LongType.newBuilder().setNullable(true)).build()
        case "address"       => Type.newBuilder().setRecord(RecordType.newBuilder()).build()
        case "base64"        =>
          // Salesforce doesn't support base64 fields in SOQL queries. They are strings, not "binary data".
          // Also the ContentVersion column version_data (base64) is advertised as string.
          Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build()
        case "time"            => Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build()
        case "phone"           => Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build()
        case "url"             => Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build()
        case "email"           => Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build()
        case "encryptedstring" => Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build()
        case "location" =>
          Type
            .newBuilder()
            .setRecord(
              RecordType
                .newBuilder()
                .addAtts(
                  AttrType
                    .newBuilder()
                    .setName("latitude")
                    .setTipe(Type.newBuilder().setDouble(DoubleType.newBuilder().setNullable(true))))
                .addAtts(AttrType
                  .newBuilder()
                  .setName("longitude")
                  .setTipe(Type.newBuilder().setDouble(DoubleType.newBuilder().setNullable(true)))))
            .build()
        case "anyType" => Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build()
        case _ =>
          logger.warn(s"Unhandled Salesforce field type: ${f.getType}, defaulting to StringType.")
          Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build()
      }
      val definition = ColumnDefinition
        .newBuilder()
        .setName(renameFromSalesforce(f.getName))
        .setDescription(f.getLabel)
        .setType(rawType)
        .build()
      SalesforceColumn(definition, updatable = f.isUpdateable, createable = f.isCreateable)
    }.toSeq
  }

  def tableEstimate(quals: Seq[Qual], columns: Seq[String]): TableEstimate = {
    TableEstimate(100, 100)
  }

  // We push down all sorts to Salesforce, so they are all supported
  override def getTableSortOrders(sortKeys: Seq[SortKey]): Seq[SortKey] = sortKeys

  // Query by unique ID returns one row
  override def getTablePathKeys: Seq[PathKey] =
    Seq(PathKey.newBuilder().addKeyColumns(uniqueColumn).setExpectedRows(1).build())

  override def explain(
      quals: Seq[Qual],
      columns: Seq[String],
      sortKeys: Seq[SortKey],
      maybeLimit: Option[Long]): Seq[String] = {
    mkSOQL(quals, columns, sortKeys, maybeLimit).split("\n").toSeq
  }

  override def execute(
      quals: Seq[Qual],
      columns: Seq[String],
      sortKeys: Seq[SortKey],
      maybeLimit: Option[Long]): DASExecuteResult = {
    logger.debug(s"Executing query with columns: $columns, quals: $quals, sortKeys: $sortKeys")

    val soql = mkSOQL(quals, columns, sortKeys, maybeLimit)
    logger.debug(s"Executing SOQL query: $soql")
    val pageIterator = connector.paginatedSOQL(soql)

    val salesforceColumns = columns.map(renameToSalesforce)

    new DASExecuteResult {

      private val rows = pageIterator.flatten.map { record =>
        val row = Row.newBuilder()
        salesforceColumns.zipWithIndex.foreach { case (salesforceColumn, idx) =>
          val salesforceValue = record(salesforceColumn)
          val columnName = columns(idx)
          val rawType = columnTypes(columnName)
          row.addColumns(Column.newBuilder().setName(columnName).setData(soqlValueToRawValue(rawType, salesforceValue)))
        }
        row.build()
      }

      override def close(): Unit = {}

      override def hasNext: Boolean = rows.hasNext

      override def next(): Row = {
        if (!hasNext) throw new NoSuchElementException("No more elements")
        rows.next()
      }
    }

  }

  override def uniqueColumn: String = "id"

  override def insert(row: Row): Row = {
    logger.debug(s"Inserting row: $row")
    val newData = row.getColumnsList.asScala
      .filter(kv => creatableFields.contains(kv.getName)) // ignore fields that are not creatable
      .map(kv => renameToSalesforce(kv.getName) -> rawValueToJavaValue(kv.getData))
      .filter(_._2 != null)
      .toMap
    // Append new "Id" to the row
    val id = connector.createSObject(salesforceObjectName, newData)
    row.toBuilder
      .addColumns(
        Column
          .newBuilder()
          .setName("id")
          .setData(Value.newBuilder().setString(ValueString.newBuilder().setV(id).build()).build()))
      .build()
  }

  override def update(rowId: Value, newValues: Row): Row = {
    logger.debug(s"Updating row with id $rowId and new values: $newValues")
    val id = rowId.getString.getV
    val newData = newValues.getColumnsList.asScala
      .filter(kv => updatableFields.contains(kv.getName)) // ignore fields that are not updatable
      .map(kv => renameToSalesforce(kv.getName) -> rawValueToJavaValue(kv.getData))
      .toMap
    logger.debug(s"Updating row with id $id and new values: $newData")
    connector.updateSObject(salesforceObjectName, id, newData)
    newValues
  }

  override def delete(rowId: Value): Unit = {
    logger.debug(s"Deleting row with id $rowId")
    val id = rowId.getString.getV
    connector.deleteSObject(salesforceObjectName, id)
  }

  private def mkSOQL(
      quals: Seq[Qual],
      columns: Seq[String],
      maybeSortKeys: Seq[SortKey],
      maybeLimit: Option[Long]): String = {
    val salesforceColumns = columns.distinct.map(renameToSalesforce)
    var soql = {
      if (salesforceColumns.isEmpty) {
        s"SELECT ${renameToSalesforce(tableDefinition.getColumns(0).getName)} FROM " + salesforceObjectName
      } else {
        "SELECT " + salesforceColumns.mkString(", ") + " FROM " + salesforceObjectName
      }
    }

    val maybePushed = quals.map(qualToSOQL)
    val skippedPredicates = maybePushed.exists(_.isEmpty)
    val supportedQuals = maybePushed.flatten
    if (supportedQuals.nonEmpty) {
      soql += " WHERE " + supportedQuals.mkString(" AND ")
    }
    if (maybeSortKeys.nonEmpty) {
      soql += " ORDER BY " + maybeSortKeys
        .map { sk =>
          val order = if (sk.getIsReversed) "DESC" else "ASC"
          val nulls = if (sk.getNullsFirst) "NULLS FIRST" else "NULLS LAST"
          renameToSalesforce(sk.getName) + " " + order + " " + nulls
        }
        .mkString(", ")
    }
    if (maybeLimit.isDefined && !skippedPredicates) {
      soql += " LIMIT " + maybeLimit.get
    }
    soql
  }

  private def qualToSOQL(q: Qual): Option[String] = {
    if (q.hasIsAnyQual) {
      if (q.getIsAnyQual.getOperator == Operator.EQUALS) {
        // = ANY is equivalent to IN
        val colName = renameToSalesforce(q.getName)
        val values = q.getIsAnyQual.getValuesList.asScala
        val soqlValues = values.map(rawValueToSOQLValue)
        Some(colName + " IN (" + soqlValues.mkString(", ") + ")")
      } else {
        logger.warn("Unsupported operator in IsAny")
        None
      }
    } else if (q.hasIsAllQual) {
      if (q.getIsAllQual.getOperator == Operator.NOT_EQUALS) {
        // <> ALL is equivalent to NOT IN
        val colName = renameToSalesforce(q.getName)
        val values = q.getIsAllQual.getValuesList.asScala
        val soqlValues = values.map(rawValueToSOQLValue)
        Some(colName + " NOT IN (" + soqlValues.mkString(", ") + ")")
      } else {
        logger.warn("Unsupported operator in IsAll")
        None
      }
    } else {
      val value = q.getSimpleQual.getValue // at this point we know it's a SimpleQual
      val op = q.getSimpleQual.getOperator
      val soqlOp = op match {
        case Operator.EQUALS                => "="
        case Operator.GREATER_THAN          => ">"
        case Operator.GREATER_THAN_OR_EQUAL => ">="
        case Operator.LESS_THAN             => "<"
        case Operator.LESS_THAN_OR_EQUAL    => "<="
        case Operator.NOT_EQUALS            => "<>"
        case _ =>
          logger.warn(s"Unsupported operator: $op")
          return None
      }
      val colType = columnTypes(q.getName)
      val colName = renameToSalesforce(q.getName)
      if (colType.hasDate && value.hasTimestamp) {
        // This isn't supported by Salesforce
        None
      } else if (colType.hasTimestamp && value.hasDate) {
        // Turn the date value into a timestamp value (00:00:00)
        val timestampValue = {
          val date = value.getDate
          Value
            .newBuilder()
            .setTimestamp(ValueTimestamp.newBuilder().setYear(date.getYear).setMonth(date.getMonth).setDay(date.getDay))
            .build()
        }
        Some(renameToSalesforce(q.getName) + " " + soqlOp + " " + rawValueToSOQLValue(timestampValue))
      } else {
        Some(colName + " " + soqlOp + " " + rawValueToSOQLValue(value))
      }
    }
  }

  private def soqlValueToRawValue(t: Type, v: Any): Value = {
    if (v == null) Value.newBuilder().setNull(ValueNull.newBuilder()).build()
    else {
      if (t.hasInt) Value.newBuilder().setInt(ValueInt.newBuilder().setV(v.asInstanceOf[Int]).build()).build()
      else if (t.hasDouble) {
        val value = v match {
          case d: Double => d
          case i: Int    => i.toDouble
          case l: Long   => l.toDouble
          case _         => throw new IllegalArgumentException(s"Unsupported value: $v")
        }
        Value.newBuilder().setDouble(ValueDouble.newBuilder().setV(value).build()).build()
      } else if (t.hasLong) {
        val value = v match {
          case l: Long => l
          case i: Int  => i.toLong
          case _       => throw new IllegalArgumentException(s"Unsupported value: $v")
        }
        Value.newBuilder().setLong(ValueLong.newBuilder().setV(value).build()).build()
      } else if (t.hasDecimal) {
        val value = v.toString
        Value.newBuilder().setDecimal(ValueDecimal.newBuilder().setV(value).build()).build()
      } else if (t.hasString) Value.newBuilder().setString(ValueString.newBuilder().setV(v.toString).build()).build()
      else if (t.hasBool)
        Value.newBuilder().setBool(ValueBool.newBuilder().setV(v.asInstanceOf[Boolean]).build()).build()
      else if (t.hasDate) {
        val localDate = LocalDate.parse(v.asInstanceOf[String])
        Value
          .newBuilder()
          .setDate(
            ValueDate
              .newBuilder()
              .setYear(localDate.getYear)
              .setMonth(localDate.getMonthValue)
              .setDay(localDate.getDayOfMonth)
              .build())
          .build()
      } else if (t.hasTimestamp) {
        val str = v.asInstanceOf[String]
        val localDateTime =
          try {
            java.time.LocalDateTime.parse(str, dateTimeFormatter)
          } catch {
            case e: java.time.format.DateTimeParseException =>
              logger.warn(s"Failed to parse timestamp: $str", e)
              try {
                val date = java.time.LocalDate.parse(str)
                date.atStartOfDay()
              } catch {
                case e: java.time.format.DateTimeParseException =>
                  logger.warn(s"Failed to parse timestamp: $str", e)
                  throw new IllegalStateException(s"Failed to parse timestamp: $str", e)
              }
          }
        Value
          .newBuilder()
          .setTimestamp(
            ValueTimestamp
              .newBuilder()
              .setYear(localDateTime.getYear)
              .setMonth(localDateTime.getMonthValue)
              .setDay(localDateTime.getDayOfMonth)
              .setHour(localDateTime.getHour)
              .setMinute(localDateTime.getMinute)
              .setSecond(localDateTime.getSecond)
              .setNano(localDateTime.getNano)
              .build())
          .build()
      } else if (t.hasRecord) {
        val record = v.asInstanceOf[Map[String, Any]]
        val recordValue = ValueRecord.newBuilder()
        val typeMap = t.getRecord.getAttsList.asScala.map(att => att.getName -> att.getTipe).toMap
        record.foreach { case (k, v) =>
          val fieldValue = typeMap.get(k) match {
            case Some(fieldType) => soqlValueToRawValue(fieldType, v)
            case None            => anySoqlValueToRawValue(v)
          }
          recordValue.addAtts(ValueRecordAttr.newBuilder().setName(k).setValue(fieldValue).build())
        }
        Value.newBuilder().setRecord(recordValue.build()).build()
      } else if (t.hasAny) anySoqlValueToRawValue(v)
      else {
        logger.error(s"Unsupported type: ${t.getTypeCase}")
        throw new IllegalArgumentException(s"Unsupported type: ${t.getClass}")
      }
    }
  }

  private def anySoqlValueToRawValue(v: Any): Value = {
    v match {
      case null       => Value.newBuilder().setNull(ValueNull.newBuilder()).build()
      case s: String  => Value.newBuilder().setString(ValueString.newBuilder().setV(s).build()).build()
      case i: Int     => Value.newBuilder().setInt(ValueInt.newBuilder().setV(i).build()).build()
      case l: Long    => Value.newBuilder().setLong(ValueLong.newBuilder().setV(l).build()).build()
      case d: Double  => Value.newBuilder().setDouble(ValueDouble.newBuilder().setV(d).build()).build()
      case b: Boolean => Value.newBuilder().setBool(ValueBool.newBuilder().setV(b).build()).build()
      case m: Map[_, _] =>
        val record = ValueRecord.newBuilder()
        m.foreach { case (k, v) =>
          record.addAtts(ValueRecordAttr.newBuilder().setName(k.toString).setValue(anySoqlValueToRawValue(v)).build())
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

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

  private val columnTypes: Map[String, Type] =
    tableDefinition.getColumnsList.asScala.map(c => c.getName -> c.getType).toMap

}
