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
