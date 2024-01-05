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
import models.subscription.{SubscriptionRequestParameters, SubscriptionResponse}
import org.scalacheck.Gen
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

import java.time.LocalDate
import scala.collection.Seq

class SubscriptionConnectorSpec extends SpecBase with WireMockServerHandler {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.pillar2.port" -> server.port()
    )
    .build()

  lazy val connector: SubscriptionConnector = app.injector.instanceOf[SubscriptionConnector]

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
      |"formBundleNumber":"119000004320",
      |"processingDate":"2023-09-22"
      |}""".stripMargin
  "SubscriptionConnector" when {
    "return Pillar2Id for create Subscription successful" in {
      stubResponse(s"$apiUrl/subscription/create-subscription", OK, businessSubscriptionSuccessJson)
      val result = connector.crateSubscription(validSubscriptionCreateParameter)
      result.futureValue mustBe Some(validSubscriptionSuccessResponse)
    }

    "return InternalServerError for create Subscription" in {

      stubResponse(s"$apiUrl/subscription/create-subscription", OK, businessSubscriptionMissingPlrRefJson)
      val result = connector.crateSubscription(validSubscriptionCreateParameter)
      result.futureValue mustBe None
    }
    "return InternalServerError for EIS returns Error status" in {
      val errorStatus: Int = errorCodes.sample.value
      stubResponse(s"$apiUrl/subscription/create-subscription", errorStatus, businessSubscriptionSuccessJson)

      val result = connector.crateSubscription(validSubscriptionCreateParameter)
      result.futureValue mustBe None
    }

  }
}
