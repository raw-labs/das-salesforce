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

import com.rawlabs.protocol.das.{ColumnDefinition, TableDefinition, TableId}
import com.rawlabs.protocol.raw.{BoolType, StringType, TimestampType, Type}

class DASSalesforceProductTable(connector: DASSalesforceConnector)
    extends DASSalesforceTable(connector, "salesforce_product", "Product2") {

  override def tableDefinition: TableDefinition = {
    var tbl = TableDefinition
      .newBuilder()
      .setTableId(TableId.newBuilder().setName(tableName))
      .setDescription(
        "Represents a product that org sells."
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("id")
          .setDescription("Unique identifier of the product in Salesforce.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("name")
          .setDescription("The product's name.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("family")
          .setDescription("Name of the product family associated with this record.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("product_code")
          .setDescription("The internal code or product number that you use to identify the product.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("is_active")
          .setDescription(
            "Indicates that the product is ready for use in a price book, opportunity, or quote, and whether you can see the product in views."
          )
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("created_by_id")
          .setDescription("The id of the user who created the product record, with the date and time of creation.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("created_date")
          .setDescription("Date and time of the creation of the product role record.")
          .setType(
            Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
          )
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("description")
          .setDescription("Description of the product.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("display_url")
          .setDescription("URL leading to a specific version of a record in the linked external data source.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("external_data_source_id")
          .setDescription("The id of the related external data source.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("external_id")
          .setDescription("The unique identifier of a record in the linked external data source.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("is_archived")
          .setDescription("Describes whether the product is archived. The default value is false.")
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("is_deleted")
          .setDescription("Indicates whether the object has been moved to the Recycle Bin (true) or not (false).")
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("last_modified_by_id")
          .setDescription("Id of the user who most recently changed the product record.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("last_modified_date")
          .setDescription("Date of most recent change in the product record.")
          .setType(
            Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
          )
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("last_referenced_date")
          .setDescription("The timestamp when the current user last viewed product record.")
          .setType(
            Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
          )
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("last_viewed_date")
          .setDescription(
            "The timestamp when the current user last viewed this record. If this value is null, this record might only have been referenced (last_referenced_date) and not viewed by the current user."
          )
          .setType(
            Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
          )
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("quantity_unit_of_measure")
          .setDescription("Unit of the product—for example, kilograms, liters, or cases.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("stock_keeping_unit")
          .setDescription("The product's SKU, which can be used with or in place of the Product Code field.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("system_modstamp")
          .setDescription("The date and time when order record was last modified by a user or by an automated process.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .setStartupCost(1000)
    tbl = addDynamicColumns(tbl)
    tbl.build()
  }

}
