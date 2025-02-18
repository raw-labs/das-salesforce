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

import com.rawlabs.protocol.das.v1.tables.{TableDefinition, TableId}

class DASSalesforceDynamicTable(connector: DASSalesforceConnector, objectName: String)
    extends DASSalesforceTable(
      connector,
      s"salesforce_${DASSalesforceUtils.renameFromSalesforce(objectName)}",
      objectName) {

  override def tableDefinition: TableDefinition = {
    val tbl = TableDefinition
      .newBuilder()
      .setTableId(TableId.newBuilder().setName(tableName))
      .setDescription(s"Custom object: $objectName.")
    val columns = readColumnsFromTable()
    columns.foreach { col =>
      val definition = col.columnDefinition
      if (col.updatable) markUpdatable(col.columnDefinition.getName)
      if (col.createable) markCreatable(col.columnDefinition.getName)
      tbl.addColumns(definition)
    }
    tbl.build()
  }

}
