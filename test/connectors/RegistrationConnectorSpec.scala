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
import org.scalacheck.Gen
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

class RegistrationConnectorSpec extends SpecBase with WireMockServerHandler {

  val businessWithoutIdJsonResponse: String =
    """{
      |  "processingDate" : "2023-11-03T11:21:32Z",
      |  "sapNumber" : "0100429672",
      |  "safeId" : "XE1111123456789"
      |}
      |""".stripMargin

  val businessWithoutIdMissingSafeIdJson: String =
    """{
      |  "processingDate" : "2023-11-03T11:21:32Z",
      |  "sapNumber" : "0100429672"
      |}
      |""".stripMargin

  val businessSubscriptionSuccessJson: String =
    """
| {
  |
  "success": {
    | "plrReference": "XMPLR0012345678",
    | "formBundleNumber": "119000004320",
    | "processingDate": "2023-09-22"
    |}
  |
}
""".stripMargin

  val businessSubscriptionMissingPlrRefJson: String =
    """
    |{
    |"formBundleNumber":"119000004320",
    |"processingDate":"2023-09-22"
    |}""".stripMargin

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.pillar2.port" -> server.port()
    )
    .build()

  lazy val connector: RegistrationConnector = app.injector.instanceOf[RegistrationConnector]

  val apiUrl = "/report-pillar2-top-up-taxes"
  private val errorCodes: Gen[Int] = Gen.oneOf(Seq(400, 403, 500, 501, 502, 503, 504))
  private val safeID = "XE1111123456789"
  "RegistrationConnector" when {
    "registerUltimateParent" should {
      "return safeId for Upe Register without Id is successful" in {

        stubResponse(s"$apiUrl/upe/registration/id", OK, businessWithoutIdJsonResponse)
        val result = connector.registerUltimateParent("id")
        result.futureValue mustBe safeID
      }

      "return InternalServerError for EIS returns Error status" in {
        val errorStatus: Int = errorCodes.sample.value
        stubResponse(s"$apiUrl/upe/registration/id", errorStatus, businessWithoutIdJsonResponse)

        val result = connector.registerUltimateParent("id")
        result.failed.futureValue mustBe models.InternalIssueError
      }
    }
    "registerFilingMember" should {
      "return safeId for a filing member when successful" in {

        stubResponse(s"$apiUrl/fm/registration/id", OK, businessWithoutIdJsonResponse)
        val result = connector.registerFilingMember("id")
        result.futureValue mustBe safeID
      }

      "return InternalServerError for EIS returns Error status for FM register withoutId" in {
        val errorStatus: Int = errorCodes.sample.value
        stubResponse(s"$apiUrl/fm/registration/id", errorStatus, businessWithoutIdJsonResponse)

        val result = connector.registerFilingMember("id")
        result.failed.futureValue mustBe models.InternalIssueError

      }
    }
    "registerNewFilingMember" should {

      "return safeId for a filing member when successful" in {

        stubResponse(s"$apiUrl/rfm/registration/id", OK, businessWithoutIdJsonResponse)
        val result = connector.registerNewFilingMember("id")
        result.futureValue mustBe safeID
      }

      "return InternalServerError for EIS returns Error status for FM register withoutId" in {
        val errorStatus: Int = errorCodes.sample.value
        stubResponse(s"$apiUrl/rfm/registration/id", errorStatus, businessWithoutIdJsonResponse)

        val result = connector.registerNewFilingMember("id")
        result.failed.futureValue mustBe models.InternalIssueError

      }
    }

  }

}
