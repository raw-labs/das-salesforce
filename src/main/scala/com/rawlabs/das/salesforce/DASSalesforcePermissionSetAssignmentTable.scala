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
import com.rawlabs.protocol.das.v1.types.{StringType, TimestampType, Type}

class DASSalesforcePermissionSetAssignmentTable(connector: DASSalesforceConnector)
    extends DASSalesforceTable(connector, "salesforce_permission_set_assignment", "PermissionSetAssignment") {

  override def tableDefinition: TableDefinition = {
    val tbl = TableDefinition
      .newBuilder()
      .setTableId(TableId.newBuilder().setName(tableName))
      .setDescription("Represents the association between a User and a PermissionSet.")
    val columns = Seq(
      ColumnDefinition
        .newBuilder()
        .setName("assignee_id")
        .setDescription("ID of the User to assign the permission set specified in PermissionSetId.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("id")
        .setDescription("The Permission Set Assignment ID.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("permission_set_group_id")
        .setDescription("If associated with a permission set group, this is the ID of that group.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("permission_set_id")
        .setDescription("ID of the PermissionSet to assign to the user specified in AssigneeId.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("system_modstamp")
        .setDescription("The Date Assigned.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build())
        .build())
    fixHiddenAndDynamicColumns(columns).foreach(tbl.addColumns)
    tbl.setStartupCost(1000)
    tbl.build()
  }

}
