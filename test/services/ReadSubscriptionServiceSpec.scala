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

package services

import akka.util.Timeout
import base.SpecBase
import connectors.ReadSubscriptionConnector
import models.subscription.ReadSubscriptionRequestParameters
import models.{BadRequestError, DuplicateSubmissionError, InternalServerError_, MandatoryInformationMissingError, NotFoundError, ServiceUnavailableError, SubscriptionCreateError, UnauthorizedError, UnprocessableEntityError, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
class ReadSubscriptionServiceSpec extends SpecBase {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[ReadSubscriptionConnector].toInstance(mockReadSubscriptionConnector)
    )
    .build()

  val service: ReadSubscriptionService = app.injector.instanceOf[ReadSubscriptionService]

  val id           = "testId"
  val plrReference = "testPlrRef"

  val transformedUserAnswers = Right(UserAnswers("someId"))
  val transformationError    = Left(MandatoryInformationMissingError("Transformation Error"))
  implicit val timeout: Timeout = Timeout(5.seconds)
  "ReadSubscriptionService" when {

    "return UserAnswers when the connector returns valid data and transformation is successful" in {
      val validJsValue: JsValue = Json.parse("""{ "someField": "someValue" }""")

      when(mockReadSubscriptionConnector.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Some(validJsValue)))

      val service = new ReadSubscriptionService(mockReadSubscriptionConnector, global)

      val result = service.readSubscription(ReadSubscriptionRequestParameters(id, plrReference)).futureValue

      result shouldBe Right(validJsValue)
    }

    "return the raw JSON data even if it's invalid for the application's needs" in {
      val invalidJsValue: JsValue = Json.parse("""{"name":"Joe","age":null}""")

      val id           = "testId"
      val plrReference = "testPlrReference"
      val parameters   = ReadSubscriptionRequestParameters(id, plrReference)

      val mockReadSubscriptionConnector = mock[ReadSubscriptionConnector]
      when(mockReadSubscriptionConnector.readSubscription(eqTo(parameters))(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Some(invalidJsValue)))
      val service = new ReadSubscriptionService(mockReadSubscriptionConnector, global)

      val result = service.readSubscription(parameters).futureValue
      result shouldBe Right(invalidJsValue)
    }

    "return SubscriptionCreateError when the connector returns None" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()

      when(mockReadSubscriptionConnector.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(None))
      val service = new ReadSubscriptionService(mockReadSubscriptionConnector, global)

      val result = service.readSubscription(ReadSubscriptionRequestParameters(id, plrReference)).futureValue

      result mustBe Left(SubscriptionCreateError)
    }

    "handle exceptions thrown by the connector" in {
      val requestParameters = ReadSubscriptionRequestParameters(id, plrReference)

      when(mockReadSubscriptionConnector.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.failed(new RuntimeException("Connection error")))

      val resultFuture = service.readSubscription(requestParameters)

      whenReady(resultFuture) { result =>
        result should matchPattern { case Left(InternalServerError_) => }
      }
    }

    "handle UpstreamErrorResponse thrown by the connector" in {
      val requestParameters = ReadSubscriptionRequestParameters(id, plrReference)

      val errorStatusCodes = List(400, 404, 409, 422, 500, 503)
      for (statusCode <- errorStatusCodes) {
        when(mockReadSubscriptionConnector.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(Future.failed(UpstreamErrorResponse("Upstream error", statusCode, statusCode)))

        val resultFuture = service.readSubscription(requestParameters)

        whenReady(resultFuture) { result =>
          statusCode match {
            case 400 => result should matchPattern { case Left(BadRequestError) => }
            case 401 => result should matchPattern { case Left(UnauthorizedError) => }
            case 404 => result should matchPattern { case Left(NotFoundError) => }
            case 409 => result should matchPattern { case Left(DuplicateSubmissionError) => }
            case 422 => result should matchPattern { case Left(UnprocessableEntityError) => }
            case 500 => result should matchPattern { case Left(InternalServerError_) => }
            case 503 => result should matchPattern { case Left(ServiceUnavailableError) => }
          }
        }
      }
    }

  }
}
