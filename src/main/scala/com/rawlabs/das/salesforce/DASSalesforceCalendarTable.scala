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

class DASSalesforceCalendarTable(connector: DASSalesforceConnector)
    extends DASSalesforceTable(connector, "salesforce_calendar", "Calendar") {

  override def tableDefinition: TableDefinition = {
    val tbl = TableDefinition
      .newBuilder()
      .setTableId(TableId.newBuilder().setName(tableName))
      .setDescription("Represents a calendar, used to track schedules and other related activities.")
    val columns = Seq(
      ColumnDefinition
        .newBuilder()
        .setName("id")
        .setDescription("Unique identifier for the calendar.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("name")
        .setDescription("The name of the calendar.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("user_id")
        .setDescription("ID of the user associated with this calendar.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("type")
        .setDescription("Type of the calendar (e.g., Shared, Personal).")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("is_active")
        .setDescription("Indicates whether the calendar is active (true/false).")
        .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("created_date")
        .setDescription("The date and time when the calendar was created.")
        .setType(
          Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
        )
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("created_by_id")
        .setDescription("ID of the user who created the calendar.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("last_modified_date")
        .setDescription("The date and time when the calendar was last modified.")
        .setType(
          Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
        )
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("last_modified_by_id")
        .setDescription("ID of the user who last modified the calendar.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("system_modstamp")
        .setDescription("The system modstamp of the calendar.")
        .setType(
          Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
        )
        .build()
    )
    fixHiddenAndDynamicColumns(columns).foreach(tbl.addColumns)
    tbl.setStartupCost(1000)
    tbl.build()
  }

}
