/*
 * Copyright 2025 RAW Labs S.A.
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

import scala.jdk.CollectionConverters._

import org.scalatest.funsuite.AnyFunSuite

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.{ClassTagExtensions, DefaultScalaModule}
import com.rawlabs.protocol.das.v1.tables.{Row => ProtoRow}
import com.typesafe.scalalogging.StrictLogging

class DASSalesforceTest extends AnyFunSuite with StrictLogging {

  private val mapper = new ObjectMapper with ClassTagExtensions {
    registerModule(new JavaTimeModule())
    registerModule(new Jdk8Module())
    registerModule(DefaultScalaModule)
  }

  // --------------------------------------------------------------------------
  // Configuration for the test
  // --------------------------------------------------------------------------

  // The options for the DAS
  private val options: Map[String, String] = Map(
    "api_version" -> sys.env("SALESFORCE_API_VERSION"),
    "username" -> sys.env("SALESFORCE_USERNAME"),
    "password" -> sys.env("SALESFORCE_PASSWORD"),
    "security_token" -> sys.env("SALESFORCE_TOKEN"),
    "client_id" -> sys.env("SALESFORCE_CLIENT_ID"),
    "url" -> sys.env("SALESFORCE_URL"),
    "dynamic_objects" -> mapper.readValue[Seq[String]](sys.env("SALESFORCE_OBJECTS")).mkString(","))

  // --------------------------------------------------------------------------
  // 1) Registration
  // --------------------------------------------------------------------------

  test("Should successfully register Salesforce with valid options") {
    new DASSalesforce(options)
  }

  test("Should fail to register Salesforce with missing options") {
    val missingOptions = options - "api_version"
    // FIXME (msb): This doesn't seem to be the correct exception
    assertThrows[NoSuchElementException] {
      new DASSalesforce(missingOptions)
    }
  }

  test("Should fail to register Salesforce with invalid options") {
    val invalidOptions = options + ("security_token" -> "invalid")
    // FIXME (msb): This doesn't seem to be the correct exception
    assertThrows[com.force.api.AuthException] {
      new DASSalesforce(invalidOptions)
    }
  }

  // --------------------------------------------------------------------------
  // 2) Definitions
  // --------------------------------------------------------------------------

  test("Should have some tables") {
    val das = new DASSalesforce(options)
    das.tableDefinitions.nonEmpty
  }

  test("Account table definition should exist with expected columns") {
    val das = new DASSalesforce(options)
    val tableDef = das.tableDefinitions.find(_.getTableId.getName == "salesforce_account")
    assert(tableDef.isDefined, "salesforce_account must be defined")
    val colNames = tableDef.get.getColumnsList
    val actualNames = colNames.asScala.map(_.getName)
    assert(
      actualNames == Seq(
        "id",
        "name",
        "annual_revenue",
        "industry",
        "owner_id",
        "type",
        "account_source",
        "clean_status",
        "created_by_id",
        "created_date",
        "description",
        "is_deleted",
        "last_modified_by_id",
        "last_modified_date",
        "number_of_employees",
        "ownership",
        "phone",
        "rating",
        "sic",
        "ticker_symbol",
        "tradestyle",
        "website",
        "billing_address",
        "shipping_address",
        "master_record_id",
        "parent_id",
        "billing_street",
        "billing_city",
        "billing_state",
        "billing_postal_code",
        "billing_country",
        "billing_latitude",
        "billing_longitude",
        "billing_geocode_accuracy",
        "shipping_street",
        "shipping_city",
        "shipping_state",
        "shipping_postal_code",
        "shipping_country",
        "shipping_latitude",
        "shipping_longitude",
        "shipping_geocode_accuracy",
        "fax",
        "account_number",
        "photo_url",
        "site",
        "currency_iso_code",
        "system_modstamp",
        "last_activity_date",
        "last_viewed_date",
        "last_referenced_date",
        "jigsaw",
        "jigsaw_company_id",
        "duns_number",
        "naics_code",
        "naics_desc",
        "year_started",
        "sic_desc",
        "dandb_company_id",
        "operating_hours_id",
        "CustomerPriority__c",
        "SLA__c",
        "Active__c",
        "NumberofLocations__c",
        "UpsellOpportunity__c",
        "SLASerialNumber__c",
        "SLAExpirationDate__c",
        "Autogenerated__c"),
      s"Expected columns, got $actualNames")
  }

  // --------------------------------------------------------------------------
  // 3) Execution
  // --------------------------------------------------------------------------

  test("Account table project + limit test") {
    val das = new DASSalesforce(options)
    val tableDef = das.getTable("salesforce_account")
    assert(tableDef.isDefined)

    val dt = tableDef.get
    val execResult = dt.execute(quals = Seq.empty, columns = Seq("id"), sortKeys = Seq.empty, maybeLimit = Some(1L))

    val rowsBuffer = scala.collection.mutable.ArrayBuffer.empty[ProtoRow]
    while (execResult.hasNext) {
      rowsBuffer += execResult.next()
    }
    execResult.close()

    assert(rowsBuffer.size == 1)
  }

}
