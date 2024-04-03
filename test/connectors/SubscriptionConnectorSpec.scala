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
import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.SubscriptionConnectorSpec._
import models.subscription._
import org.scalacheck.Gen
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.LocalDate
import scala.collection.immutable.Seq

class SubscriptionConnectorSpec extends SpecBase with WireMockServerHandler {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.pillar2.port" -> server.port()
    )
    .build()

  lazy val connector: SubscriptionConnector = app.injector.instanceOf[SubscriptionConnector]

  "SubscriptionConnector subscribe" should {

    "return Pillar2Id for create Subscription successful" in {
      stubResponse(s"$apiUrl/subscription/create-subscription", OK, businessSubscriptionSuccessJson)

      val futureValue = connector.subscribe(validSubscriptionCreateParameter)

      futureValue.futureValue mustEqual validSubscriptionSuccessResponse.plrReference
    }

    "return InternalServerError for create Subscription" in {
      stubResponse(s"$apiUrl/subscription/create-subscription", errorCodes.sample.value, "")

      val futureResult = connector.subscribe(validSubscriptionCreateParameter).failed.futureValue

      futureResult mustBe models.InternalIssueError
    }

    "return Duplicated submission for when trying to subscribe an entity that has already been subscribed" in {
      stubResponse(s"$apiUrl/subscription/create-subscription", 409, "")
      val futureResult = connector.subscribe(validSubscriptionCreateParameter).failed.futureValue
      futureResult mustBe models.DuplicateSubmissionError
    }
  }

  "SubscriptionConnector readSubscription" should {

    "return Some(json) when the backend has returned 200 OK with data" in {
      stubGet(s"$readSubscriptionPath/$id/$plrReference", OK, successfulResponseJson)
      val result: Option[ReadSubscriptionResponse] = connector.readSubscription(readSubscriptionParameters).futureValue

      result mustBe defined
      result mustBe Some(
        ReadSubscriptionResponse(UpeDetails("International Organisation Inc.", LocalDate.parse("2022-01-31")), Some(AccountStatus(true)))
      )

    }

    "return None when the backend has returned a non-success status code" in {
      server.stubFor(
        get(urlEqualTo(s"$readSubscriptionPath/$id/$plrReference"))
          .willReturn(aResponse().withStatus(errorCodes.sample.value).withBody(unsuccessfulResponseJson))
      )

      val result = connector.readSubscription(readSubscriptionParameters).futureValue

      result mustBe None
    }
  }
}

object SubscriptionConnectorSpec {
  val apiUrl = "/report-pillar2-top-up-taxes"
  private val errorCodes: Gen[Int] = Gen.oneOf(Seq(400, 403, 500, 501, 502, 503, 504))

  private val businessSubscriptionSuccessJson: String =
    """
      |{
      |"success" : {
      |"plrReference":"XMPLR0012345678",
      |"formBundleNumber":"119000004320",
      |"processingDate":"2023-09-22T00:00"
      |}
      |}""".stripMargin
  val validSubscriptionCreateParameter = SubscriptionRequestParameters("id", "regSafeId", Some("fmSafeId"))

  val validSubscriptionSuccessResponse =
    SubscriptionResponse(
      plrReference = "XMPLR0012345678",
      formBundleNumber = "119000004320",
      processingDate = LocalDate.parse("2023-09-22").atStartOfDay()
    )

  val businessSubscriptionMissingPlrRefJson: String =
    """
      |{
      |"failure" : {
      |"formBundleNumber":"119000004320",
      |"processingDate":"2023-09-22"
      |}
      |}""".stripMargin

  private val readSubscriptionPath       = "/report-pillar2-top-up-taxes/subscription/read-subscription"
  private val id                         = "testId"
  private val plrReference               = "testPlrRef"
  private val readSubscriptionParameters = ReadSubscriptionRequestParameters(id, plrReference)
  private val successfulResponseJson =
    """
      |{
      |  "success": {
      |
      |      "formBundleNumber": "119000004320",
      |      "upeDetails": {
      |          "domesticOnly": false,
      |          "organisationName": "International Organisation Inc.",
      |          "customerIdentification1": "12345678",
      |          "customerIdentification2": "12345678",
      |          "registrationDate": "2022-01-31",
      |          "filingMember": false
      |      },
      |      "upeCorrespAddressDetails": {
      |          "addressLine1": "1 High Street",
      |          "addressLine2": "Egham",
      |
      |          "addressLine3": "Wycombe",
      |          "addressLine4": "Surrey",
      |          "postCode": "HP13 6TT",
      |          "countryCode": "GB"
      |      },
      |      "primaryContactDetails": {
      |          "name": "Fred Flintstone",
      |          "telephone": "0115 9700 700",
      |          "emailAddress": "fred.flintstone@aol.com"
      |      },
      |      "secondaryContactDetails": {
      |          "name": "Donald Trump",
      |          "telephone": "0115 9700 701",
      |          "emailAddress": "donald.trump@potus.com"
      |
      |      },
      |      "filingMemberDetails": {
      |          "safeId": "XL6967739016188",
      |          "organisationName": "Domestic Operations Ltd",
      |          "customerIdentification1": "1234Z678",
      |          "customerIdentification2": "1234567Y"
      |      },
      |      "accountingPeriod": {
      |          "startDate": "2024-01-06",
      |          "endDate": "2025-04-06",
      |          "duetDate": "2024-04-06"
      |      },
      |      "accountStatus": {
      |          "inactive": true
      |      }
      |  }
      |}
      |""".stripMargin

  private val unsuccessfulResponseJson = """{ "status": "error" }"""
}
