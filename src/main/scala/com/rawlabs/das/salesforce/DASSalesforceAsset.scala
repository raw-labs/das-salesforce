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
import com.rawlabs.protocol.raw.{BoolType, DoubleType, IntType, StringType, TimestampType, Type}

class DASSalesforceAsset(connector: DASSalesforceConnector)
    extends DASSalesforceTable(connector, "salesforce_asset", "Asset") {

  override def tableDefinition: TableDefinition = {
    var tbl = TableDefinition
      .newBuilder()
      .setTableId(TableId.newBuilder().setName(tableName))
      .setDescription(
        "Represents an item of commercial value, such as a product sold by your company or a competitor, that a customer has purchased and installed."
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("id")
          .setDescription("Unique identifier of the product in asset.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("name")
          .setDescription("Name of the asset.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("account_id")
          .setDescription("ID of the Account associated with this asset.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("asset_level")
          .setDescription(
            "The asset's position in an asset hierarchy. If the asset has no parent or child assets, its level is 1."
          )
          .setType(Type.newBuilder().setInt(IntType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("contact_id")
          .setDescription("ID of the Contact associated with this asset.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("asset_provided_by_id")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .setDescription("The account that provided the asset, typically a manufacturer.")
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("asset_serviced_by_id")
          .setDescription("The account in charge of servicing the asset.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("created_by_id")
          .setDescription("The id of the user who created the asset.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("created_date")
          .setDescription("Date and time of the creation of the price book record.")
          .setType(
            Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
          )
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("description")
          .setDescription("Description of the asset.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("install_date")
          .setDescription("Date when the asset was installed.")
          .setType(
            Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
          )
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("is_competitor_product")
          .setDescription(
            "Indicates whether this Asset represents a product sold by a competitor (true) or not (false)."
          )
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("is_deleted")
          .setDescription("The Deleted.")
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("is_internal")
          .setDescription(
            "Indicates that the asset is produced or used internally (true) or not (false). Default value is false."
          )
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("last_modified_by_id")
          .setDescription("The Last Modified By ID.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("last_modified_date")
          .setDescription("The Last Modified Date.")
          .setType(
            Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
          )
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("last_referenced_date")
          .setDescription("The date and time that the asset was last modified.")
          .setType(
            Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
          )
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("last_viewed_date")
          .setDescription("The date and time that the asset was last viewed.")
          .setType(
            Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
          )
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("owner_id")
          .setDescription(
            "The asset's owner. By default, the asset owner is the user who created the asset record."
          )
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("parent_id")
          .setDescription("The asset's parent asset.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("price")
          .setDescription("Price paid for this asset.")
          .setType(Type.newBuilder().setDouble(DoubleType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("product_2_id")
          .setDescription("ID of the Product2 associated with this asset. Must be a valid Product2 ID.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("product_code")
          .setDescription("The product code of the related product.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("purchase_date")
          .setDescription("Date on which this asset was purchased.")
          .setType(
            Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
          )
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("quantity")
          .setDescription("Quantity purchased or installed.")
          .setType(Type.newBuilder().setDouble(DoubleType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("root_asset_id")
          .setDescription(
            "The top-level asset in an asset hierarchy. Depending on where an asset lies in the hierarchy, its root could be the same as its parent."
          )
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("serial_number")
          .setDescription("Serial number for this asset.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("status")
          .setDescription(
            "Customizable picklist of values. The default picklist includes the following values: Purchased, Shipped, Installed, Registered, Obsolete."
          )
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("stock_keeping_unit")
          .setDescription("The SKU assigned to the related product.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("system_modstamp")
          .setDescription("The System Modstamp.")
          .setType(
            Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
          )
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("usage_end_date")
          .setDescription("Date when usage for this asset ends or expires.")
          .setType(
            Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
          )
          .build()
      )
      .setStartupCost(1000)
    tbl = addDynamicColumns(tbl)
    tbl.build()
  }

}
