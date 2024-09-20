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

object DASSalesforceUtils {

  // Convert e.g. account_number to AccountNumber
  // This only works because Salesforce native fields never have underscores
  def renameToSalesforce(name: String): String = {
    if (name.endsWith("__c")) name // Custom objects must be left as is
    else name.split("_").map(_.capitalize).mkString
  }

  // Convert e.g. Price2Book to price_2_book
  // This only works because Salesforce native fields never have underscores
  def renameFromSalesforce(name: String): String = {
    if (name.endsWith("__c")) name // Custom objects must be left as is
    else {
      val parts = name.split("(?=[A-Z0-9])")
      parts.mkString("_").toLowerCase
    }
  }
}
