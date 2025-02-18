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

import com.rawlabs.protocol.das.v1.tables.{ColumnDefinition, TableDefinition, TableId}
import com.rawlabs.protocol.das.v1.types._

class DASSalesforceOpportunityLineItemTable(connector: DASSalesforceConnector)
    extends DASSalesforceTable(connector, "salesforce_opportunity_line_item", "OpportunityLineItem") {

  override def tableDefinition: TableDefinition = {
    val tbl = TableDefinition
      .newBuilder()
      .setTableId(TableId.newBuilder().setName(tableName))
      .setDescription(
        "Represents a product added to an opportunity in Salesforce, storing details such as product ID, quantity, price, etc.")
    val columns = Seq(
      ColumnDefinition
        .newBuilder()
        .setName("id")
        .setDescription("The unique ID for the opportunity line item.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("opportunity_id")
        .setDescription("The ID of the related opportunity.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("sort_order")
        .setDescription("The sort order for the opportunity line item.")
        .setType(Type.newBuilder().setDouble(DoubleType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("pricebook_entry_id")
        .setDescription("The ID of the related pricebook entry.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("product_2_id")
        .setDescription("The ID of the related product.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("product_code")
        .setDescription("The code of the product being sold.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("name")
        .setDescription("The name of the opportunity line item.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("quantity")
        .setDescription("The quantity of the product being sold.")
        .setType(Type.newBuilder().setDouble(DoubleType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("total_price")
        .setDescription("The total price of the product being sold.")
        .setType(Type.newBuilder().setDouble(DoubleType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("unit_price")
        .setDescription("The price per unit of the product.")
        .setType(Type.newBuilder().setDouble(DoubleType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("list_price")
        .setDescription("The list price of the product.")
        .setType(Type.newBuilder().setDouble(DoubleType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("service_date")
        .setDescription("The date when the service or product is delivered.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("description")
        .setDescription("A detailed description of the product or service.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("created_date")
        .setDescription("The date and time when the opportunity line item was created.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("created_by_id")
        .setDescription("The ID of the user who created the opportunity line item.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("last_modified_date")
        .setDescription("The date and time when the opportunity line item was last modified.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("last_modified_by_id")
        .setDescription("The ID of the user who last modified the opportunity line item.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("system_modstamp")
        .setDescription("The timestamp for system modifications.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("is_deleted")
        .setDescription("Indicates whether the opportunity line item has been deleted.")
        .setType(Type.newBuilder().setBool(BoolType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("last_viewed_date")
        .setDescription("The date when the opportunity line item was last viewed.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("last_referenced_date")
        .setDescription("The date when the opportunity line item was last referenced.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build())
        .build())
    fixHiddenAndDynamicColumns(columns).foreach(tbl.addColumns)
    tbl.setStartupCost(1000)
    tbl.build()
  }
}
