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

import com.rawlabs.das.sdk._
import com.rawlabs.protocol.das._
import com.typesafe.scalalogging.StrictLogging

import scala.collection.JavaConverters._

class DASSalesforce(options: Map[String, String]) extends DASSdk with StrictLogging {

  private val connector = new DASSalesforceConnector(options)

  private val accountTable = new DASSalesforceAccountTable(connector)
  private val staticTables = Seq(accountTable)

  private val dynamicTableNames =
    options.get("dynamic_objects").map(_.split(",").toSeq).getOrElse(Seq.empty) ++ Seq("Opportunity")

  logger.debug(s"Dynamic tables: $dynamicTableNames")
  connector.forceApi.describeGlobal().getSObjects.asScala.foreach(sObject => logger.debug(sObject.getName))

  private val dynamicTables = dynamicTableNames.map(name => new DASSalesforceDynamicTable(connector, name))

  private val allTables = staticTables ++ dynamicTables

  override def tableDefinitions: Seq[TableDefinition] = allTables.map(_.tableDefinition)

  override def functionDefinitions: Seq[FunctionDefinition] = Seq.empty

  override def getTable(name: String): Option[DASTable] = allTables.find(_.tableName == name)

  override def getFunction(name: String): Option[DASFunction] = None

}
