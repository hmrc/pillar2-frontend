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
import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import models.subscription.ReadSubscriptionRequestParameters
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

class ReadSubscriptionConnectorSpec extends SpecBase {
  private val readSubscriptionPath       = "/report-pillar2-top-up-taxes/subscription/read-subscription"
  private val id                         = "testId"
  private val plrReference               = "testPlrRef"
  private val readSubscriptionParameters = ReadSubscriptionRequestParameters(id, plrReference)
  private val successfulResponseJson = """{
                                             |    "subMneOrDomestic": "uk",
                                             |    "upeNameRegistration": "International Organisation Inc.",
                                             |    "subPrimaryContactName": "Fred Flintstone",
                                             |    "subPrimaryEmail": "fred.flintstone@aol.com",
                                             |    "subSecondaryContactName": "Donald Trump",
                                             |    "upeRegInformationId": {
                                             |        "crn": "12345678",
                                             |        "utr": "12345678",
                                             |        "safeId": "",
                                             |        "registrationDate": "2022-01-31",
                                             |        "filingMember": false
                                             |    },
                                             |    "upeRegisteredAddress": {
                                             |        "addressLine1": "1 High Street",
                                             |        "addressLine2": "Egham",
                                             |        "addressLine3": "Surrey",
                                             |        "postalCode": "HP13 6TT",
                                             |        "countryCode": "GB"
                                             |    },
                                             |    "FmSafeID": "XL6967739016188",
                                             |    "subFilingMemberDetails": {
                                             |        "safeId": "XL6967739016188",
                                             |        "customerIdentification1": "1234Z678",
                                             |        "customerIdentification2": "1234567Y",
                                             |        "organisationName": "Domestic Operations Ltd"
                                             |    },
                                             |    "subAccountingPeriod": {
                                             |        "startDate": "2023-04-06",
                                             |        "endDate": "2023-04-06",
                                             |        "duetDate": "2023-04-06"
                                             |    },
                                             |    "subAccountStatus": {
                                             |        "inactive": true
                                             |    },
                                             |    "subSecondaryEmail": "fred.flintstone@potus.com",
                                             |    "subSecondaryCapturePhone": "0115 9700 700"
                                             |}""".stripMargin
  private val unsuccessfulResponseJson = """{ "status": "error" }"""
  val apiUrl                           = "/report-pillar2-top-up-taxes"

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.pillar2.port" -> server.port()
    )
    .build()
  lazy val connector: ReadSubscriptionConnector = app.injector.instanceOf[ReadSubscriptionConnector]

  "ReadSubscriptionConnector" should {

    "return Some(json) when the backend has returned 200 OK with data" in {
      stubGet(s"$readSubscriptionPath/$id/$plrReference", OK, successfulResponseJson)
      val result = connector.readSubscription(readSubscriptionParameters).futureValue

      result mustBe defined
      result.get mustBe Json.parse(successfulResponseJson)

    }

    "return None when the backend has returned a non-success status code" in {
      server.stubFor(
        get(urlEqualTo(s"$readSubscriptionPath/$id/$plrReference"))
          .willReturn(aResponse().withStatus(404).withBody(unsuccessfulResponseJson))
      )

      val result = connector.readSubscription(readSubscriptionParameters).futureValue

      result mustBe None
    }

    "return None when there is an exception during the call" in {
      server.stubFor(
        get(urlEqualTo(s"$readSubscriptionPath/$id/$plrReference"))
          .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
      )
      val result = connector.readSubscription(readSubscriptionParameters).futureValue

      result mustBe None
    }
  }
}
