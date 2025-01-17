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
import com.rawlabs.protocol.das.v1.types.{DateType, DoubleType, StringType, TimestampType, Type}

class DASSalesforceDatedConversionRateTable(connector: DASSalesforceConnector)
    extends DASSalesforceTable(connector, "salesforce_dated_conversion_rate", "DatedConversionRate") {
  override def tableDefinition: TableDefinition = {
    val tbl = TableDefinition
      .newBuilder()
      .setTableId(TableId.newBuilder().setName(tableName))
      .setDescription(
        "Represents the dated conversion rates for a currency pair."
      )
    val columns = Seq(
      ColumnDefinition
        .newBuilder()
        .setName("id")
        .setDescription("DatedConversion Rate ID.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("iso_code")
        .setDescription("ISO code of the currency (ISO 4217 standard).")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("start_date")
        .setDescription("The date on which the effective dated exchange rate starts.")
        .setType(Type.newBuilder().setDate(DateType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("next_start_date")
        .setDescription(
          "The date on which the next effective dated exchange rate will start. Effectively the day after the end date for this exchange rate."
        )
        .setType(Type.newBuilder().setDate(DateType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("conversion_rate")
        .setDescription("Conversion rate of this currency type against the corporate currency.")
        .setType(Type.newBuilder().setDouble(DoubleType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("created_date")
        .setDescription("Created Date.")
        .setType(
          Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build()
        )
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("created_by_id")
        .setDescription("Created By ID.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("last_modified_date")
        .setDescription("Last Modified Date.")
        .setType(
          Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build()
        )
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("last_modified_by_id")
        .setDescription("Last Modified By ID.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("system_modstamp")
        .setDescription("System Modstamp.")
        .setType(
          Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build()
        )
        .build()
    )
    fixHiddenAndDynamicColumns(columns).foreach(tbl.addColumns)
    tbl.setStartupCost(1000)
    tbl.build()
  }

}
