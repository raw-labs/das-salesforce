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
import com.rawlabs.protocol.raw.{BoolType, StringType, TimestampType, IntType, Type}

class DASSalesforceContentVersionTable(connector: DASSalesforceConnector)
  extends DASSalesforceTable(connector, "salesforce_content_version", "ContentVersion") {

  override def tableDefinition: TableDefinition = {
    var tbl = TableDefinition
      .newBuilder()
      .setTableId(TableId.newBuilder().setName(tableName))
      .setDescription(
        "Represents a specific version of a Salesforce CRM Content document or Salesforce File."
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("id")
          .setDescription("Unique identifier for the content version.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("content_document_id")
          .setDescription("The ID of the related content document.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("is_latest")
          .setDescription("Indicates if this is the latest version of the content (true/false).")
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("content_url")
          .setDescription("The URL of the content.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("content_body_id")
          .setDescription("The ID of the content body.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("version_number")
          .setDescription("The version number of the content.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("title")
          .setDescription("The title of the content version.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("description")
          .setDescription("The description of the content version.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("reason_for_change")
          .setDescription("The reason for the content version change.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("sharing_option")
          .setDescription("Sharing option for the content.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("sharing_privacy")
          .setDescription("Sharing privacy for the content.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("path_on_client")
          .setDescription("The original path on the client's system for the content.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("rating_count")
          .setDescription("Number of ratings for the content.")
          .setType(Type.newBuilder().setInt(IntType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("is_deleted")
          .setDescription("Indicates whether the content version is deleted (true/false).")
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("content_modified_date")
          .setDescription("The date and time when the content was last modified.")
          .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("content_modified_by_id")
          .setDescription("The ID of the user who last modified the content.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("positive_rating_count")
          .setDescription("Number of positive ratings for the content.")
          .setType(Type.newBuilder().setInt(IntType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("negative_rating_count")
          .setDescription("Number of negative ratings for the content.")
          .setType(Type.newBuilder().setInt(IntType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("featured_content_boost")
          .setDescription("The featured content boost score.")
          .setType(Type.newBuilder().setInt(IntType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("featured_content_date")
          .setDescription("The date when the content was featured.")
          .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("owner_id")
          .setDescription("The ID of the owner of the content version.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("created_by_id")
          .setDescription("The ID of the user who created the content version.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("created_date")
          .setDescription("The date and time the content version was created.")
          .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("last_modified_by_id")
          .setDescription("The ID of the user who last modified the content version.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("last_modified_date")
          .setDescription("The date and time the content version was last modified.")
          .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("system_modstamp")
          .setDescription("The system modstamp for the content version.")
          .setType(Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("tag_csv")
          .setDescription("Comma-separated list of tags for the content version.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("file_type")
          .setDescription("The file type of the content version (e.g., PDF, DOC).")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("publish_status")
          .setDescription("The publish status of the content version (e.g., Published, Draft).")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("version_data")
          .setDescription("The base64-encoded version data of the content.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("content_size")
          .setDescription("The size of the content version in bytes.")
          .setType(Type.newBuilder().setInt(IntType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("file_extension")
          .setDescription("The file extension of the content version (e.g., pdf, docx).")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("first_publish_location_id")
          .setDescription("The ID of the location where the content version was first published.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("origin")
          .setDescription("The origin of the content (e.g., H, L).")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("content_location")
          .setDescription("The location of the content (e.g., S, E).")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(false)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("text_preview")
          .setDescription("Text preview for the content version.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("external_document_info_1")
          .setDescription("First external document information field.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("external_document_info_2")
          .setDescription("Second external document information field.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("external_data_source_id")
          .setDescription("The ID of the external data source.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("checksum")
          .setDescription("Checksum for the content version.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("is_major_version")
          .setDescription("Indicates if this is a major version (true/false).")
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("is_asset_enabled")
          .setDescription("Indicates if the content version is asset-enabled (true/false).")
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .setStartupCost(1000)

    tbl = addDynamicColumns(tbl)
    tbl.build()
  }

}
