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

import com.force.api.ApiException
import com.rawlabs.das.sdk.scala.{DASFunction, DASSdk, DASTable}
import com.rawlabs.protocol.das.v1.functions.FunctionDefinition
import com.rawlabs.protocol.das.v1.tables.TableDefinition
import com.typesafe.scalalogging.StrictLogging

class DASSalesforce(options: Map[String, String]) extends DASSdk with StrictLogging {

  private val connector = new DASSalesforceConnector(options)

  private val accountContactRoleTable = new DASSalesforceAccountContactRoleTable(connector)
  private val accountTable = new DASSalesforceAccountTable(connector)
  private val assetTable = new DASSalesforceAsset(connector)
  private val contactTable = new DASSalesforceContactTable(connector)
  private val contractTable = new DASSalesforceContractTable(connector)
  private val leadTable = new DASSalesforceLeadTable(connector)
  private val objectPermissionTable = new DASSalesforceObjectPermissionTable(connector)
  private val opportunityContactRoleTable = new DASSalesforceOpportunityContactRoleTable(connector)
  private val opportunityTable = new DASSalesforceOpportunityTable(connector)
  private val orderTable = new DASSalesforceOrderTable(connector)
  private val permissionSetAssignmentTable = new DASSalesforcePermissionSetAssignmentTable(connector)
  private val permissionSetTable = new DASSalesforcePermissionSetTable(connector)
  private val pricebookTable = new DASSalesforcePricebookTable(connector)
  private val productTable = new DASSalesforceProductTable(connector)
  private val userTable = new DASSalesforceUserTable(connector)
  private val taskTable = new DASSalesforceTaskTable(connector)
  private val opportunityLineItemTable = new DASSalesforceOpportunityLineItemTable(connector)
  private val eventTable = new DASSalesforceEventTable(connector)
  private val calendarTable = new DASSalesforceCalendarTable(connector)
  private val caseTable = new DASSalesforceCaseTable(connector)
  private val contentDocumentTable = new DASSalesforceContentDocumentTable(connector)
  private val contentDocumentLinkTable = new DASSalesforceContentDocumentLinkTable(connector)
  private val contentVersionTable = new DASSalesforceContentVersionTable(connector)
  private val staticTables = Seq(
    accountContactRoleTable,
    accountTable,
    assetTable,
    contactTable,
    contractTable,
    leadTable,
    objectPermissionTable,
    opportunityContactRoleTable,
    opportunityTable,
    orderTable,
    permissionSetAssignmentTable,
    permissionSetTable,
    pricebookTable,
    productTable,
    userTable,
    taskTable,
    opportunityLineItemTable,
    eventTable,
    calendarTable,
    caseTable,
    contentDocumentTable,
    contentDocumentLinkTable,
    contentVersionTable)

  private val dynamicTableNames = {
    options.get("dynamic_objects") match {
      case Some(objectNames) =>
        val objs = objectNames.strip()
        if (objs.isEmpty) {
          Seq.empty
        } else {
          objs.split(",").map(_.strip).filter(_.nonEmpty).toSeq
        }
      case None => Seq.empty
    }
  }

  logger.debug(s"Dynamic tables: $dynamicTableNames")

  private val maybeDatedConversionRateTable: Option[DASSalesforceDatedConversionRateTable] = {
    try {
      val description = connector.forceApi.describeSObject("DatedConversionRate")
      logger.info(s"Found DatedConversionRate (${description.getName})")
      Some(new DASSalesforceDatedConversionRateTable(connector))
    } catch {
      case e: ApiException =>
        logger.warn("DatedConversionRate not found", e)
        None
    }
  }

  private val dynamicTables = dynamicTableNames.map(name => new DASSalesforceDynamicTable(connector, name))

  private val allTables = staticTables ++ dynamicTables ++ maybeDatedConversionRateTable

  // These are the table definitions that will be returned to the client. It's stored
  // in a val to avoid recalculating it every time it's accessed. We do that because we
  // don't expect the tables to change during the lifetime of the DAS instance.
  private val definitions = allTables.map(_.tableDefinition)

  override def tableDefinitions: Seq[TableDefinition] = definitions

  override def functionDefinitions: Seq[FunctionDefinition] = Seq.empty

  override def getTable(name: String): Option[DASTable] = allTables.find(_.tableName == name)

  override def getFunction(name: String): Option[DASFunction] = None

}
