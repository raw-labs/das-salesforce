package com.rawlabs.das.salesforce

import com.rawlabs.protocol.das.{ColumnDefinition, TableDefinition, TableId}
import com.rawlabs.protocol.raw.{BoolType, StringType, TimestampType, Type}

class DASSalesforceAccountContactRoleTable(connector: DASSalesforceConnector)
    extends DASSalesforceTable(connector, "salesforce_account_contact_role", "AccountContactRole") {

  // TODO (msb): Add helper methods to create the table definition
  // TODO (msb): Add dynamic columns based on the Salesforce schema
  override def tableDefinition: TableDefinition = {
    TableDefinition
      .newBuilder()
      .setTableId(TableId.newBuilder().setName(tableName))
      .setDescription(
        "Represents the role that a Contact plays on an Account."
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("id")
          .setDescription("Unique identifier of the account contact role in Salesforce.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("account_id")
          .setDescription("Unique identifier of the account contact role in Salesforce.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("contact_id")
          .setDescription("ID of the Contact associated with this account.")
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("is_primary")
          .setDescription(
            "Specifies whether the Contact plays the primary role on the Account (true) or not (false). Note that each account has only one primary contact role."
          )
          .setType(Type.newBuilder().setBool(BoolType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("role")
          .setDescription(
            "Name of the role played by the Contact on this Account, such as Decision Maker, Approver, Buyer, and so on."
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
          .setDescription("The creation date and time of the account contact role.")
          .setType(
            Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
          )
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("last_modified_by_id")
          .setDescription(
            "Id of the user who most recently changed the contact role record."
          )
          .setType(Type.newBuilder().setString(StringType.newBuilder().setTriable(false).setNullable(true)).build())
          .build()
      )
      .addColumns(
        ColumnDefinition
          .newBuilder()
          .setName("last_modified_date")
          .setDescription("Date of most recent change in the contact role record.")
          .setType(
            Type.newBuilder().setTimestamp(TimestampType.newBuilder().setTriable(false).setNullable(true)).build()
          )
          .build()
      )
      .setStartupCost(1000)
      .build()
  }

  override protected val fieldsCannotBeUpdated: Seq[String] = Seq(
    "id",
    "account_id",
    "contact_id",
    "created_by_id",
    "created_date",
    "last_modified_by_id",
    "last_modified_date"
  )

}
