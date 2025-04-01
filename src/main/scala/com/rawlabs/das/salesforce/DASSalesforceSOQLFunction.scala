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

import com.rawlabs.das.sdk.DASSdkInvalidArgumentException
import com.rawlabs.protocol.das.v1.functions.{FunctionDefinition, FunctionId, ParameterDefinition}
import com.rawlabs.protocol.das.v1.types._
import com.typesafe.scalalogging.StrictLogging

class DASSalesforceSOQLFunction(executeSOQL: String => Iterable[Map[_, _]])
    extends DASSalesforceFunction
    with StrictLogging {

  val definition: FunctionDefinition = FunctionDefinition
    .newBuilder()
    .setFunctionId(FunctionId.newBuilder().setName("soql"))
    .setDescription("Executes a SOQL query against Salesforce.")
    .addParams(
      ParameterDefinition
        .newBuilder()
        .setName("q")
        .setType(Type.newBuilder().setString(StringType.newBuilder().setNullable(false)).build())
        .build())
    .setReturnType(Type
      .newBuilder()
      .setList(ListType.newBuilder().setInnerType(Type.newBuilder().setRecord(RecordType.newBuilder()))))
    .build()

  override def execute(args: Map[String, Value]): Value = {
    args.get("q") match {
      case Some(value) =>
        if (value.hasString) {
          val soql = value.getString.getV
          val items = for (map <- executeSOQL(soql)) yield {
            val builder = ValueRecord.newBuilder()
            map.foreach {
              case (k: String, v) =>
                val attrValue = anyToValue(v)
                builder.addAtts(ValueRecordAttr.newBuilder().setName(k).setValue(attrValue))
              case kv => logger.warn(s"Unsupported row item: ${kv.getClass}")
            }
            Value.newBuilder().setRecord(builder.build()).build()
          }
          val result = ValueList.newBuilder()
          items.foreach(result.addValues)
          Value.newBuilder().setList(result.build()).build()
        } else {
          throw new DASSdkInvalidArgumentException("Invalid parameter type for 'q'")
        }
      case None => throw new DASSdkInvalidArgumentException("Missing required parameter 'q'")
    }
  }

  private def anyToValue(value: Any): Value = {
    value match {
      case null       => Value.newBuilder().setNull(ValueNull.newBuilder()).build()
      case v: String  => Value.newBuilder().setString(ValueString.newBuilder().setV(v)).build()
      case v: Int     => Value.newBuilder().setInt(ValueInt.newBuilder().setV(v)).build()
      case v: Long    => Value.newBuilder().setLong(ValueLong.newBuilder().setV(v)).build()
      case v: Double  => Value.newBuilder().setDouble(ValueDouble.newBuilder().setV(v)).build()
      case v: Boolean => Value.newBuilder().setBool(ValueBool.newBuilder().setV(v)).build()
      case v: Map[_, _] =>
        val builder = ValueRecord.newBuilder()
        v.foreach {
          case (k: String, v) =>
            val attrValue = anyToValue(v)
            builder.addAtts(ValueRecordAttr.newBuilder().setName(k).setValue(attrValue))
          case _ => logger.warn(s"Unsupported key type: ${v.getClass}")
        }
        Value.newBuilder().setRecord(builder.build()).build()
      case v: Iterable[_] =>
        val builder = ValueList.newBuilder()
        v.foreach { item =>
          val attrValue = anyToValue(item)
          builder.addValues(attrValue)
        }
        Value.newBuilder().setList(builder.build()).build()
      case _ => throw new DASSdkInvalidArgumentException(s"Unsupported value type: ${value.getClass}")
    }
  }
}
