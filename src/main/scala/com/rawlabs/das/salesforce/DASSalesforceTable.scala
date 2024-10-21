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

import com.rawlabs.das.sdk.DASTable
import com.rawlabs.protocol.das._
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
  protected def fixHiddenAndDynamicColumns(staticColumns: Seq[ColumnDefinition]): Seq[ColumnDefinition] = {
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

  protected def readColumnsFromTable(): Seq[ColumnDefinition] = {
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

  override def uniqueColumn: String = "id"

  override def insert(row: Row): Row = {
    val newData = row.getColumnsList.asScala
      .map(c => renameToSalesforce(c.getName) -> rawValueToJavaValue(c.getData))
      .filter(_._2 != null)
      .toMap
    // Append new "Id" to the row
    val id = connector.forceApi.createSObject(salesforceObjectName, newData.asJava)
    row.toBuilder
      .addColumns(
        Column
          .newBuilder()
          .setName("id")
          .setData(Value.newBuilder().setString(ValueString.newBuilder().setV(id).build()).build())
          .build()
      )
      .build()
  }

  override def update(rowId: Value, newValues: Row): Row = {
    val id = rowId.getString.getV
    val newData = newValues.getColumnsList.asScala
      .filter(c => !readOnlyFields.contains(c.getName))
      .map(c => renameToSalesforce(c.getName) -> rawValueToJavaValue(c.getData))
      .toMap
    logger.debug(s"Updating row with id $id and new values: $newData")
    connector.forceApi.updateSObject(salesforceObjectName, id, newData.asJava)
    newValues
  }

  override def delete(rowId: Value): Unit = {
    val id = rowId.getString.getV
    connector.forceApi.deleteSObject(salesforceObjectName, id)
  }

}
