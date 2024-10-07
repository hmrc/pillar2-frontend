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
import models.EnrolmentRequest.{KnownFacts, KnownFactsParameters, KnownFactsResponse}
import models.{GroupIds, InternalIssueError, UnexpectedJsResult}
import org.scalacheck.Gen
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

class EnrolmentStoreProxyConnectorSpec extends SpecBase {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.enrolment-store-proxy.port" -> server.port()
    )
    .build()
  lazy val connector: EnrolmentStoreProxyConnector = app.injector.instanceOf[EnrolmentStoreProxyConnector]
  private val getIdsUrl = "/enrolment-store-proxy/enrolment-store/enrolments/HMRC-PILLAR2-ORG~PLRID~200/groups"

  private val getKnownFactsUrl = "/enrolment-store-proxy/enrolment-store/enrolments"

  val groupIds:     GroupIds = GroupIds(principalGroupIds = Seq("ABCEDEFGI1234567"), delegatedGroupIds = Seq("ABCEDEFGI1234568"))
  val jsonGroupIds: String   = Json.toJson(groupIds).toString()

  private val errorCodes: Gen[Int] =
    Gen.oneOf(Seq(BAD_REQUEST, FORBIDDEN, NOT_FOUND, INTERNAL_SERVER_ERROR, BAD_GATEWAY, GATEWAY_TIMEOUT, SERVICE_UNAVAILABLE))

  "EnrolmentStoreProxyConnector when calling enrolment store" when {

    "getGroupIds" should {
      "return group IDs associated with an enrolment if 200 response is received" in {

        stubGet(getIdsUrl, OK, jsonGroupIds)
        val result = connector.getGroupIds("200")
        result.futureValue mustBe Some(groupIds)
      }

      "return None for any non-200 status received for this API" in {
        stubGet(getIdsUrl, errorCodes.sample.value, "")
        val result = connector.getGroupIds("200")
        result.futureValue mustBe None

      }
    }
    "getKnownFacts" should {
      "return an identifier and two verifiers using a valid pillar 2 reference" in {
        stubResponse(getKnownFactsUrl, OK, expectedKnownFactsResponse)
        val requestParameters = Json.parse(knownFactsRequest).as[KnownFactsParameters]
        val expectedResponse  = Json.parse(expectedKnownFactsResponse).as[KnownFactsResponse]
        val result            = connector.getKnownFacts(requestParameters)
        result.futureValue mustEqual expectedResponse
      }
      "return failed response in case of a non-200 response from tax enrolment" in {
        stubResponse(getKnownFactsUrl, errorCodes.sample.value, "")
        val result = connector.getKnownFacts(KnownFactsParameters(knownFacts = Seq(KnownFacts("PLRID", "id"))))
        result.failed.futureValue mustBe InternalIssueError
      }
      "return failed response in case of a js result error in the body received from tax enrolments" in {
        stubResponse(getKnownFactsUrl, OK, badKnownFactsResponse)
        val requestParameters = Json.parse(knownFactsRequest).as[KnownFactsParameters]
        val result            = connector.getKnownFacts(requestParameters)
        result.failed.futureValue mustEqual UnexpectedJsResult
      }
    }

  }

}
