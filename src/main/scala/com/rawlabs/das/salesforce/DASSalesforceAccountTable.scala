/**
 * Copyright 2024 RAW Labs S.A.
 * All rights reserved.
 *
 * This source code is the property of RAW Labs S.A. It contains
 * proprietary and confidential information that is protected by applicable
 * intellectual property and other laws. Unauthorized use, reproduction,
 * or distribution of this code, or any portion of it, may result in severe
 * civil and criminal penalties and will be prosecuted to the maximum
 * extent possible under the law.
 */

package com.rawlabs.das.salesforce

import com.rawlabs.protocol.das.{ColumnDefinition, TableDefinition, TableId}
import com.rawlabs.protocol.raw.{BoolType, DoubleType, RecordType, StringType, TimestampType, Type}

class DASSalesforceAccountTable(connector: DASSalesforceConnector) extends DASSalesforceTable(connector, "salesforce_account", "Account") {

  // TODO (msb): Add helper methods to create the table definition
  // TODO (msb): Add dynamic columns based on the Salesforce schema
  override def tableDefinition: TableDefinition = {
    TableDefinition
      .newBuilder()
      .setTableId(TableId.newBuilder().setName(tableName))
      .setDescription(
        "Represents an individual account, which is an organization or person involved with business (such as customers, competitors, and partners)."
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("id")
          .setDescription("Unique identifier of the account in Salesforce.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("name")
          .setDescription("Name of the account.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("annual_revenue")
          .setDescription("Estimated annual revenue of the account.")
          .setType(Type.newBuilder().setDouble(DoubleType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("industry")
          .setDescription("Primary business of account.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("owner_id")
          .setDescription(
            "The ID of the user who currently owns this account. Default value is the user logged in to the API to perform the create."
          )
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("type")
          .setDescription("Type of account, for example, Customer, Competitor, or Partner.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("account_source")
          .setDescription("The source of the account record. For example, Advertisement, Data.com, or Trade Show.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("clean_status")
          .setDescription(
            "Indicates the record's clean status as compared with Data.com. Values are: Matched, Different,Acknowledged,NotFound,Inactive,Pending, SelectMatch, or Skipped."
          )
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("created_by_id")
          .setDescription("The id of the user who created the account.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("created_date")
          .setDescription("The creation date and time of the account.")
          .setType(
            Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
          )
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("description")
          .setDescription("Text description of the account.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("is_deleted")
          .setDescription("Indicates whether the object has been moved to the Recycle Bin (true) or not (false).")
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("last_modified_by_id")
          .setDescription(
            "The id of the user who last changed the contact fields, including modification date and time."
          )
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("last_modified_date")
          .setDescription("The date and time of last modification to account.")
          .setType(
            Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
          )
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("number_of_employees")
          .setDescription("Number of employees working at the company represented by this account.")
          .setType(Type.newBuilder().setDouble(DoubleType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("ownership")
          .setDescription("Ownership type for the account, for example Private, Public, or Subsidiary.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("phone")
          .setDescription("The contact's primary phone number.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("rating")
          .setDescription("The account's prospect rating, for example Hot, Warm, or Cold.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("sic")
          .setDescription(
            "Standard Industrial Classification code of the company's main business categorization, for example, 57340 for Electronics."
          )
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("ticker_symbol")
          .setDescription("The stock market symbol for this account.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("tradestyle")
          .setDescription(
            "A name, different from its legal name, that an org may use for conducting business. Similar to “Doing business as” or \"DBA\"."
          )
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("website")
          .setDescription("The website of this account, for example, www.acme.com.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("billing_address")
          .setDescription("The billing address of the account.")
          .setType(Type.newBuilder().setRecord(RecordType.newBuilder()).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("shipping_address")
          .setDescription("The shipping address of the account.")
          .setType(Type.newBuilder().setRecord(RecordType.newBuilder()).build())
          .build()
      )
      .setStartupCost(1000)
      .build()
  }

  override protected val fieldsCannotBeUpdated: Seq[String] = Seq(
    "id",
    "last_modified_date",
    "billing_address",
    "created_by_id",
    "is_deleted",
    "shipping_address",
    "created_date",
    "last_modified_by_id"
  )

}
