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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.{ClassTagExtensions, DefaultScalaModule}
import com.force.api.{ApiConfig, ForceApi}
import com.typesafe.scalalogging.StrictLogging

import scala.collection.JavaConverters._

class DASSalesforceConnector(options: Map[String, String]) extends StrictLogging {

  private val jsonMapper = new ObjectMapper with ClassTagExtensions {
    registerModule(new JavaTimeModule())
    registerModule(new Jdk8Module())
    registerModule(new JodaModule())
    registerModule(DefaultScalaModule)
  }

  // TODO (msb): Add these dynamically?
  private val apiConfig = new ApiConfig()
    .setApiVersionString(options("api_version"))
    .setUsername(options("username"))
    .setPassword(options("password") + options("security_token"))
    .setClientId(options("client_id"))
    //.setClientSecret(options("client_secret"))
    .setForceURL(options("url"))
    .setObjectMapper(jsonMapper)

  val forceApi = new ForceApi(apiConfig)

  val x = forceApi.describeGlobal()
//  x.getSObjects.asScala.foreach(sObject => logger.debug(sObject.getName))
  logger.debug(x.toString)
  val y = forceApi.describeSObject("Customer_Feedback__c")
  y.getFields.asScala.foreach(field => logger.debug(s"${field.getName} -> ${field.getType}"))
  logger.debug(y.toString)
}
