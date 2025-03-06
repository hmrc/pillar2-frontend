/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import base.{SpecBase, WireMockServerHandler}
import connectors.BarsConnectorSpec._
import models.InternalIssueError
import models.bars._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

import java.util.UUID

class BarsConnectorSpec extends SpecBase with WireMockServerHandler {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.bank-account-reputation.port" -> server.port()
    )
    .build()

  lazy val connector: BarsConnector = app.injector.instanceOf[BarsConnector]

  "BarsConnector" must {
    "verify business back account details and return response" in {
      stubResponse(barsUrl, returnStatus = 200, barsResponse)

      val value = connector.verify(business, account, UUID.randomUUID())

      value.futureValue mustBe expectedBarsResponse
    }

    "return failed future with InternalIssueError for any bad response" in {
      stubResponse(barsUrl, returnStatus = 400, errorResponse)

      val value = connector.verify(business, account, UUID.randomUUID())

      value.failed.futureValue mustBe InternalIssueError
    }
  }
}

object BarsConnectorSpec {
  val barsUrl = "/verify/business"

  val barsRequest: String =
    """
      |{
      |  "account": {
      |    "sortCode": "123456",
      |    "accountNumber": "87654321"
      |  },
      |  "business": {
      |    "companyName": "some company"
      |  }
      |}
      |""".stripMargin

  val barsResponse: String =
    """{
      |  "accountNumberIsWellFormatted": "yes",
      |  "sortCodeIsPresentOnEISCD":"yes",
      |  "sortCodeBankName": "Lloyds",
      |  "nonStandardAccountDetailsRequiredForBacs": "no",
      |  "accountExists": "yes",
      |  "nameMatches": "yes",
      |  "sortCodeSupportsDirectDebit": "yes",
      |  "sortCodeSupportsDirectCredit": "yes"
      |}""".stripMargin

  val errorResponse: String =
    s"""
      |{
      | "error": "error-code-thrown
      |""".stripMargin

  val account:  Account  = Account("123456", "87654321", None)
  val business: Business = Business("some company")

  val expectedBarsResponse: BarsAccountResponse = BarsAccountResponse(
    AccountNumberIsWellFormatted.Yes,
    SortCodeIsPresentOnEISCD.Yes,
    Some("Lloyds"),
    NonStandardAccountDetailsRequiredForBacs.No,
    AccountExists.Yes,
    NameMatches.Yes,
    None,
    SortCodeSupportsDirectDebit.Yes,
    SortCodeSupportsDirectCredit.Yes,
    None
  )
}
