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

import com.rawlabs.protocol.raw.{
  Value,
  ValueBool,
  ValueDouble,
  ValueInt,
  ValueNull,
  ValueRecord,
  ValueRecordField,
  ValueString
}
import com.typesafe.scalalogging.StrictLogging

object DASSalesforceUtils extends StrictLogging {

  def soqlValueToRawValue(v: Any): Value = {
    v match {
      case null => Value.newBuilder().setNull(ValueNull.newBuilder()).build()
      case s: String => Value.newBuilder().setString(ValueString.newBuilder().setV(s).build()).build()
      case i: Int => Value.newBuilder().setInt(ValueInt.newBuilder().setV(i).build()).build()
      case d: Double => Value.newBuilder().setDouble(ValueDouble.newBuilder().setV(d).build()).build()
      case b: Boolean => Value.newBuilder().setBool(ValueBool.newBuilder().setV(b).build()).build()
      case m: Map[_, _] =>
        val record = ValueRecord.newBuilder()
        m.foreach {
          case (k: String, v) =>
            record.addFields(ValueRecordField.newBuilder().setName(k).setValue(soqlValueToRawValue(v)).build())
        }
        Value.newBuilder().setRecord(record.build()).build()
      case t =>
        logger.error(s"Unsupported type: ${t.getClass} (type = ${t.getClass})")
        throw new IllegalArgumentException(s"Unsupported type: ${t.getClass}")
    }
  }

  def rawValueToSOQLValue(v: Value): String = {
    if (v.hasInt) {
      v.getInt.getV.toString
    } else if (v.hasLong) {
      v.getLong.getV.toString
    } else if (v.hasDouble) {
      v.getDouble.getV.toString
    } else if (v.hasDecimal) {
      v.getDecimal.getV.toString
    } else if (v.hasString) {
      // Quote string safely for ', including escaping quotes
      "'" + v.getString.getV.replace("'", "\\'") + "'"
    } else if (v.hasBool) {
      v.getBool.getV.toString
    } else if (v.hasNull) {
      "NULL"
    } else if (v.hasTime) {
      // Format time as ISO 8601
      val hour = v.getTime.getHour
      val minute = v.getTime.getMinute
      val second = v.getTime.getSecond
      val nano = v.getTime.getNano
      f"$hour%02d:$minute%02d:$second%02d.$nano%09dZ"
    } else if (v.hasDate) {
      // Format date as ISO 8601
      val year = v.getDate.getYear
      val month = v.getDate.getMonth
      val day = v.getDate.getDay
      f"$year%04d-$month%02d-$day%02d"
    } else if (v.hasTimestamp) {
      // Format timestamp as ISO 8601
      val year = v.getTimestamp.getYear
      val month = v.getTimestamp.getMonth
      val day = v.getTimestamp.getDay
      val hour = v.getTimestamp.getHour
      val minute = v.getTimestamp.getMinute
      val second = v.getTimestamp.getSecond
      val nano = v.getTimestamp.getNano
      f"$year%04d-$month%02d-$day%02dT$hour%02d:$minute%02d:$second%02d.$nano%09dZ"
    } else {
      throw new IllegalArgumentException(s"Unsupported value: $v")
    }
  }

  def rawValueToJavaValue(v: Value): Any = {
    if (v.hasInt) v.getInt.getV
    else if (v.hasDouble) v.getDouble.getV
    else if (v.hasString) v.getString.getV
    else if (v.hasBool) v.getBool.getV
    else if (v.hasNull) null
    else if (v.hasTimestamp) {
      val year = v.getTimestamp.getYear
      val month = v.getTimestamp.getMonth
      val day = v.getTimestamp.getDay
      val hour = v.getTimestamp.getHour
      val minute = v.getTimestamp.getMinute
      val second = v.getTimestamp.getSecond
      val nano = v.getTimestamp.getNano
      val formatted = f"$year%04d-$month%02d-$day%02dT$hour%02d:$minute%02d:$second%02d.$nano%09dZ"
      formatted
    } else {
      throw new IllegalArgumentException(s"Unsupported value: $v")
    }
  }

  // Convert e.g. account_number to AccountNumber
  // This only works because Salesforce native fields never have underscores
  def renameToSalesforce(name: String): String = {
    if (name.endsWith("__c") || name.endsWith("__s")) name // Custom objects must be left as is
    else name.split("_").map(_.capitalize).mkString
  }

  // Convert e.g. Price2Book to price_2_book
  // This only works because Salesforce native fields never have underscores
  def renameFromSalesforce(name: String): String = {
    if (name.endsWith("__c") || name.endsWith("__s")) name // Custom objects must be left as is
    else {
      val parts = name.split("(?=[A-Z0-9])")
      parts.mkString("_").toLowerCase
    }
  }

}
