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

class DASSalesforceCaseTable(connector: DASSalesforceConnector)
  extends DASSalesforceTable(connector, "salesforce_case", "Case") {

  override def tableDefinition: TableDefinition = {
    var tbl = TableDefinition
      .newBuilder()
      .setTableId(TableId.newBuilder().setName(tableName))
      .setDescription(
        "Represents a case, which is a customer support or service issue."
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("id")
          .setDescription("Unique identifier for the case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("is_deleted")
          .setDescription("Indicates whether the case has been deleted (true/false).")
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("master_record_id")
          .setDescription("ID of the master record if this record was deleted due to a merge.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("case_number")
          .setDescription("The unique case number assigned to the case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("contact_id")
          .setDescription("ID of the contact associated with this case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("account_id")
          .setDescription("ID of the account associated with this case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("asset_id")
          .setDescription("ID of the asset associated with this case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("source_id")
          .setDescription("ID of the source associated with this case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("parent_id")
          .setDescription("ID of the parent case, if applicable.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("supplied_name")
          .setDescription("Name of the person who supplied the case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("supplied_email")
          .setDescription("Email address of the person who supplied the case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("supplied_phone")
          .setDescription("Phone number of the person who supplied the case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("supplied_company")
          .setDescription("Company name of the person who supplied the case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("type")
          .setDescription("The type of case (e.g., Feature Request, Bug).")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("status")
          .setDescription("Current status of the case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("reason")
          .setDescription("Reason for the case (e.g., Installation Problem, User Error).")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("origin")
          .setDescription("Origin of the case (e.g., Email, Phone, Web).")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("subject")
          .setDescription("The subject of the case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("priority")
          .setDescription("Priority of the case (e.g., High, Medium, Low).")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("description")
          .setDescription("Description of the case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("is_closed")
          .setDescription("Indicates if the case is closed (true/false).")
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("closed_date")
          .setDescription("The date and time the case was closed.")
          .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("is_escalated")
          .setDescription("Indicates if the case is escalated (true/false).")
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("owner_id")
          .setDescription("The ID of the user who owns the case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("created_date")
          .setDescription("The date and time the case was created.")
          .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("created_by_id")
          .setDescription("The ID of the user who created the case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("last_modified_date")
          .setDescription("The date and time the case was last modified.")
          .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("last_modified_by_id")
          .setDescription("The ID of the user who last modified the case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("system_modstamp")
          .setDescription("The system modstamp for the case.")
          .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("contact_phone")
          .setDescription("Phone number of the contact associated with the case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("contact_mobile")
          .setDescription("Mobile phone number of the contact associated with the case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("contact_email")
          .setDescription("Email address of the contact associated with the case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("contact_fax")
          .setDescription("Fax number of the contact associated with the case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("comments")
          .setDescription("Additional comments about the case.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("last_viewed_date")
          .setDescription("The date and time the case was last viewed.")
          .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("last_referenced_date")
          .setDescription("The date and time the case was last referenced.")
          .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .setStartupCost(1000)

    tbl = addDynamicColumns(tbl)
    tbl.build()
  }

}

