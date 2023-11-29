/*
 * Copyright 2023 HM Revenue & Customs
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
import models.{MandatoryInformationMissingError, SubscriptionCreateError, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

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
  val validJsValue = Json.parse("""{
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
      |}""".stripMargin)
  val invalidJsValue = Json.parse("""{"invalid": "json"}""")

  private val readSubscriptionParameters = ReadSubscriptionRequestParameters(id, plrReference)

  val transformedUserAnswers = Right(UserAnswers("someId"))
  val transformationError    = Left(MandatoryInformationMissingError("Transformation Error"))
  implicit val timeout: Timeout = Timeout(5.seconds)
  "ReadSubscriptionService" when {

    "return UserAnswers when the connector returns valid data and transformation is successful" in {
      val validJsValue = Json.parse("""{
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
          |}""".stripMargin)

      val userAnswers                        = UserAnswers("some-id", validJsValue.as[JsObject])
      val mockSubscriptionTransformerWrapper = mock[SubscriptionTransformerWrapper]
      val expectedResult                     = Right(userAnswers)

      when(mockReadSubscriptionConnector.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Some(validJsValue.as[JsObject])))

      when(mockSubscriptionTransformerWrapper.jsValueToSubscription(validJsValue))
        .thenReturn(Right(userAnswers))

      val service = new ReadSubscriptionService(mockReadSubscriptionConnector, mockSubscriptionTransformerWrapper)

      val result = service.readSubscription(ReadSubscriptionRequestParameters(id, plrReference)).futureValue

      result shouldBe expectedResult
    }

    "return ApiError when the connector returns valid data but transformation fails" in {
      val mockSubscriptionTransformerWrapper = mock[SubscriptionTransformerWrapper]
      implicit val hc: HeaderCarrier = HeaderCarrier()

      val id                  = "testId"
      val plrReference        = "testPlrReference"
      val invalidJsValue      = Json.parse("""{"name":"Joe","age":null}""")
      val transformationError = MandatoryInformationMissingError("some error message")

      // Mock the transformation to return an error
      when(mockSubscriptionTransformerWrapper.jsValueToSubscription(any[JsValue]))
        .thenReturn(Left(transformationError))

      val mockReadSubscriptionConnector = mock[ReadSubscriptionConnector]
      when(mockReadSubscriptionConnector.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Some(invalidJsValue)))

      val service = new ReadSubscriptionService(mockReadSubscriptionConnector, mockSubscriptionTransformerWrapper)

      val result = service.readSubscription(ReadSubscriptionRequestParameters(id, plrReference)).futureValue

      result mustBe Left(transformationError)
    }

    "return SubscriptionCreateError when the connector returns None" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val mockSubscriptionTransformerWrapper = mock[SubscriptionTransformerWrapper]

      when(mockReadSubscriptionConnector.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(None))
      val service = new ReadSubscriptionService(mockReadSubscriptionConnector, mockSubscriptionTransformerWrapper)

      val result = service.readSubscription(ReadSubscriptionRequestParameters(id, plrReference)).futureValue

      result mustBe Left(SubscriptionCreateError)
    }

    "handle exceptions thrown by the connector" in {
      val requestParameters = ReadSubscriptionRequestParameters(id, plrReference)

      when(mockReadSubscriptionConnector.readSubscription(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.failed(new RuntimeException("Connection error")))

      val resultFuture = service.readSubscription(requestParameters)

      whenReady(resultFuture.failed) { e =>
        e            shouldBe a[RuntimeException]
        e.getMessage shouldBe "Connection error"
      }
    }

  }
}
