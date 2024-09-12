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

class DASSalesforceConnector(options: Map[String, String]) extends StrictLogging {

  private val jsonMapper = new ObjectMapper with ClassTagExtensions {
    registerModule(new JavaTimeModule())
    registerModule(new Jdk8Module())
    registerModule(new JodaModule())
    registerModule(DefaultScalaModule)
  }

  private val apiConfig = new ApiConfig()
    .setApiVersionString(options("api_version"))
    .setUsername(options("username"))
    .setPassword(options("password") + options("security_token"))
    .setClientId(options("client_id"))
    .setForceURL(options("url"))
    .setObjectMapper(jsonMapper)

  val forceApi = new ForceApi(apiConfig)

  val addDynamicColumns = options.getOrElse("add_dynamic_columns", "true").toBoolean
}
