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
import models.{BadRequestError, DuplicateSubmissionError, InternalServerError_, MandatoryInformationMissingError, NotFoundError, ServiceUnavailableError, SubscriptionCreateError, UnprocessableEntityError, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.global
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

  val requestParameters = ReadSubscriptionRequestParameters(id, plrReference)

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

    "handle IOException thrown by the connector" in {
      val requestParameters = ReadSubscriptionRequestParameters(id, plrReference)

      when(mockReadSubscriptionConnector.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(None))

      val resultFuture = service.readSubscription(requestParameters)

      whenReady(resultFuture) { result =>
        result should matchPattern { case Left(SubscriptionCreateError) => }
      }
    }

    "handle BadRequest (400) response from the connector" in {

      when(mockReadSubscriptionConnector.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Some(Json.obj("statusCode" -> 400, "error" -> "Bad Request"))))

      val resultFuture = service.readSubscription(requestParameters)

      whenReady(resultFuture) { result =>
        result should matchPattern { case Left(BadRequestError) => }
      }
    }

    "handle 404 Not Found response from the connector" in {
      when(mockReadSubscriptionConnector.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Some(Json.obj("statusCode" -> 404, "error" -> Json.obj("errorDetail" -> Json.obj("errorCode" -> "404"))))))

      val resultFuture = service.readSubscription(requestParameters)

      resultFuture.map { result =>
        result shouldEqual Left(NotFoundError)
      }
    }

    "handle 409 Conflict response from the connector" in {
      when(mockReadSubscriptionConnector.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Some(Json.obj("statusCode" -> 409, "error" -> Json.obj("errorDetail" -> Json.obj("errorCode" -> "409"))))))

      val resultFuture = service.readSubscription(requestParameters)

      resultFuture.map { result =>
        result shouldEqual Left(DuplicateSubmissionError)
      }
    }

    "handle 422 Unprocessable Entity response from the connector" in {
      when(mockReadSubscriptionConnector.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Some(Json.obj("statusCode" -> 422, "error" -> Json.obj("errorDetail" -> Json.obj("errorCode" -> "422"))))))

      val resultFuture = service.readSubscription(requestParameters)

      resultFuture.map { result =>
        result shouldEqual Left(UnprocessableEntityError)
      }
    }

    "handle 500 Internal Server Error response from the connector" in {
      when(mockReadSubscriptionConnector.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Some(Json.obj("statusCode" -> 500, "error" -> Json.obj("errorDetail" -> Json.obj("errorCode" -> "500"))))))

      val resultFuture = service.readSubscription(requestParameters)

      resultFuture.map { result =>
        result shouldEqual Left(InternalServerError_)
      }
    }

    "handle 503 Service Unavailable response from the connector" in {
      when(mockReadSubscriptionConnector.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Some(Json.obj("statusCode" -> 503, "error" -> Json.obj("errorDetail" -> Json.obj("errorCode" -> "503"))))))

      val resultFuture = service.readSubscription(requestParameters)

      resultFuture.map { result =>
        result shouldEqual Left(ServiceUnavailableError)
      }
    }

  }
}
