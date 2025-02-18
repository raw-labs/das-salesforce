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

class DASSalesforceTaskTable(connector: DASSalesforceConnector)
    extends DASSalesforceTable(connector, "salesforce_task", "Task") {

  override def tableDefinition: TableDefinition = {
    val tbl = TableDefinition
      .newBuilder()
      .setTableId(TableId.newBuilder().setName(tableName))
      .setDescription(
        "Represents an activity related to records such as Accounts, Contacts, Leads, Opportunities, and other Salesforce objects.")
    val columns = Seq(
      ColumnDefinition
        .newBuilder()
        .setName("id")
        .setDescription("The unique ID for the task.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("subject")
        .setDescription("A short description or title of the task.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("status")
        .setDescription("The current status of the task.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("priority")
        .setDescription("Priority level of the task (e.g., High, Normal, Low).")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("activity_date")
        .setDescription("The due date of the task.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("description")
        .setDescription("A detailed description of the task.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("owner_id")
        .setDescription("The ID of the user or queue that owns the task.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("what_id")
        .setDescription("The related object for the task (e.g., Account, Opportunity, etc.).")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("who_id")
        .setDescription("The ID of the person (Contact or Lead) related to the task.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("created_by_id")
        .setDescription("The ID of the user who created the task.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("created_date")
        .setDescription("The date and time when the task was created.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("last_modified_by_id")
        .setDescription("The ID of the user who last modified the task.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("last_modified_date")
        .setDescription("The date and time when the task was last modified.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("is_closed")
        .setDescription("Indicates whether the task is closed.")
        .setType(Type.newBuilder().setBool(BoolType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("is_deleted")
        .setDescription("Indicates whether the task has been deleted.")
        .setType(Type.newBuilder().setBool(BoolType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("is_high_priority")
        .setDescription("A boolean field that flags whether the task is marked as high priority.")
        .setType(Type.newBuilder().setBool(BoolType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("reminder_date_time")
        .setDescription("The date and time for a task reminder.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("is_reminder_set")
        .setDescription("Boolean field to indicate if a reminder has been set for the task.")
        .setType(Type.newBuilder().setBool(BoolType.newBuilder().setNullable(true)).build())
        .build(),
      // RelatedTo
      //        ColumnDefinition
      //          .newBuilder()
      //          .setName("related_to")
      //          .setDescription(
      //            "A polymorphic field for associating the task with various objects (e.g., Accounts, Opportunities)."
      //          )
      //          .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
      //          .build(),
      ColumnDefinition
        .newBuilder()
        .setName("account_id")
        .setDescription("The ID of the account related to the task.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      //        ColumnDefinition
      //          .newBuilder()
      //          .setName("opportunity_id")
      //          .setDescription("The ID of the opportunity related to the task.")
      //          .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
      //          .build(),
      ColumnDefinition
        .newBuilder()
        .setName("call_type")
        .setDescription("If the task is a phone call, this indicates whether it was Inbound or Outbound.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("call_duration_in_seconds")
        .setDescription("The length of the call in seconds (if the task is a phone call).")
        .setType(Type.newBuilder().setDouble(DoubleType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("call_disposition")
        .setDescription("The result or outcome of the phone call (e.g., Reached, Left Message).")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("is_recurrence")
        .setDescription("Indicates if the task is recurring.")
        .setType(Type.newBuilder().setBool(BoolType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("recurrence_interval")
        .setDescription("The number of intervals between recurring tasks.")
        .setType(Type.newBuilder().setDouble(DoubleType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("recurrence_end_date_only")
        .setDescription("The end date of the recurring task series.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("recurrence_day_of_week_mask")
        .setDescription("A bitmask that specifies the day of the week for recurrence.")
        .setType(Type.newBuilder().setDouble(DoubleType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("recurrence_type")
        .setDescription("Specifies the type of recurrence (e.g., RecursDaily, RecursWeekly, etc.).")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build())
    fixHiddenAndDynamicColumns(columns).foreach(tbl.addColumns)
    tbl.setStartupCost(1000)
    tbl.build()
  }
}
