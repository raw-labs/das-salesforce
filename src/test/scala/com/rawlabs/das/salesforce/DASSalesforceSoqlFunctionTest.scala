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

import scala.jdk.CollectionConverters.IterableHasAsScala

import org.scalatest.funsuite.AnyFunSuite

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.rawlabs.das.sdk.DASSdkInvalidArgumentException
import com.rawlabs.protocol.das.v1.types.{Value, ValueInt, ValueNull, ValueString}

class DASSalesforceSoqlFunctionTest extends AnyFunSuite {

  test("Fails gracefully if q (soql query string) is absent") {
    val f = new DASSalesforceSOQLFunction(_ => Seq.empty)
    assertThrows[DASSdkInvalidArgumentException] {
      f.execute(Map.empty)
    }
    assertThrows[DASSdkInvalidArgumentException] {
      f.execute(
        Map("query" -> Value.newBuilder().setString(ValueString.newBuilder().setV("SELECT Id From Event")).build()))
    }
  }

  test("Fails gracefully if q (string) is of the wrong type") {
    val f = new DASSalesforceSOQLFunction(_ => Seq.empty)
    // Int
    assertThrows[DASSdkInvalidArgumentException] {
      f.execute(Map("q" -> Value.newBuilder().setInt(ValueInt.newBuilder().setV(14)).build()))
    }
    // Null
    assertThrows[DASSdkInvalidArgumentException] {
      f.execute(Map("q" -> Value.newBuilder().setNull(ValueNull.newBuilder()).build()))
    }
  }

  test("Successfully processes a couple of JSON rows returned by the Salesforce API") {
    // The Salesforce output is a JSON array containing two records.
    val json = jsonStringToSalesforceValue(
      """[{"Id": "1", "Name": "Jane Doe"},{"Id": "2", "Name": "John Doe"},{"Id": "3", "Name": "Joe Dohn"}]""")
    val f = new DASSalesforceSOQLFunction(_ => json)
    val result = runWith(f, "...") // soql is not used in this test
    assert(result.getList.getValuesCount == 3)
    val item1 = result.getList.getValues(0)

    assert(item1.hasRecord)
    assert(item1.getRecord.getAttsCount == 2)

    assert(item1.getRecord.getAtts(0).getName == "Id")
    assert(item1.getRecord.getAtts(0).getValue.getString.getV == "1")
    assert(item1.getRecord.getAtts(1).getName == "Name")
    assert(item1.getRecord.getAtts(1).getValue.getString.getV == "Jane Doe")

    val item2 = result.getList.getValues(1)
    assert(item2.hasRecord)
    assert(item2.getRecord.getAttsCount == 2)
    assert(item2.getRecord.getAtts(0).getName == "Id")
    assert(item2.getRecord.getAtts(0).getValue.getString.getV == "2")
    assert(item2.getRecord.getAtts(1).getName == "Name")
    assert(item2.getRecord.getAtts(1).getValue.getString.getV == "John Doe")

    val item3 = result.getList.getValues(2)
    assert(item3.hasRecord)
    assert(item3.getRecord.getAttsCount == 2)
    assert(item3.getRecord.getAtts(0).getName == "Id")
    assert(item3.getRecord.getAtts(0).getValue.getString.getV == "3")
    assert(item3.getRecord.getAtts(1).getName == "Name")
    assert(item3.getRecord.getAtts(1).getValue.getString.getV == "Joe Dohn")
  }

  test("Successfully parses an empty record") {
    // The Salesforce output is a JSON array containing one record. Just make sure it
    // doesn't throw an exception.
    val json = jsonStringToSalesforceValue("[{}]")
    val f = new DASSalesforceSOQLFunction(_ => json)
    val result = runWith(f, "SELECT * FROM table")
    assert(result.getList.getValuesCount == 1)
    val item = result.getList.getValues(0)
    assert(item.hasRecord)
    assert(item.getRecord.getAttsCount == 0)
  }

  test("Successfully parses an empty result set") {
    // The Salesforce output is a JSON array containing no records. That would happen if
    // the query returned no results.
    val f = new DASSalesforceSOQLFunction(_ => Seq.empty)
    val result = runWith(f, "SELECT * FROM table")
    assert(result.getList.getValuesCount == 0)
  }

  test("Successfully parses an integer field") {
    val json = jsonStringToSalesforceValue("""[{"Id": 1}]""")
    val f = new DASSalesforceSOQLFunction(_ => json)
    val result = runWith(f, "SELECT Id FROM table")
    assert(result.getList.getValuesCount == 1)
    val item = result.getList.getValues(0)
    assert(item.hasRecord)
    assert(item.getRecord.getAttsCount == 1)
    assert(item.getRecord.getAtts(0).getName == "Id")
    assert(item.getRecord.getAtts(0).getValue.getInt.getV == 1)
  }

  test("Successfully parses a double field") {
    val json = jsonStringToSalesforceValue("""[{"Id": 3.14}]""")
    val f = new DASSalesforceSOQLFunction(_ => json)
    val result = runWith(f, "SELECT Id FROM table")
    assert(result.getList.getValuesCount == 1)
    val item = result.getList.getValues(0)
    assert(item.hasRecord)
    assert(item.getRecord.getAttsCount == 1)
    assert(item.getRecord.getAtts(0).getName == "Id")
    assert(item.getRecord.getAtts(0).getValue.getDouble.getV == 3.14)
  }

  test("Successfully parses a boolean field") {
    val json = jsonStringToSalesforceValue("""[{"IsDeleted": true}]""")
    val f = new DASSalesforceSOQLFunction(_ => json)
    val result = runWith(f, "SELECT IsDeleted FROM table")
    assert(result.getList.getValuesCount == 1)
    val item = result.getList.getValues(0)
    assert(item.hasRecord)
    assert(item.getRecord.getAttsCount == 1)
    assert(item.getRecord.getAtts(0).getName == "IsDeleted")
    assert(item.getRecord.getAtts(0).getValue.getBool.getV == true)
  }

  test("Successfully processes an array of primitives") {
    // The Salesforce output is a JSON array containing one record,
    // where the field "Numbers" is itself an array of integers.
    val jsonNumbers = jsonStringToSalesforceValue("""[{"Numbers": [1, 2, 3]}]""")
    val f = new DASSalesforceSOQLFunction(_ => jsonNumbers)
    val result = runWith(f, "SELECT value FROM table")
    // Expect one row (record)
    assert(result.getList.getValuesCount == 1)
    val row = result.getList.getValues(0)
    assert(row.hasRecord)
    assert(row.getRecord.getAttsCount == 1)
    val numbersField = row.getRecord.getAtts(0)
    assert(numbersField.getName == "Numbers")
    val numbersValue = numbersField.getValue
    assert(numbersValue.hasList)
    val numbersList = numbersValue.getList.getValuesList
    assert(numbersList.size() == 3)
    assert(numbersList.get(0).hasInt)
    assert(numbersList.get(0).getInt.getV == 1)
    assert(numbersList.get(1).hasInt)
    assert(numbersList.get(1).getInt.getV == 2)
    assert(numbersList.get(2).hasInt)
    assert(numbersList.get(2).getInt.getV == 3)
  }

  test("Successfully processes a single JSON object") {
    // The Salesforce output is a JSON array with one object.
    // The object has an "Id" and an "Address" field.
    val jsonObject =
      jsonStringToSalesforceValue("""[{"Id": "1", "Address": {"Street": "Jane Doe St", "City": "San Francisco"}}]""")
    val f = new DASSalesforceSOQLFunction(_ => jsonObject)
    val result = runWith(f, "SELECT Id, Address FROM table")
    assert(result.getList.getValuesCount == 1)
    val row = result.getList.getValues(0)
    assert(row.hasRecord)
    val record = row.getRecord
    // Expect two fields: "Id" and "Address"
    assert(record.getAttsCount == 2)
    val idField = record.getAtts(0)
    assert(idField.getName == "Id")
    assert(idField.getValue.hasString)
    assert(idField.getValue.getString.getV == "1")
    val addressField = record.getAtts(1)
    assert(addressField.getName == "Address")
    assert(addressField.getValue.hasRecord)
    val addressRecord = addressField.getValue.getRecord
    // Expect the address record to have two fields: "Street" and "City"
    assert(addressRecord.getAttsCount == 2)
    val streetField = addressRecord.getAttsList.asScala
      .find(_.getName == "Street")
      .getOrElse(fail("Missing 'Street' field in Address"))
    assert(streetField.getValue.hasString)
    assert(streetField.getValue.getString.getV == "Jane Doe St")
    val cityField = addressRecord.getAttsList.asScala
      .find(_.getName == "City")
      .getOrElse(fail("Missing 'City' field in Address"))
    assert(cityField.getValue.hasString)
    assert(cityField.getValue.getString.getV == "San Francisco")
  }

  // A helper to wrap the query string in a Value object.
  private def runWith(f: DASSalesforceSOQLFunction, soql: String): Value = {
    f.execute(Map("q" -> Value.newBuilder().setString(ValueString.newBuilder().setV(soql)).build()))
  }

  // jsonMapper with scala module
  private val jsonMapper = new ObjectMapper().registerModule(DefaultScalaModule)

  // Convert a JSON string to a Scala value. That Scala value is what we get from the
  // Salesforce API. (The JSON string input argument is a convenience for the test.)
  private def jsonStringToSalesforceValue(stringOutput: String) = {
    jsonMapper.readValue(stringOutput, classOf[Array[Map[String, Any]]])
  }

}
