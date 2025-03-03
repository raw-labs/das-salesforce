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

import scala.jdk.CollectionConverters.{IterableHasAsScala, MapHasAsJava, MapHasAsScala}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.{ClassTagExtensions, DefaultScalaModule}
import com.force.api._
import com.rawlabs.das.sdk.{
  DASSdkInvalidArgumentException,
  DASSdkPermissionDeniedException,
  DASSdkUnauthenticatedException
}
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

  private val forceApi: ForceApi =
    withSalesforceException {
      new ForceApi(apiConfig)
    }

  val addDynamicColumns: Boolean = options.getOrElse("add_dynamic_columns", "true").toBoolean

  def createSObject(salesforceObjectName: String, data: Map[String, Any]): String = withSalesforceException {
    forceApi.createSObject(salesforceObjectName, data.asJava)
  }

  def describeSObject(salesforceObjectName: String): DescribeSObject = withSalesforceException {
    forceApi.describeSObject(salesforceObjectName)
  }

  def updateSObject(salesforceObjectName: String, id: String, data: Map[String, Any]): Unit = withSalesforceException {
    forceApi.updateSObject(salesforceObjectName, id, data.asJava)
  }

  def deleteSObject(salesforceObjectName: String, id: String): Unit = withSalesforceException {
    forceApi.deleteSObject(salesforceObjectName, id)
  }

  def paginatedSOQL(soql: String): Iterator[Seq[Map[String, Any]]] = {
    val page0 = withSalesforceException {
      forceApi.query(soql)
    }

    new Iterator[Seq[Map[String, Any]]] {

      private var currentBatch = Option(page0.getRecords) // We start with the first batch
      private var nextUrl = Option(page0.getNextRecordsUrl) // If there's a next URL, there are more rows

      override def hasNext: Boolean = {
        // currentBatch is set to null when a page was consumed.
        if (currentBatch.isEmpty) {
          // 'next' did consume the last batch. If there's a next URL, there are more rows,
          // fetch the next batch.
          nextUrl.foreach { url =>
            val nextPage =
              withSalesforceException {
                forceApi.queryMore(url)
              }
            currentBatch = Option(nextPage.getRecords)
            nextUrl = Option(nextPage.getNextRecordsUrl)
          }
        }
        // If currentBatch is still null, there are no more rows.
        currentBatch.nonEmpty
      }

      override def next(): Seq[collection.immutable.Map[String, Any]] = {
        if (!hasNext) {
          throw new NoSuchElementException("No more rows")
        }
        val rows = currentBatch.get
        currentBatch = None
        rows.asScala.map(map => map.asScala.map { case (k, v) => (k.asInstanceOf[String], v) }.toMap).toSeq
      }
    }

  }

  private def withSalesforceException[T](block: => T): T = {
    try {
      block
    } catch {
      case t: Throwable =>
        logger.warn("Salesforce API error", t)
        throw mapToSdkException(t).getOrElse(t)
    }
  }

  private def mapToSdkException(t: Throwable): Option[RuntimeException] = t match {
    case e: AuthException =>
      // e.getCode isn't telling us much. For example, a wrong login/password may return 500 (internal server error).
      // The AuthException at least tells us the error occurred during the authentication process.
      Some(new DASSdkUnauthenticatedException(e.getMessage, e))
    case e: ApiTokenException =>
      // Comes with HTTP 401
      Some(new DASSdkUnauthenticatedException(e.getMessage, e))
    case e: ResourceException =>
      // Thrown when creating a resource fails, because the JSON body (part of the query) couldn't be generated.
      // That usually means the query is wrong. We cannot map this to a DAS exception.
      None
    case e: SObjectException =>
      // Thrown when creating a resource fails (the server returns an error). We map it to an "invalid argument" exception.
      Some(new DASSdkInvalidArgumentException(e.getMessage, e))
    case e: ApiException =>
      e.getCode match {
        case 400 | 404 | 409 | 410 | 412 =>
          // API docs:
          // - 400: The request couldn’t be understood, usually because the JSON or XML body contains an error.
          // - 404: The requested resource couldn’t be found. Check the URI for errors, and verify that there are no sharing issues.
          // - 409: The request couldn’t be completed due to a conflict with the current state of the resource.
          //        Check that the API version is compatible with the resource you’re requesting.
          // - 410: The requested resource has been retired or removed. Delete or update any references to the resource.
          // - 412: The request wasn’t executed because one or more of the preconditions that the client
          //        specified in the request headers wasn’t satisfied. For example, the request includes
          //        an If-Unmodified-Since header, but the data was modified after the specified date.
          Some(new DASSdkInvalidArgumentException(e.getMessage, e))
        case 401 =>
          // The session ID or OAuth token used has expired or is invalid.
          // The response body contains the message and errorCode.
          Some(new DASSdkUnauthenticatedException(e.getMessage, e))
        case 403 =>
          // The request has been refused. Verify that the logged-in user has appropriate permissions.
          // If the error code is REQUEST_LIMIT_EXCEEDED, you’ve exceeded API request limits in your org.
          Some(new DASSdkPermissionDeniedException(e.getMessage, e))
        case _ =>
          // Other errors are unexpected
          logger.warn("Unexpected salesforce API error", e)
          None
      }
    case _: Throwable =>
      logger.warn("Unexpected error", t)
      None
  }
}
