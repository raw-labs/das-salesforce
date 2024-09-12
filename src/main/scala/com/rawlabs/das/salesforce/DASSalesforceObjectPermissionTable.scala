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
import com.rawlabs.protocol.raw.{BoolType, StringType, Type}

class DASSalesforceObjectPermissionTable(connector: DASSalesforceConnector)
    extends DASSalesforceTable(connector, "salesforce_object_permission", "ObjectPermissions") {

  override def tableDefinition: TableDefinition = {
    var tbl = TableDefinition
      .newBuilder()
      .setTableId(TableId.newBuilder().setName(tableName))
      .setDescription(
        "Represents the enabled object permissions for the parent PermissionSet."
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("id")
          .setDescription("The ObjectPermissions ID.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("parent_id")
          .setDescription("The Id of this object's parent PermissionSet.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("sobject_type")
          .setDescription("The object's API name. For example, Merchandise__c.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("permissions_create")
          .setDescription(
            "If true, users assigned to the parent PermissionSet can create records for this object. Requires PermissionsRead for the same object to be true."
          )
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("permissions_delete")
          .setDescription(
            "If true, users assigned to the parent PermissionSet can delete records for this object. Requires PermissionsRead and PermissionsEdit for the same object to be true."
          )
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("permissions_edit")
          .setDescription(
            "If true, users assigned to the parent PermissionSet can edit records for this object. Requires PermissionsRead for the same object to be true."
          )
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("permissions_read")
          .setDescription("If true, users assigned to the parent PermissionSet can view records for this object.")
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("permissions_modify_all_records")
          .setDescription(
            "If true, users assigned to the parent PermissionSet can edit all records for this object, regardless of sharing settings. Requires PermissionsRead, PermissionsDelete, PermissionsEdit, and PermissionsViewAllRecords for the same object to be true."
          )
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("permissions_view_all_records")
          .setDescription(
            "If true, users assigned to the parent PermissionSet can view all records for this object, regardless of sharing settings. Requires PermissionsRead for the same object to be true."
          )
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .setStartupCost(1000)
    tbl = addDynamicColumns(tbl)
    tbl.build()
  }

}
