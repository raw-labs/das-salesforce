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

import net.sf.jsqlparser.expression.operators.relational._
import net.sf.jsqlparser.expression._
import net.sf.jsqlparser.expression.operators.arithmetic._
import net.sf.jsqlparser.expression.operators.conditional._
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.schema.{Column, Table}
import net.sf.jsqlparser.statement.Statement
import net.sf.jsqlparser.statement.select._

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

// Represents the type of JOIN operation
sealed trait DASJoinType
object DASJoinType {
  case object Inner extends DASJoinType
  case object Left extends DASJoinType
  case object Right extends DASJoinType
  case object Full extends DASJoinType
  // Add more join types as needed
}

// Represents the direction for ORDER BY
sealed trait DASOrderDirection
object DASOrderDirection {
  case object Ascending extends DASOrderDirection
  case object Descending extends DASOrderDirection
}

// Represents a table with an optional alias
case class DASSQLTable(name: String, alias: Option[String] = None)

// Represents a column with an optional table reference and alias
case class DASColumn(
    table: Option[String] = None, // Table name or alias
    name: String
) extends DASExpr

// Represents an expression in WHERE, JOIN conditions, etc.
// Enumeration for supported unary operators
sealed trait DASUnaryOperator
object DASUnaryOperator {
  case object Not extends DASUnaryOperator
  case object Negate extends DASUnaryOperator // Represents "-"
  case object Plus extends DASUnaryOperator // Represents "+"
  // Add more unary operators as needed
}

// Enumeration for supported binary operators
sealed trait DASBinaryOperator
object DASBinaryOperator {
  case object Equals extends DASBinaryOperator // "="
  case object NotEquals extends DASBinaryOperator // "!=" or "<>"
  case object GreaterThan extends DASBinaryOperator // ">"
  case object LessThan extends DASBinaryOperator // "<"
  case object GreaterThanOrEqual extends DASBinaryOperator // ">="
  case object LessThanOrEqual extends DASBinaryOperator // "<="
  case object And extends DASBinaryOperator // "AND"
  case object Or extends DASBinaryOperator // "OR"
  case object Add extends DASBinaryOperator // "+"
  case object Subtract extends DASBinaryOperator // "-"
  case object Multiply extends DASBinaryOperator // "*"
  case object Divide extends DASBinaryOperator // "/"
  // Add more binary operators as needed
}

// Represents any SQL expression
sealed trait DASExpr

case class DASLongValue(value: Long) extends DASExpr

case class DASStringValue(value: String) extends DASExpr

// Represents an identifier, optionally qualified by a table or alias
case class DASIdentifier(table: Option[String] = None, name: String) extends DASExpr

// Represents a unary operation (e.g., NOT, -, +)
case class DASUnaryOp(operator: DASUnaryOperator, expr: DASExpr) extends DASExpr

// Represents a binary operation (e.g., a + b, a = b)
case class DASBinaryOp(left: DASExpr, operator: DASBinaryOperator, right: DASExpr) extends DASExpr

// Represents a function call (e.g., COUNT(b), LOWER(name))
case class DASFunctionCall(name: String, args: List[DASExpr] = List.empty) extends DASExpr

// Represents a JOIN operation
case class DASJoin(
    joinType: DASJoinType,
    table: DASSQLTable,
    condition: DASExpr
)

// Represents the FROM clause with base table and joins
case class DASFrom(
    baseTable: DASSQLTable,
    joins: List[DASJoin] = List.empty
)

// Represents the WHERE clause
case class DASWhere(
    condition: DASExpr
)

// Represents the GROUP BY clause
case class DASGroupBy(
    columns: List[DASColumn]
)

// Represents the HAVING clause
case class DASHaving(
    condition: DASExpr
)

// Represents a SELECT item, which can be a column or an expression
sealed trait DASSelectItem
case class DASSelectColumn(column: DASColumn) extends DASSelectItem
case class DASSelectExpression(expression: DASExpr, alias: Option[String] = None) extends DASSelectItem

// Represents the SELECT clause
case class DASSelect(
    items: List[DASSelectItem]
)

// Represents the DISTINCT clause
case class DASDistinct(
    isDistinct: Boolean
)

// Represents an ORDER BY item
case class DASOrderByItem(
    column: DASColumn,
    direction: DASOrderDirection
)

// Represents the ORDER BY clause
case class DASOrderBy(
    items: List[DASOrderByItem]
)

// Represents the LIMIT clause
case class DASLimit(
    count: Int
)

// Represents the OFFSET clause
case class DASOffset(
    count: Int
)

// Represents a complete SQL query
case class DASSQLQuery(
    from: DASFrom,
    where: Option[DASWhere] = None,
    groupBy: Option[DASGroupBy] = None,
    having: Option[DASHaving] = None,
    select: DASSelect,
    distinct: Option[DASDistinct] = None,
    orderBy: Option[DASOrderBy] = None,
    limit: Option[DASLimit] = None,
    offset: Option[DASOffset] = None
)

object DASSQLParser {

  // Exception for unsupported features
  case class UnsupportedFeatureException(message: String) extends Exception(message)

  // Main parse method
  def parseSQL(sql: String): Either[String, DASSQLQuery] = {
    Try {
      val statement: Statement = CCJSqlParserUtil.parse(sql)

      statement match {
        case plainSelect: PlainSelect => mapPlainSelect(plainSelect)
        case _ => throw UnsupportedFeatureException("Only plain SELECT statements are supported.")
      }
    } match {
      case Success(query) => Right(query)
      case Failure(ex: UnsupportedFeatureException) => Left(s"Unsupported feature: ${ex.getMessage}")
      case Failure(ex) => Left(s"Failed to parse SQL: ${ex.getMessage}")
    }
  }

  // Map PlainSelect to SQLQuery
  private def mapPlainSelect(ps: PlainSelect): DASSQLQuery = {
    // FROM clause
    val from = mapFrom(ps.getFromItem, ps.getJoins)

    // WHERE clause
    val where = Option(ps.getWhere).map(mapExpression).map(DASWhere)

    // GROUP BY clause
    val groupBy = Option(ps.getGroupBy).map(ge => mapGroupBy(ge, ps.getSelectItems.asScala))

    // HAVING clause
    val having = Option(ps.getHaving).map(mapExpression).map(DASHaving)

    // SELECT clause
    val select = mapSelect(ps)

    // DISTINCT clause
    val distinct = if (ps.getDistinct != null) Some(DASDistinct(isDistinct = true)) else None

    // ORDER BY clause
    val orderBy = Option(ps.getOrderByElements).map { obeList =>
      DASOrderBy(obeList.asScala.map(mapOrderByItem).toList)
    }

    // LIMIT clause
    val limit = Option(ps.getLimit).map { lim =>
      val rowCount = lim.getRowCount
      if (rowCount != null) {
        DASLimit(rowCount.asInstanceOf[LongValue].getValue.toInt)
      } else {
        throw UnsupportedFeatureException("LIMIT without row count is not supported.")
      }
    }

    // OFFSET clause
    val offset = Option(ps.getOffset).map { off =>
      val offsetValue = off.getOffset
      if (offsetValue != null) {
        DASOffset(offsetValue.asInstanceOf[LongValue].getValue.toInt)
      } else {
        throw UnsupportedFeatureException("OFFSET without value is not supported.")
      }
    }

    DASSQLQuery(
      from = from,
      where = where,
      groupBy = groupBy,
      having = having,
      select = select,
      distinct = distinct,
      orderBy = orderBy,
      limit = limit,
      offset = offset
    )
  }

  private def mapGroupBy(gb: GroupByElement, pes: Seq[SelectItem[_]]): DASGroupBy = {
    DASGroupBy(gb.getGroupByExpressionList.asScala.map {
      case l: LongValue => mapColumn(pes(l.getValue.toInt - 1).asInstanceOf[SelectItem[Expression]].getExpression)
      case col: Column =>
        val table = Option(col.getTable).filter(_.getName != null).map(_.getName)
        DASColumn(table = table, name = col.getColumnName)
    }.toList)
  }

  // Map FROM and JOINs
  private def mapFrom(fromItem: FromItem, joins: java.util.List[Join]): DASFrom = {
    // Handle only Table types in FROM
    fromItem match {
      case table: Table =>
        val baseTable = mapTable(table)
        // Handle joins
        val joinList =
          if (joins != null) {
            joins.asScala.toList.map(j => mapJoin(j))
          } else {
            List.empty
          }
        DASFrom(baseTable, joinList)
      case parenthesedFromItem: ParenthesedFromItem =>
        mapFrom(parenthesedFromItem.getFromItem, parenthesedFromItem.getJoins)
      case _ => throw UnsupportedFeatureException(s"Unsupported FROM item: ${fromItem.getClass.getSimpleName}")
    }
  }

  // Map a single JOIN
  private def mapJoin(j: Join): DASJoin = {
    // Determine join type
    val joinType =
      if (j.isInner()) {
        DASJoinType.Inner
      } else if (j.isLeft()) {
        DASJoinType.Left
      } else if (j.isRight()) {
        DASJoinType.Right
      } else if (j.isFull()) {
        DASJoinType.Full
      } else {
        throw UnsupportedFeatureException(s"Unsupported join type: ${j.toString}")
      }

    // Get the joined table
    val rightItem = j.getRightItem
    val joinedTable = rightItem match {
      case table: Table => mapTable(table)
      case _ => throw UnsupportedFeatureException(s"Unsupported JOIN item: ${rightItem.getClass.getSimpleName}")
    }

    // Get the join condition
    val onExpression = j.getOnExpressions.iterator().next()
    if (onExpression == null) {
      throw UnsupportedFeatureException("Only JOINs with ON expressions are supported.")
    }
    val condition = mapExpression(onExpression)

    DASJoin(
      joinType = joinType,
      table = joinedTable,
      condition = condition
    )
  }

  // Map a JSQLParser Table to our Table case class
  private def mapTable(t: Table): DASSQLTable = {
    val alias = Option(t.getAlias).map(_.getName)
    DASSQLTable(name = t.getName, alias = alias)
  }

  // Map SELECT clause
  private def mapSelect(ps: PlainSelect): DASSelect = {
    val selectItems = ps.getSelectItems.asScala.toList.map {
      case si: SelectItem[_] => mapSelectItem(si.asInstanceOf[SelectItem[Expression]])
      case _ => throw UnsupportedFeatureException("Only expression select items are supported.")
    }
    DASSelect(items = selectItems)
  }

  // Map a single SelectItem
  private def mapSelectItem(si: SelectItem[Expression]): DASSelectItem = {
    val expr = mapExpression(si.getExpression)
    val alias = Option(si.getAlias).map(_.getName)
    expr match {
      case col: DASColumn => DASSelectColumn(col)
      case _ => DASSelectExpression(expression = expr, alias = alias)
    }
  }

  // Map a JSQLParser Expression to our Expression
  private def mapExpression(expr: Expression): DASExpr = {
    expr match {
      case e: ParenthesedExpressionList[_] =>
        if (e.size() != 1) {
          throw UnsupportedFeatureException("Only single expressions are supported in parentheses.")
        }
        mapExpression(e.getFirst)

      case c: Column => mapColumn(c)

      case be: BinaryExpression => mapBinaryExpression(be)

      case f: Function => mapFunction(f)

      case l: LongValue => DASLongValue(l.getValue)

      case s: StringValue => DASStringValue(s.getValue)

      case e: NotExpression =>
        val op = DASUnaryOperator.Not
        val operand = mapExpression(e.getExpression)
        DASUnaryOp(operator = op, expr = operand)

      case se: SignedExpression =>
        val op =
          if (se.getSign == '+') {
            DASUnaryOperator.Plus
          } else if (se.getSign == '-') {
            DASUnaryOperator.Negate
          } else {
            throw UnsupportedFeatureException(s"Unsupported signed expression: ${se.getSign}")
          }
        val operand = mapExpression(se.getExpression)
        DASUnaryOp(operator = op, expr = operand)

      case _ => throw UnsupportedFeatureException(s"Unsupported expression type: ${expr.getClass.getSimpleName}")
    }
  }

  // Map a JSQLParser Column to our Column case class
  private def mapColumn(e: Expression): DASColumn = {
    e match {
      case col: Column =>
        val table = Option(col.getTable).filter(_.getName != null).map(_.getName)
        DASColumn(table = table, name = col.getColumnName)
      case _ => throw UnsupportedFeatureException(s"Only columns are supported: ${e.getClass.getSimpleName}")
    }
  }

  // Map a JSQLParser BinaryExpression to our BinaryOp
  private def mapBinaryExpression(be: BinaryExpression): DASBinaryOp = {
    val operator = be match {
      case _: EqualsTo => DASBinaryOperator.Equals
      case _: NotEqualsTo => DASBinaryOperator.NotEquals
      case _: GreaterThan => DASBinaryOperator.GreaterThan
      case _: GreaterThanEquals => DASBinaryOperator.GreaterThanOrEqual
      case _: MinorThan => DASBinaryOperator.LessThan
      case _: MinorThanEquals => DASBinaryOperator.LessThanOrEqual
      case _: AndExpression => DASBinaryOperator.And
      case _: OrExpression => DASBinaryOperator.Or
      case _: Addition => DASBinaryOperator.Add
      case _: Subtraction => DASBinaryOperator.Subtract
      case _: Multiplication => DASBinaryOperator.Multiply
      case _: Division => DASBinaryOperator.Divide
      case _ => throw UnsupportedFeatureException(s"Unsupported binary operator: ${be.getClass.getSimpleName}")
    }

    val left = mapExpression(be.getLeftExpression)
    val right = mapExpression(be.getRightExpression)

    DASBinaryOp(left = left, operator = operator, right = right)
  }

  // Map a JSQLParser Function to our FunctionCall
  private def mapFunction(f: Function): DASFunctionCall = {
    val name = f.getName.toUpperCase // Normalize function name
    val params = f.getParameters

    val args =
      if (params != null && params != null) {
        params.asScala.toList.map { case e: Expression => mapExpression(e) }
      } else {
        List.empty
      }

    DASFunctionCall(name = name, args = args)
  }

  // Map ORDER BY item
  private def mapOrderByItem(obe: OrderByElement): DASOrderByItem = {
    val expr = obe.getExpression
    expr match {
      case col: Column =>
        val column = mapColumn(col)
        val direction = if (obe.isAsc) DASOrderDirection.Ascending else DASOrderDirection.Descending
        DASOrderByItem(column = column, direction = direction)
      case _ => throw UnsupportedFeatureException("Only columns are supported in ORDER BY.")
    }
  }

//  // Map ORDER BY expression to Column
//  private def mapOrderByColumn(expr: Expression): DASColumn = {
//    expr match {
//      case col: net.sf.jsqlparser.schema.Column => mapColumn(col)
//      case _ => throw UnsupportedFeatureException("Only columns are supported in ORDER BY.")
//    }
//  }

}

object Test extends App {
//  val sql = "SELECT a, b, c FROM table1 WHERE a = 1 AND b = 2"
//  val sql =
//    """SELECT r1."id", r2."id" FROM ("people1" r1 INNER JOIN "people2" r2 ON (((r1."id" = r2."id"))))""".stripMargin
//. val sql = """SELECT r1."id", max(r2."id") FROM ("people1" r1 INNER JOIN "people2" r2 ON (((r1."id" = r2."id")))) GROUP BY 1""".stripMargin
//  val sql =
//    """SELECT max(r2."id"), r1."id" FROM ("people1" r1 INNER JOIN "people2" r2 ON (((r1."id" = r2."id")))) GROUP BY 2""".stripMargin
  val sql =
    """SELECT count("id"), count("is_deleted") FROM "salesforce_account" WHERE (("created_date" >= '2024-01-01 12:13:14')) LIMIT 10 OFFSET 4""".stripMargin
  val result = DASSQLParser.parseSQL(sql)
  println(result)
}
