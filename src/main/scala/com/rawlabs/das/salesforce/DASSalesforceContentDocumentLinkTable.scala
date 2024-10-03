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

class DASSalesforceContentDocumentLinkTable(connector: DASSalesforceConnector)
    extends DASSalesforceTable(connector, "salesforce_content_document_link", "ContentDocumentLink") {

  override def tableDefinition: TableDefinition = {
    var tbl = TableDefinition
      .newBuilder()
      .setTableId(TableId.newBuilder().setName(tableName))
      .setDescription(
        "Represents the link between a Salesforce CRM Content document (ContentDocument) and where it's shared (LinkedEntityId)."
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("id")
          .setDescription("Unique identifier for the content document link.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("linked_entity_id")
          .setDescription("The ID of the entity to which the document is linked.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("content_document_id")
          .setDescription("The ID of the content document.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("is_deleted")
          .setDescription("Indicates whether the content document link has been deleted (true/false).")
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("system_modstamp")
          .setDescription("The system modstamp for the content document link.")
          .setType(
            Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
          )
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("share_type")
          .setDescription("The type of sharing access for the document.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("visibility")
          .setDescription("The visibility level of the document link (e.g., AllUsers, InternalUsers, SharedUsers).")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .setStartupCost(1000)

    tbl = addDynamicColumns(tbl)
    tbl.build()
  }

}
