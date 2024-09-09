package com.rawlabs.das.salesforce
import com.rawlabs.protocol.das.{ColumnDefinition, TableDefinition, TableId}
import com.rawlabs.protocol.raw.{BoolType, DateType, DecimalType, DoubleType, IntType, StringType, TimestampType, Type}

import scala.collection.JavaConverters._

class DASSalesforceDynamicTable(connector: DASSalesforceConnector, objectName: String)
    extends DASSalesforceTable(connector, s"salesforce_${objectName.toLowerCase}", objectName) {

  override def tableDefinition: TableDefinition = {
    val tbl = TableDefinition
      .newBuilder()
      .setTableId(TableId.newBuilder().setName(tableName))
      .setDescription(s"Custom object: $objectName.")

    val obj = connector.forceApi.describeSObject(objectName)
    obj.getFields.asScala.foreach { f =>
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
        case "boolean" => Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build()
        case "textarea" =>
          Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build()
        case "currency" =>
          Type.newBuilder().setDecimal(DecimalType.newBuilder().setTriable(false).setNullable(true)).build()
        case "percent" =>
          Type.newBuilder().setDecimal(DecimalType.newBuilder().setTriable(false).setNullable(true)).build()
        case "double" =>
          Type.newBuilder().setDouble(DoubleType.newBuilder().setTriable(false).setNullable(true)).build()
        case "int" => Type.newBuilder().setInt(IntType.newBuilder().setTriable(false).setNullable(true)).build()
        case _ => throw new IllegalArgumentException(s"Unsupported type: ${f.getType}")
      }
      tbl.addColumns(
        ColumnDefinition
          .newBuilder()
          .setName(renameFromSalesforce(f.getName))
          .setDescription(f.getLabel)
          .setType(rawType)
          .build()
      )
    }
    tbl.build()
  }

}
