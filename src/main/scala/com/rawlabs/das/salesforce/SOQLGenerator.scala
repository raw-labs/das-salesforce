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

import com.rawlabs.das.salesforce.DASSalesforceUtils.renameToSalesforce

object SOQLGenerator {

  // Function to generate SOQL from DASSQLQuery
  def generateSOQL(query: DASSQLQuery): String = {
    val selectClause = generateSelectClause(query.select, query.distinct)
    val fromClause = generateFromClause(query.from)
    val whereClause = query.where.map(generateWhereClause).getOrElse("")
    // SOQL supports ORDER BY and GROUP BY, but based on user comment, they are omitted
    val limitClause = query.limit.map(l => s" LIMIT ${l.count}").getOrElse("")
    val offsetClause = query.offset.map(o => s" OFFSET ${o.count}").getOrElse("")

    // Combine all parts
    List(selectClause, fromClause, whereClause, limitClause, offsetClause)
      .filter(_.nonEmpty)
      .mkString(" ")
  }

  // Generate SELECT clause
  private def generateSelectClause(select: DASSelect, distinctOpt: Option[DASDistinct]): String = {
    val distinctStr = distinctOpt.filter(_.isDistinct).map(_ => "SELECT DISTINCT").getOrElse("SELECT")
    val itemsStr = select.items
      .map {
        case DASSelectColumn(column) => generateColumn(column)
        case DASSelectExpression(expr, alias) => generateExpression(expr) + alias.map(a => s" AS $a").getOrElse("")
      }
      .mkString(", ")
    s"$distinctStr $itemsStr"
  }

  // Generate FROM clause
  private def generateFromClause(from: DASFrom): String = {
    val baseTableStr = generateTable(from.baseTable)
    val joinsStr = from.joins.map(generateJoin).mkString(" ")
    s"FROM $baseTableStr$joinsStr"
  }

  // Generate WHERE clause
  private def generateWhereClause(where: DASWhere): String = {
    s"WHERE ${generateExpression(where.condition)}"
  }

  // Generate Table with optional alias
  private def generateTable(table: DASSQLTable): String = {
    val salesforceTableName = renameToSalesforce(table.name.replace("\"", "").replace("salesforce_", ""))
    table.alias.map(a => s"${table.name} $a").getOrElse(salesforceTableName)
  }

  // Generate JOIN clause
  private def generateJoin(join: DASJoin): String = {
    val joinTypeStr = join.joinType match {
      case DASJoinType.Inner => "INNER JOIN"
      case DASJoinType.Left => "LEFT OUTER JOIN"
      case DASJoinType.Right => "RIGHT OUTER JOIN"
      case DASJoinType.Full => "FULL OUTER JOIN"
      // Add more mappings if needed
    }
    val tableStr = generateTable(join.table)
    val conditionStr = generateExpression(join.condition)
    s" $joinTypeStr $tableStr ON $conditionStr"
  }

  // Generate Column with optional table qualifier
  private def generateColumn(column: DASColumn): String = {
    column.table.map(t => s"$t.").getOrElse("") + renameToSalesforce(column.name.replace("\"", ""))
  }

  // Generate Expression recursively
  private def generateExpression(expr: DASExpr): String = expr match {
    case DASLongValue(value) => value.toString
    case DASStringValue(value) => s"'${escapeString(value)}'"
    case DASIdentifier(tableOpt, name) => tableOpt.map(t => s"$t.").getOrElse("") + name
    case DASUnaryOp(operator, expr) =>
      val opStr = operator match {
        case DASUnaryOperator.Not => "NOT "
        case DASUnaryOperator.Negate => "-"
        case DASUnaryOperator.Plus => "+"
        // Add more operators if needed
      }
      s"$opStr(${generateExpression(expr)})"
    case DASBinaryOp(left, operator, right) =>
      val opStr = operator match {
        case DASBinaryOperator.Equals => "="
        case DASBinaryOperator.NotEquals => "!="
        case DASBinaryOperator.GreaterThan => ">"
        case DASBinaryOperator.LessThan => "<"
        case DASBinaryOperator.GreaterThanOrEqual => ">="
        case DASBinaryOperator.LessThanOrEqual => "<="
        case DASBinaryOperator.And => "AND"
        case DASBinaryOperator.Or => "OR"
        case DASBinaryOperator.Add => "+"
        case DASBinaryOperator.Subtract => "-"
        case DASBinaryOperator.Multiply => "*"
        case DASBinaryOperator.Divide => "/"
        // Add more operators if needed
      }
      s"(${generateExpression(left)} $opStr ${generateExpression(right)})"
    case DASFunctionCall(name, args) =>
      val argsStr = args.map(generateExpression).mkString(", ")
      s"$name($argsStr)"
    case DASColumn(table, name) => generateColumn(DASColumn(table, name))
    // Add more cases as needed
  }

  // Helper to escape single quotes in strings
  private def escapeString(str: String): String = {
    str.replace("'", "\\'")
  }
}
