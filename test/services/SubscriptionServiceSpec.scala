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

import akka.Done
import base.SpecBase
import connectors.{EnrolmentConnector, EnrolmentStoreProxyConnector, RegistrationConnector, SubscriptionConnector}
import models.InternalIssueError
import models.registration.RegistrationInfo
import models.subscription.{AccountStatus, ReadSubscriptionRequestParameters, ReadSubscriptionResponse, UpeDetails}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.inject.bind
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionServiceSpec extends SpecBase {

  val service: SubscriptionService = app.injector.instanceOf[SubscriptionService]

  val id           = "testId"
  val plrReference = "testPlrRef"

  "SubscriptionService" when {
    "subscribe" should {
      "return a success response with a pillar 2 reference for non uk based upe and fm" in {
        val userAnswer = emptyUserAnswers
          .setOrException(UpeRegisteredInUKPage, false)
          .setOrException(FmRegisteredInUKPage, false)
          .setOrException(NominateFilingMemberPage, true)
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[EnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
          )
          .build()
        val service = application.injector.instanceOf[SubscriptionService]
        running(application) {
          when(mockRegistrationConnector.register(any(), any())(any())).thenReturn(Future.successful("upeID"))
          when(mockRegistrationConnector.register(any(), any())(any())).thenReturn(Future.successful("fmID"))
          when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentConnector.createEnrolment(any())(any())).thenReturn(Future.successful(Done))
          when(mockEnrolmentStoreProxyConnector.enrolmentExists(any())(any(), any())).thenReturn(Future.successful(false))
          val result = service.createSubscription(userAnswer)
          result.futureValue mustBe "ID"
        }
      }

      "return a success response with a pillar 2 reference for uk based upe and filing member" in {
        val userAnswer = emptyUserAnswers
          .setOrException(UpeRegisteredInUKPage, true)
          .setOrException(FmRegisteredInUKPage, true)
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(FmSafeIDPage, "fmSafeID")
          .setOrException(
            UpeRegInformationPage,
            RegistrationInfo(crn = "crn", utr = "utr", safeId = "upeSafeID", registrationDate = None, filingMember = None)
          )
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[EnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
          )
          .build()
        val service = application.injector.instanceOf[SubscriptionService]
        running(application) {
          when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentConnector.createEnrolment(any())(any())).thenReturn(Future.successful(Done))
          when(mockEnrolmentStoreProxyConnector.enrolmentExists(any())(any(), any())).thenReturn(Future.successful(false))
          val result = service.createSubscription(userAnswer)
          result.futureValue mustBe "ID"
        }
      }
      "return a success response with a pillar 2 reference for uk based upe and no filing member" in {
        val userAnswer = emptyUserAnswers
          .setOrException(UpeRegisteredInUKPage, true)
          .setOrException(NominateFilingMemberPage, false)
          .setOrException(
            UpeRegInformationPage,
            RegistrationInfo(crn = "crn", utr = "utr", safeId = "upeSafeID", registrationDate = None, filingMember = None)
          )
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[EnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
          )
          .build()
        val service = application.injector.instanceOf[SubscriptionService]
        running(application) {
          when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentConnector.createEnrolment(any())(any())).thenReturn(Future.successful(Done))
          when(mockEnrolmentStoreProxyConnector.enrolmentExists(any())(any(), any())).thenReturn(Future.successful(false))
          val result = service.createSubscription(userAnswer)
          result.futureValue mustBe "ID"
        }
      }

      "throw an exception if subscription fails" in {
        val userAnswer = emptyUserAnswers
          .setOrException(UpeRegisteredInUKPage, true)
          .setOrException(FmRegisteredInUKPage, true)
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(FmSafeIDPage, "fmSafeID")
          .setOrException(
            UpeRegInformationPage,
            RegistrationInfo(crn = "crn", utr = "utr", safeId = "upeSafeID", registrationDate = None, filingMember = None)
          )
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[EnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
          )
          .build()
        val service = application.injector.instanceOf[SubscriptionService]
        running(application) {
          when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.failed(models.InternalIssueError))
          when(mockEnrolmentConnector.createEnrolment(any())(any())).thenReturn(Future.successful(Done))
          when(mockEnrolmentStoreProxyConnector.enrolmentExists(any())(any(), any())).thenReturn(Future.successful(false))
          val result = service.createSubscription(userAnswer)
          result.failed.futureValue mustBe models.InternalIssueError
        }
      }

      "throw an exception if create enrolment fails" in {
        val userAnswer = emptyUserAnswers
          .setOrException(UpeRegisteredInUKPage, true)
          .setOrException(FmRegisteredInUKPage, true)
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(FmSafeIDPage, "fmSafeID")
          .setOrException(
            UpeRegInformationPage,
            RegistrationInfo(crn = "crn", utr = "utr", safeId = "upeSafeID", registrationDate = None, filingMember = None)
          )
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[EnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
          )
          .build()
        val service = application.injector.instanceOf[SubscriptionService]
        running(application) {
          when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentConnector.createEnrolment(any())(any())).thenReturn(Future.failed(models.InternalIssueError))
          when(mockEnrolmentStoreProxyConnector.enrolmentExists(any())(any(), any())).thenReturn(Future.successful(false))
          val result = service.createSubscription(userAnswer)
          result.failed.futureValue mustBe models.InternalIssueError
        }
      }

      "throw an exception if enrolment proxy returns true" in {
        val userAnswer = emptyUserAnswers
          .setOrException(UpeRegisteredInUKPage, true)
          .setOrException(FmRegisteredInUKPage, true)
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(FmSafeIDPage, "fmSafeID")
          .setOrException(
            UpeRegInformationPage,
            RegistrationInfo(crn = "crn", utr = "utr", safeId = "upeSafeID", registrationDate = None, filingMember = None)
          )
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[EnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
          )
          .build()
        val service = application.injector.instanceOf[SubscriptionService]
        running(application) {
          when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentStoreProxyConnector.enrolmentExists(any())(any(), any())).thenReturn(Future.successful(true))
          when(mockEnrolmentConnector.createEnrolment(any())(any())).thenReturn(Future.failed(models.InternalIssueError))
          val result = service.createSubscription(userAnswer)
          result.failed.futureValue mustBe models.DuplicateSubmissionError
        }
      }

    }

    "readSubscription" when {

      "return ReadSubscriptionResponse when the connector returns valid data and transformation is successful" in {
        val validResponse =
          ReadSubscriptionResponse(UpeDetails("International Organisation Inc.", LocalDate.parse("2022-01-31")), Some(AccountStatus(true)))
        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[EnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
          )
          .build()

        val service = application.injector.instanceOf[SubscriptionService]

        running(application) {

          when(mockSubscriptionConnector.readSubscriptionAndCache(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(
              Future.successful(
                Some(validResponse)
              )
            )

          val result = service.readSubscription(ReadSubscriptionRequestParameters(id, plrReference)).futureValue

          result mustBe validResponse
        }
      }

      "return InternalIssueError when the connector returns None" in {
        implicit val hc: HeaderCarrier = HeaderCarrier()
        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[EnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
          )
          .build()
        running(application) {
          when(mockSubscriptionConnector.readSubscriptionAndCache(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(None))

          val result = service.readSubscription(ReadSubscriptionRequestParameters(id, plrReference)).failed.futureValue

          result mustBe models.InternalIssueError
        }
      }

      "handle exceptions thrown by the connector" in {
        val requestParameters = ReadSubscriptionRequestParameters(id, plrReference)
        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[EnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
          )
          .build()

        running(application) {
          when(mockSubscriptionConnector.readSubscriptionAndCache(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.failed(new RuntimeException("Connection error")))

          val resultFuture = service.readSubscription(requestParameters)

          whenReady(resultFuture.failed) { e =>
            e mustBe InternalIssueError
          }
        }
      }

    }
  }

}
