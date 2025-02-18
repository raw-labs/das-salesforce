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

class DASSalesforceContentDocumentTable(connector: DASSalesforceConnector)
    extends DASSalesforceTable(connector, "salesforce_content_document", "ContentDocument") {

  override def tableDefinition: TableDefinition = {
    val tbl = TableDefinition
      .newBuilder()
      .setTableId(TableId.newBuilder().setName(tableName))
      .setDescription(
        "Represents a document that has been uploaded to a library in Salesforce CRM Content or Salesforce Files.")
    val columns = Seq(
      ColumnDefinition
        .newBuilder()
        .setName("id")
        .setDescription("Unique identifier for the document.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("created_by_id")
        .setDescription("The ID of the user who created the document.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("created_date")
        .setDescription("The date and time when the document was created.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("last_modified_by_id")
        .setDescription("The ID of the user who last modified the document.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("last_modified_date")
        .setDescription("The date and time when the document was last modified.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("is_archived")
        .setDescription("Indicates if the document is archived (true/false).")
        .setType(Type.newBuilder().setBool(BoolType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("archived_by_id")
        .setDescription("The ID of the user who archived the document.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("archived_date")
        .setDescription("The date when the document was archived.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("is_deleted")
        .setDescription("Indicates if the document has been deleted (true/false).")
        .setType(Type.newBuilder().setBool(BoolType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("owner_id")
        .setDescription("The ID of the user who owns the document.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("system_modstamp")
        .setDescription("The system modstamp of the document.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("title")
        .setDescription("The title of the document.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("publish_status")
        .setDescription("The publish status of the document.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("latest_published_version_id")
        .setDescription("The ID of the latest published version of the document.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("parent_id")
        .setDescription("The ID of the parent entity associated with the document.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("last_viewed_date")
        .setDescription("The date and time when the document was last viewed.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("last_referenced_date")
        .setDescription("The date and time when the document was last referenced.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("description")
        .setDescription("The description of the document.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("content_size")
        .setDescription("The size of the document content (in bytes).")
        .setType(Type.newBuilder().setInt(IntType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("file_type")
        .setDescription("The file type of the document (e.g., PDF, DOC).")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("file_extension")
        .setDescription("The file extension of the document (e.g., pdf, docx).")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("sharing_option")
        .setDescription("The sharing option for the document.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("sharing_privacy")
        .setDescription("The sharing privacy option for the document (e.g., Private, Public).")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("content_modified_date")
        .setDescription("The date and time when the document content was last modified.")
        .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setNullable(true)).build())
        .build(),
      ColumnDefinition
        .newBuilder()
        .setName("content_asset_id")
        .setDescription("The ID of the content asset associated with the document.")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(true)).build())
        .build())
    fixHiddenAndDynamicColumns(columns).foreach(tbl.addColumns)
    tbl.setStartupCost(1000)
    tbl.build()
  }

}
