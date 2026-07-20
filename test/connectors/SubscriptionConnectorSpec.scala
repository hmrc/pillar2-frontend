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
import com.github.tomakehurst.wiremock.client.WireMock.*
import models.*
import models.subscription.*
import org.apache.pekko.Done
import play.api.Application
import play.api.http.Status.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpException

class SubscriptionConnectorSpec extends SpecBase with WireMockServerHandler {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(conf = "microservice.services.pillar2.port" -> server.port())
    .build()

  lazy val connector: SubscriptionConnector = app.injector.instanceOf[SubscriptionConnector]

  "SubscriptionConnector" when {

    "calling subscribe" must {
      "return the Pillar 2 reference when the backend returns 200 OK" in {
        stubResponse(createSubscriptionPath, OK, businessSubscriptionSuccessResponseJson)

        val futureValue = connector.subscribe(validSubscriptionCreateParameter)

        futureValue.futureValue mustEqual validBusinessSubscriptionSuccessResponse.plrReference
      }

      "fail with InternalIssueError when the backend returns an error status" in {
        stubResponse(createSubscriptionPath, errorCodes.sample.value, "")

        val futureResult = connector.subscribe(validSubscriptionCreateParameter).failed.futureValue

        futureResult mustBe models.InternalIssueError
      }

      "fail with DuplicateSubmissionError when the backend returns 409 (entity already subscribed)" in {
        stubResponse(createSubscriptionPath, 409, "")

        val futureResult = connector.subscribe(validSubscriptionCreateParameter).failed.futureValue

        futureResult mustBe models.DuplicateSubmissionError
      }
    }

    "calling amendSubscriptionV2" must {
      val amendV2Data = Json.parse(subscriptionDataAmendJson).as[SubscriptionDataAmend]

      "return Done when the backend has returned 200 OK" in {
        stubResponseForPutRequest(s"$amendSubscriptionV2Path/$testId", OK, Some(subscriptionDataAmendJson))
        connector.amendSubscriptionV2(testId, amendV2Data).futureValue mustBe Done
      }

      "fail with UnprocessableEntityError when the backend has returned a 422 status" in {
        stubResponseForPutRequest(s"$amendSubscriptionV2Path/$testId", UNPROCESSABLE_ENTITY, None)
        connector.amendSubscriptionV2(testId, amendV2Data).failed.futureValue mustEqual UnprocessableEntityError
      }

      "fail with UnexpectedResponse when the backend has returned a non-success and no 422 status code" in {
        stubResponseForPutRequest(s"$amendSubscriptionV2Path/$testId", errorCodes.sample.value, None)
        connector.amendSubscriptionV2(testId, amendV2Data).failed.futureValue mustEqual UnexpectedResponse
      }
    }

    "calling readSubscriptionV2" must {
      "return Some(SubscriptionDataDisplay) when backend returns 200 OK" in {
        stubGet(s"$readSubscriptionV2Path/$testPillar2Id", OK, subscriptionDataDisplayWrappedJson)
        val result = connector.readSubscriptionV2(testPillar2Id).futureValue
        result mustBe defined
        result.get.formBundleNumber mustBe testFormBundleNumber
        result.get.accountingPeriod mustBe defined
        result.get.accountingPeriod.value must have size 1
        result.get.accountingPeriod.value.head.canAmendStartDate mustBe Some(true)
      }

      "return None when backend returns 404" in {
        stubGet(s"$readSubscriptionV2Path/$testPillar2Id", NOT_FOUND, unsuccessfulNotFoundJson)
        connector.readSubscriptionV2(testPillar2Id).futureValue mustBe None
      }

      "fail with UnprocessableEntityError when backend returns 422" in {
        stubGet(s"$readSubscriptionV2Path/$testPillar2Id", UNPROCESSABLE_ENTITY, unsuccessfulResponseJson)
        connector.readSubscriptionV2(testPillar2Id).failed.futureValue mustBe UnprocessableEntityError
      }

      "fail with RetryableGatewayError when backend returns 500" in {
        stubGet(s"$readSubscriptionV2Path/$testPillar2Id", INTERNAL_SERVER_ERROR, "")
        connector.readSubscriptionV2(testPillar2Id).failed.futureValue mustBe RetryableGatewayError
      }

      "fail with InternalIssueError when backend returns 503" in {
        stubGet(s"$readSubscriptionV2Path/$testPillar2Id", SERVICE_UNAVAILABLE, "")
        connector.readSubscriptionV2(testPillar2Id).failed.futureValue mustBe InternalIssueError
      }
    }

    "calling readAndCacheSubscriptionV2" must {
      "return SubscriptionDataDisplay when backend returns 200 OK" in {
        stubGet(s"$readSubscriptionV2Path/$testId/$testPillar2Id", OK, subscriptionDataDisplayJson)
        val result = connector.readAndCacheSubscriptionV2(testId, testPillar2Id).futureValue
        result.formBundleNumber mustBe testFormBundleNumber
        result.accountingPeriod mustBe defined
        result.accountingPeriod.value must have size 1
        result.accountingPeriod.value.head.canAmendStartDate mustBe Some(true)
      }

      "fail with NoResultFound when backend returns 404" in {
        stubGet(s"$readSubscriptionV2Path/$testId/$testPillar2Id", NOT_FOUND, unsuccessfulNotFoundJson)
        connector.readAndCacheSubscriptionV2(testId, testPillar2Id).failed.futureValue mustBe models.NoResultFound
      }

      "fail with UnprocessableEntityError when backend returns 422" in {
        stubGet(s"$readSubscriptionV2Path/$testId/$testPillar2Id", UNPROCESSABLE_ENTITY, unsuccessfulResponseJson)
        connector.readAndCacheSubscriptionV2(testId, testPillar2Id).failed.futureValue mustBe UnprocessableEntityError
      }

      "fail with RetryableGatewayError when backend returns 500" in {
        stubGet(s"$readSubscriptionV2Path/$testId/$testPillar2Id", INTERNAL_SERVER_ERROR, "")
        connector.readAndCacheSubscriptionV2(testId, testPillar2Id).failed.futureValue mustBe RetryableGatewayError
      }

      "fail with RetryableGatewayError when backend returns 502" in {
        stubGet(s"$readSubscriptionV2Path/$testId/$testPillar2Id", BAD_GATEWAY, "")
        connector.readAndCacheSubscriptionV2(testId, testPillar2Id).failed.futureValue mustBe RetryableGatewayError
      }

      "fail with InternalIssueError when backend returns 503" in {
        stubGet(s"$readSubscriptionV2Path/$testId/$testPillar2Id", SERVICE_UNAVAILABLE, "")
        connector.readAndCacheSubscriptionV2(testId, testPillar2Id).failed.futureValue mustBe InternalIssueError
      }
    }

    "calling getSubscriptionCache" must {
      "return Some(SubscriptionLocalData) when the backend returns 200 OK with valid data" in {
        stubGet(s"$readCachedSubscriptionPath/$testId", OK, Json.toJson(emptySubscriptionLocalData).toString)
        val result: Option[SubscriptionLocalData] = connector.getSubscriptionCache(testId).futureValue

        result mustBe defined
        result mustBe Some(emptySubscriptionLocalData)
      }

      "return None when the backend returns 200 but JSON is unparseable" in {
        stubGet(s"$readCachedSubscriptionPath/$testId", OK, """{"invalid": "json"}""")
        val result = connector.getSubscriptionCache(testId).futureValue
        result mustBe None
      }

      "return None when the backend has returned a non-success status code" in {
        server.stubFor(
          get(urlEqualTo(s"$readCachedSubscriptionPath/$testId"))
            .willReturn(aResponse().withStatus(errorCodes.sample.value))
        )

        val result = connector.getSubscriptionCache(testId).futureValue
        result mustBe None
      }
    }

    "calling save" must {
      "return the saved data when the backend returns 200 OK" in {
        val json = Json.toJson(emptySubscriptionLocalData)
        stubResponse(s"$readCachedSubscriptionPath/$testId", OK, json.toString)
        val result = connector.save(testId, json).futureValue
        result mustBe json
      }

      "fail with HttpException when the backend returns a non-success status code" in {
        val json = Json.toJson(emptySubscriptionLocalData)
        stubResponse(s"$readCachedSubscriptionPath/$testId", errorCodes.sample.value, "")
        val result = connector.save(testId, json)

        result.failed.futureValue mustBe a[HttpException]
      }
    }
  }
}
