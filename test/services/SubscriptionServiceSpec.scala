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

import base.SpecBase
import connectors.*
import models.*
import models.EnrolmentRequest.{AllocateEnrolmentParameters, KnownFactsParameters, KnownFactsResponse}
import models.grs.{GrsRegistrationResult, RegistrationStatus}
import models.registration.*
import models.rfm.CorporatePosition
import models.subscription.*
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, verify, when}
import org.scalatest.matchers.should.Matchers.*
import pages.*
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionServiceSpec extends SpecBase {

  val id           = "testId"
  val plrReference = "testPlrRef"
  val expectedGroupIdReturned: Future[Some[GroupIds]] =
    Future.successful(Some(GroupIds(principalGroupIds = Seq("groupID"), delegatedGroupIds = Seq.empty)))
  val mockTaxEnrolmentConnector: TaxEnrolmentConnector = mock[TaxEnrolmentConnector]

  "SubscriptionService" must {
    "subscribe" when {
      "return a success response with a pillar 2 reference for non uk based upe and fm" in {
        val userAnswer = emptyUserAnswers
          .setOrException(UpeRegisteredInUKPage, false)
          .setOrException(FmRegisteredInUKPage, false)
          .setOrException(NominateFilingMemberPage, true)
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[TaxEnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        val service = application.injector.instanceOf[SubscriptionService]
        running(application) {
          when(mockRegistrationConnector.registerUltimateParent(any())(using any())).thenReturn(Future.successful("upeID"))
          when(mockRegistrationConnector.registerFilingMember(any())(using any())).thenReturn(Future.successful("fmID"))
          when(mockSubscriptionConnector.subscribe(any())(using any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentConnector.enrolAndActivate(any())(using any())).thenReturn(Future.successful(Done))
          when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(using any())).thenReturn(Future.successful(None))
          when(mockUserAnswersConnectors.save(any(), any())(using any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
          when(mockUserAnswersConnectors.getUserAnswer(any())(using any())).thenReturn(Future.successful(Some(userAnswer)))

          val result = service.createSubscription(userAnswer)
          result.futureValue mustBe "ID"
        }
      }

      "return a success response with a pillar 2 reference for non uk based upe and no filing member" in {
        val userAnswer = emptyUserAnswers
          .setOrException(UpeRegisteredInUKPage, false)
          .setOrException(FmRegisteredInUKPage, false)
          .setOrException(NominateFilingMemberPage, false)
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[TaxEnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        val service = application.injector.instanceOf[SubscriptionService]
        running(application) {
          when(mockRegistrationConnector.registerUltimateParent(any())(using any())).thenReturn(Future.successful("upeID"))
          when(mockSubscriptionConnector.subscribe(any())(using any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentConnector.enrolAndActivate(any())(using any())).thenReturn(Future.successful(Done))
          when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(using any())).thenReturn(Future.successful(None))
          when(mockUserAnswersConnectors.save(any(), any())(using any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
          when(mockUserAnswersConnectors.getUserAnswer(any())(using any())).thenReturn(Future.successful(Some(userAnswer)))

          val result = service.createSubscription(userAnswer)
          result.futureValue mustBe "ID"
        }
      }

      "return success response and do not call register connector when non uk upe and fm is already set" in {
        val userAnswer = emptyUserAnswers
          .setOrException(UpeRegisteredInUKPage, false)
          .setOrException(FmRegisteredInUKPage, false)
          .setOrException(NominateFilingMemberPage, true)
          .setOrException(UpeNonUKSafeIDPage, "123123")
          .setOrException(FmNonUKSafeIDPage, "321321")
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[TaxEnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        val service = application.injector.instanceOf[SubscriptionService]
        running(application) {
          when(mockSubscriptionConnector.subscribe(any())(using any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentConnector.enrolAndActivate(any())(using any())).thenReturn(Future.successful(Done))
          when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(using any())).thenReturn(Future.successful(None))
          when(mockUserAnswersConnectors.save(any(), any())(using any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
          when(mockUserAnswersConnectors.getUserAnswer(any())(using any())).thenReturn(Future.successful(Some(userAnswer)))

          val result = service.createSubscription(userAnswer)

          verify(mockRegistrationConnector, never()).registerUltimateParent(any())(using any())
          verify(mockRegistrationConnector, never()).registerFilingMember(any())(using any())

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
            bind[TaxEnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        val service = application.injector.instanceOf[SubscriptionService]
        running(application) {
          when(mockSubscriptionConnector.subscribe(any())(using any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentConnector.enrolAndActivate(any())(using any())).thenReturn(Future.successful(Done))
          when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(using any())).thenReturn(Future.successful(None))
          when(mockUserAnswersConnectors.getUserAnswer(any())(using any())).thenReturn(Future.successful(Some(userAnswer)))
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
            bind[TaxEnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        val service = application.injector.instanceOf[SubscriptionService]
        running(application) {
          when(mockSubscriptionConnector.subscribe(any())(using any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentConnector.enrolAndActivate(any())(using any())).thenReturn(Future.successful(Done))
          when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(using any())).thenReturn(Future.successful(None))
          when(mockUserAnswersConnectors.getUserAnswer(any())(using any())).thenReturn(Future.successful(Some(userAnswer)))
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
            bind[TaxEnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        val service = application.injector.instanceOf[SubscriptionService]
        running(application) {
          when(mockSubscriptionConnector.subscribe(any())(using any())).thenReturn(Future.failed(models.InternalIssueError))
          when(mockEnrolmentConnector.enrolAndActivate(any())(using any())).thenReturn(Future.successful(Done))
          when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(using any())).thenReturn(Future.successful(None))
          when(mockUserAnswersConnectors.save(any(), any())(using any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
          when(mockUserAnswersConnectors.getUserAnswer(any())(using any())).thenReturn(Future.successful(Some(userAnswer)))
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
            bind[TaxEnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        val service = application.injector.instanceOf[SubscriptionService]
        running(application) {
          when(mockSubscriptionConnector.subscribe(any())(using any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentConnector.enrolAndActivate(any())(using any())).thenReturn(Future.failed(models.InternalIssueError))
          when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(using any())).thenReturn(Future.successful(None))
          when(mockUserAnswersConnectors.save(any(), any())(using any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
          when(mockUserAnswersConnectors.getUserAnswer(any())(using any())).thenReturn(Future.successful(Some(userAnswer)))
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
            bind[TaxEnrolmentConnector].toInstance(mockEnrolmentConnector),
            bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        val service = application.injector.instanceOf[SubscriptionService]
        running(application) {
          when(mockSubscriptionConnector.subscribe(any())(using any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(using any())).thenReturn(expectedGroupIdReturned)
          when(mockEnrolmentConnector.enrolAndActivate(any())(using any())).thenReturn(Future.failed(models.InternalIssueError))
          when(mockUserAnswersConnectors.getUserAnswer(any())(using any())).thenReturn(Future.successful(Some(userAnswer)))
          val result = service.createSubscription(userAnswer)
          result.failed.futureValue mustBe models.DuplicateSubmissionError
        }
      }

      "throw an exception if the upeSafeId is equal to the nfmSafeId" in {
        val userAnswer = emptyUserAnswers
          .setOrException(UpeRegisteredInUKPage, false)
          .setOrException(FmRegisteredInUKPage, false)
          .setOrException(NominateFilingMemberPage, true)
        val application = applicationBuilder(userAnswers = Some(userAnswer))
          .overrides(
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        val service = application.injector.instanceOf[SubscriptionService]
        running(application) {
          when(mockRegistrationConnector.registerUltimateParent(any())(using any())).thenReturn(Future.successful("DuplicateID"))
          when(mockRegistrationConnector.registerFilingMember(any())(using any())).thenReturn(Future.successful("DuplicateID"))
          when(mockUserAnswersConnectors.save(any(), any())(using any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
          when(mockUserAnswersConnectors.getUserAnswer(any())(using any())).thenReturn(Future.successful(Some(userAnswer)))

          val result = service.createSubscription(userAnswer)
          result.failed.futureValue mustBe models.DuplicateSafeIdError
        }
      }

    }

    "cacheSubscription" when {
      "return SubscriptionData when the connector returns valid data" in {
        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()
        running(application) {
          when(mockSubscriptionConnector.cacheSubscription(any[ReadSubscriptionRequestParameters])(using any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(subscriptionData))
          val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
          val result = service.cacheSubscription(ReadSubscriptionRequestParameters(id, plrReference)).futureValue

          result mustBe subscriptionData
        }
      }

      "return InternalIssueError when the connector returns an error" in {
        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()
        running(application) {
          when(mockSubscriptionConnector.cacheSubscription(any[ReadSubscriptionRequestParameters])(using any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.failed(InternalIssueError))
          val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
          val result = service.cacheSubscription(ReadSubscriptionRequestParameters(id, plrReference)).failed.futureValue

          result mustBe InternalIssueError
        }
      }
    }

    "readSubscription" when {

      "return SubscriptionData object when the connector returns valid data and transformation is successful" in {

        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()
        running(application) {
          when(mockSubscriptionConnector.readSubscription(any())(using any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(Some(subscriptionData)))
          val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
          val result = service.readSubscription("plr").futureValue

          result mustBe subscriptionData
        }
      }

      "return NoResultFound when the connector returns a 404 response" in {
        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()
        running(application) {
          when(mockSubscriptionConnector.readSubscription(any())(using any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(None))
          val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
          val result = service.readSubscription("plr").failed.futureValue

          result mustBe models.NoResultFound
        }
      }

      "handle exceptions thrown by the connector" in {

        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()

        running(application) {
          when(mockSubscriptionConnector.readSubscription(any())(using any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.failed(new RuntimeException("Connection error")))
          val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
          val resultFuture = service.readSubscription("plr")

          resultFuture.failed.futureValue shouldBe a[RuntimeException]
        }
      }

    }

    "maybeReadSubscription" when {

      "return Some(SubscriptionData) when the connector returns valid data" in {
        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()
        running(application) {
          when(mockSubscriptionConnector.readSubscription(any())(using any(), any()))
            .thenReturn(Future.successful(Some(subscriptionData)))
          val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
          val result = service.maybeReadSubscription(plrReference).futureValue

          result mustBe Some(subscriptionData)
        }
      }

      "return None when the connector returns None" in {
        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()
        running(application) {
          when(mockSubscriptionConnector.readSubscription(any())(using any(), any()))
            .thenReturn(Future.successful(None))
          val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
          val result = service.maybeReadSubscription(plrReference).futureValue

          result mustBe None
        }
      }

      "handle exceptions thrown by the connector" in {
        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()

        running(application) {
          when(mockSubscriptionConnector.readSubscription(any())(using any(), any()))
            .thenReturn(Future.failed(new RuntimeException("Connection error")))
          val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
          val resultFuture = service.maybeReadSubscription(plrReference)

          resultFuture.failed.futureValue shouldBe a[RuntimeException]
        }
      }
    }

    "amendSubscription" when {
      "call read subscription and create the required amend object to submit when no secondary contact" in {

        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()
        running(application) {
          when(mockSubscriptionConnector.readSubscription(any())(using any(), any())).thenReturn(Future.successful(Some(subscriptionData)))
          when(mockSubscriptionConnector.amendSubscription(any(), any[AmendSubscription])(using any[HeaderCarrier]))
            .thenReturn(Future.successful(Done))
          val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
          val result = service.amendContactOrGroupDetails("id", "plr", emptySubscriptionLocalData).futureValue

          result mustBe Done
        }
      }
      "return InternalIssueError when the connector returns None" in {

        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()
        running(application) {
          when(mockSubscriptionConnector.readSubscription(any())(using any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.failed(InternalIssueError))
          val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
          val result = service.amendContactOrGroupDetails("id", "plr", emptySubscriptionLocalData).failed.futureValue

          result mustBe InternalIssueError
        }
      }
      "handle exceptions thrown by the connector" in {

        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        running(application) {
          when(mockSubscriptionConnector.readSubscription(any())(using any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.failed(new RuntimeException("Connection error")))

          val resultFuture = service.amendContactOrGroupDetails("id", "plr", emptySubscriptionLocalData)

          resultFuture.failed.futureValue shouldBe a[RuntimeException]
        }
      }
    }

    "createAmendObject" when {
      "create the right object when secondary contact is nominated" in {
        val service: SubscriptionService = app.injector.instanceOf[SubscriptionService]
        val newLocalData = emptySubscriptionLocalData.set(SubAddSecondaryContactPage, true).success.value
        val resultFuture = service.amendGroupOrContactDetails("plr", subscriptionData, newLocalData)
        resultFuture.secondaryContactDetails mustBe None
      }
      "create the right object when no secondary detail is nominated" in {
        val service: SubscriptionService = app.injector.instanceOf[SubscriptionService]
        val resultFuture = service.amendGroupOrContactDetails("plr", subscriptionData, emptySubscriptionLocalData)
        resultFuture.secondaryContactDetails mustBe None
      }
    }

    "amendFilingMemberDetails" when {
      "return done if the amend subscription is successful and delete userAnswers" in {
        when(mockUserAnswersConnectors.remove(any())(using any())).thenReturn(Future.successful(Done))

        val userAnswers = emptyUserAnswers.setOrException(RfmUkBasedPage, true)
        val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        when(mockSubscriptionConnector.amendSubscription(any(), any())(using any())).thenReturn(Future.successful(Done))
        val service: SubscriptionService = application.injector().instanceOf[SubscriptionService]

        service.amendFilingMemberDetails("id", amendData).futureValue mustEqual Done
      }

      "return failure if amend subscription fails" in {
        val application = applicationBuilder().overrides(
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        when(mockSubscriptionConnector.amendSubscription(any(), any())(using any())).thenReturn(Future.failed(InternalIssueError))
        val service: SubscriptionService = application.injector().instanceOf[SubscriptionService]

        service.amendFilingMemberDetails("id", amendData).failed.futureValue mustEqual InternalIssueError
      }
    }

    "deallocateEnrolment" when {
      "get old filing member group id from tax enrolment and use that to deallocate pillar 2 enrolment" in {
        val application = applicationBuilder().overrides(
          bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector),
          bind[TaxEnrolmentConnector].toInstance(mockTaxEnrolmentConnector)
        )
        when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(using any())).thenReturn(expectedGroupIdReturned)
        when(mockTaxEnrolmentConnector.revokeEnrolment(any(), any())(using any())).thenReturn(Future.successful(Done))
        val service: SubscriptionService = application.injector().instanceOf[SubscriptionService]

        service.deallocateEnrolment("plrReference").futureValue mustEqual Done
      }

      "call service enrolment connector and returns failure if no group id is returned" in {
        val application = applicationBuilder().overrides(
          bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector),
          bind[TaxEnrolmentConnector].toInstance(mockTaxEnrolmentConnector)
        )
        when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(using any())).thenReturn(Future.successful(None))
        when(mockTaxEnrolmentConnector.revokeEnrolment(any(), any())(using any())).thenReturn(Future.successful(Done))
        val service: SubscriptionService = application.injector().instanceOf[SubscriptionService]

        service.deallocateEnrolment("plrReference").failed.futureValue mustEqual models.InternalIssueError
      }

      "call tax enrolment connector and returns failure if revoking group enrolment fails" in {
        val application = applicationBuilder().overrides(
          bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector),
          bind[TaxEnrolmentConnector].toInstance(mockTaxEnrolmentConnector)
        )
        when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(using any())).thenReturn(expectedGroupIdReturned)
        when(mockTaxEnrolmentConnector.revokeEnrolment(any(), any())(using any())).thenReturn(Future.failed(InternalIssueError))
        val service: SubscriptionService = application.injector().instanceOf[SubscriptionService]

        service.deallocateEnrolment("plrReference").failed.futureValue mustEqual models.InternalIssueError
      }
    }

    "allocateEnrolment" when {
      val enrolmentInfo = AllocateEnrolmentParameters(userId = "id", verifiers = Seq(Verifier("nonUkPostCode", "somePostCode")))
      "return done if tax enrolment has successfully allocated an enrolment to a group" in {
        val application = applicationBuilder().overrides(
          bind[TaxEnrolmentConnector].toInstance(mockTaxEnrolmentConnector)
        )
        when(mockTaxEnrolmentConnector.allocateEnrolment(any(), any(), any[AllocateEnrolmentParameters])(using any()))
          .thenReturn(Future.successful(Done))
        val service: SubscriptionService = application.injector().instanceOf[SubscriptionService]

        service.allocateEnrolment("groupdID", "plr", enrolmentInfo).futureValue mustEqual Done
      }

      "return failure object if enrolment allocation fails in tax enrolment" in {
        val application = applicationBuilder().overrides(
          bind[TaxEnrolmentConnector].toInstance(mockTaxEnrolmentConnector)
        )
        when(mockTaxEnrolmentConnector.allocateEnrolment(any(), any(), any[AllocateEnrolmentParameters])(using any()))
          .thenReturn(Future.failed(InternalIssueError))
        val service: SubscriptionService = application.injector().instanceOf[SubscriptionService]

        service.allocateEnrolment("groupdID", "plr", enrolmentInfo).failed.futureValue mustEqual models.InternalIssueError
      }
    }

    "getUltimateParentEnrolmentInformation" when {
      "get ultimate parent verifiers from subscription data if ultimate was registered via GRS" in {
        val grsRegisteredSubData = subscriptionData.copy(upeDetails =
          subscriptionData.upeDetails.copy(customerIdentification1 = Some("Crn"), customerIdentification2 = Some("Utr"))
        )
        val service: SubscriptionService = app.injector.instanceOf[SubscriptionService]
        val result = service.getUltimateParentEnrolmentInformation(grsRegisteredSubData, "plrId", "id")
        result.futureValue mustBe allocateEnrolmentParameters
      }
      "get ultimate parent verifiers via a call to tax enrolment if no Crn or UTR can be found in subcription data" in {
        val knownFactsResponse = Future.successful(
          KnownFactsResponse(enrolments =
            Seq(EnrolmentRequest(identifiers = Seq(Identifier("PLRID", "plrId")), verifiers = Seq(Verifier("CTUTR", "Utr"), Verifier("CRN", "Crn"))))
          )
        )
        val application = applicationBuilder().overrides(
          bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
        )
        when(mockEnrolmentStoreProxyConnector.getKnownFacts(any[KnownFactsParameters])(using any())).thenReturn(knownFactsResponse)
        val service: SubscriptionService = application.injector().instanceOf[SubscriptionService]
        val result = service.getUltimateParentEnrolmentInformation(subscriptionData, "plrId", "id")
        result.futureValue mustBe allocateEnrolmentParameters
      }

      "return failed result if call to enrolment store fails" in {
        val application = applicationBuilder().overrides(
          bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
        )
        when(mockEnrolmentStoreProxyConnector.getKnownFacts(any[KnownFactsParameters])(using any())).thenReturn(Future.failed(InternalIssueError))
        val service: SubscriptionService = application.injector().instanceOf[SubscriptionService]
        val result = service.getUltimateParentEnrolmentInformation(subscriptionData, "plrId", "id")
        result.failed.futureValue mustEqual InternalIssueError
      }
    }

    "createAmendObjectForReplacingFilingMember" when {
      "set ultimate parent as the new filing member if user has chosen corporate position as upe" in {
        val service: SubscriptionService = app.injector.instanceOf[SubscriptionService]
        val expectedResult = amendData.copy(upeDetails = amendData.upeDetails.copy(filingMember = true), filingMemberDetails = None)
        val result         = service.createAmendObjectForReplacingFilingMember(subscriptionData, replaceFilingMemberData, emptyUserAnswers)
        result.futureValue mustEqual expectedResult
      }
      "collate all relevant information for new filing member detail if they are uk based" in {
        val userAnswers = emptyUserAnswers.setOrException(RfmUkBasedPage, true).setOrException(RfmGrsDataPage, rfmGrsData)
        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[RegistrationConnector].toInstance(mockRegistrationConnector),
              bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
            )
            .build()
        when(mockUserAnswersConnectors.save(any(), any())(using any())).thenReturn(Future.successful(Json.obj()))
        when(mockRegistrationConnector.registerNewFilingMember(any())(using any())).thenReturn(Future.successful("someSafeId"))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        val expectedResult = amendData.copy(upeDetails = amendData.upeDetails.copy(filingMember = false))
        val result         = service.createAmendObjectForReplacingFilingMember(
          subscriptionData,
          replaceFilingMemberData.copy(corporatePosition = CorporatePosition.NewNfm),
          userAnswers
        )
        result.futureValue mustEqual expectedResult
      }
      "collate all relevant information for new filing member detail and register them if they are not uk based" in {
        val userAnswers = emptyUserAnswers.setOrException(RfmUkBasedPage, false).setOrException(RfmNameRegistrationPage, "Company")
        val application = applicationBuilder()
          .overrides(
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        when(mockRegistrationConnector.registerNewFilingMember(any())(using any())).thenReturn(Future.successful("someSafeId"))
        when(mockUserAnswersConnectors.save(any(), any())(using any())).thenReturn(Future.successful(Json.obj()))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        val expectedResult = amendData.copy(
          upeDetails = amendData.upeDetails.copy(filingMember = false),
          filingMemberDetails = Some(
            FilingMemberAmendDetails(
              addNewFilingMember = true,
              safeId = "someSafeId",
              customerIdentification1 = None,
              customerIdentification2 = None,
              organisationName = "Company"
            )
          )
        )
        val result = service.createAmendObjectForReplacingFilingMember(
          subscriptionData,
          replaceFilingMemberData.copy(corporatePosition = CorporatePosition.NewNfm),
          userAnswers
        )
        result.futureValue mustEqual expectedResult
      }

      "fetch the rfm safe ID from the database if it exists" in {
        val userAnswers = emptyUserAnswers
          .setOrException(RfmUkBasedPage, false)
          .setOrException(RfmNameRegistrationPage, "Company")
          .setOrException(RfmSafeIdPage, "someSafeId")
        val application = applicationBuilder()
          .overrides(
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        when(mockUserAnswersConnectors.save(any(), any())(using any())).thenReturn(Future.successful(Json.obj()))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        val expectedResult = amendData.copy(
          upeDetails = amendData.upeDetails.copy(filingMember = false),
          filingMemberDetails = Some(
            FilingMemberAmendDetails(
              addNewFilingMember = true,
              safeId = "someSafeId",
              customerIdentification1 = None,
              customerIdentification2 = None,
              organisationName = "Company"
            )
          )
        )
        val result = service.createAmendObjectForReplacingFilingMember(
          subscriptionData,
          replaceFilingMemberData.copy(corporatePosition = CorporatePosition.NewNfm),
          userAnswers
        )
        result.futureValue mustEqual expectedResult
        verify(mockUserAnswersConnectors).save(eqTo(userAnswers.id), eqTo(userAnswers.data))(using any())
      }
      "throw an exception if new fm corporate position is chosen but no RfmUkBasedPage value can be found" in {
        val userAnswers = emptyUserAnswers
          .setOrException(RfmNameRegistrationPage, "Company")
          .setOrException(RfmSafeIdPage, "someSafeId")
        val application = applicationBuilder(Some(userAnswers))
          .overrides(
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]

        val exception = intercept[Exception] {
          service.createAmendObjectForReplacingFilingMember(
            subscriptionData,
            replaceFilingMemberData.copy(corporatePosition = CorporatePosition.NewNfm),
            userAnswers
          )
        }
        exception.getMessage must include("RfmUkBased")
      }

      "throw failure if registering non-uk based filing member fails" in {
        val userAnswers = emptyUserAnswers.setOrException(RfmUkBasedPage, false).setOrException(RfmNameRegistrationPage, "Company")
        val application = applicationBuilder().overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector)).build()
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        when(mockRegistrationConnector.registerNewFilingMember(any())(using any())).thenReturn(Future.failed(InternalIssueError))
        val result = service.createAmendObjectForReplacingFilingMember(
          subscriptionData,
          replaceFilingMemberData.copy(corporatePosition = CorporatePosition.NewNfm),
          userAnswers
        )
        result.failed.futureValue mustEqual models.InternalIssueError
      }
    }

    "getCompanyName" when {
      "return the company name if it is stored in the session" in {
        val companyName = "TestCompany"
        val userAnswers = emptyUserAnswers
          .setOrException(FmNameRegistrationPage, companyName)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()

        when(mockUserAnswersConnectors.getUserAnswer(any())(using any())).thenReturn(Future.successful(Some(userAnswers)))

        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        val result = service.getCompanyName(userAnswers)

        result shouldEqual Right(companyName)
      }
      "return an error redirect if the retrieval of the company name fails" in {
        val userAnswers = emptyUserAnswers

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()

        when(mockUserAnswersConnectors.getUserAnswer(any())(using any())).thenReturn(Future.successful(Some(userAnswers)))

        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        val result = service.getCompanyName(userAnswers)

        result shouldEqual Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

    "getCompanyNameFromGRS" when {
      "return the company name if it is stored in the session" in {
        val date        = LocalDate.now()
        val grsResponse = GrsResponse(
          Some(
            IncorporatedEntityRegistrationData(
              companyProfile = CompanyProfile(
                companyName = "ABC Limited",
                companyNumber = "1234",
                dateOfIncorporation = Some(date),
                unsanitisedCHROAddress = IncorporatedEntityAddress(address_line_1 = Some("line 1"), None, None, None, None, None, None, None)
              ),
              ctutr = "1234567890",
              identifiersMatch = true,
              businessVerification = None,
              registration = GrsRegistrationResult(
                registrationStatus = RegistrationStatus.Registered,
                registeredBusinessPartnerId = Some("XB0000000000001"),
                failures = None
              )
            )
          )
        )
        val userAnswers = emptyUserAnswers
          .setOrException(FmGRSResponsePage, grsResponse)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()

        when(mockUserAnswersConnectors.getUserAnswer(any())(using any())).thenReturn(Future.successful(Some(userAnswers)))

        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        val result = service.getCompanyNameFromGRS(grsResponse)

        result shouldEqual Some("ABC Limited")
      }
      "return an error redirect if the retrieval of the company name fails" in {
        val grsResponse = GrsResponse(None, None)

        val userAnswers = emptyUserAnswers
          .setOrException(FmGRSResponsePage, grsResponse)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()

        when(mockUserAnswersConnectors.getUserAnswer(any())(using any())).thenReturn(Future.successful(Some(userAnswers)))

        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        val result = service.getCompanyNameFromGRS(grsResponse)

        result shouldEqual None
      }
    }

    "matchingPillar2Records" when {
      val registrationDate = LocalDate.now()
      "return true if the pillar2 and reg date records in FE and BE database match" in {
        val userAnswers = emptyUserAnswers
          .setOrException(RfmPillar2ReferencePage, "matchingPillar2Id")
          .setOrException(RfmRegistrationDatePage, registrationDate)
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()
        when(mockUserAnswersConnectors.getUserAnswer(any())(using any())).thenReturn(Future.successful(Some(userAnswers)))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        val result = service.matchingPillar2Records("id", "matchingPillar2Id", registrationDate)
        result.futureValue mustEqual true
      }
      "return false if pillar2 records in FE and BE database do not match" in {
        val userAnswers = emptyUserAnswers.setOrException(RfmPillar2ReferencePage, "pillar2Backend")
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()
        when(mockUserAnswersConnectors.getUserAnswer(any())(using any())).thenReturn(Future.successful(Some(userAnswers)))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        val result = service.matchingPillar2Records("id", "pillar2Frontend", registrationDate)
        result.futureValue mustEqual false
      }
      "return false if no data can be found in the BE database" in {
        val application = applicationBuilder()
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()
        when(mockUserAnswersConnectors.getUserAnswer(any())(using any())).thenReturn(Future.successful(None))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        val result = service.matchingPillar2Records("id", "pillar2Frontend", registrationDate)
        result.futureValue mustEqual false
      }
      "return failed results if call to BE database fails" in {
        val application = applicationBuilder()
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()
        when(mockUserAnswersConnectors.getUserAnswer(any())(using any())).thenReturn(Future.failed(InternalIssueError))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        val result = service.matchingPillar2Records("id", "pillar2Frontend", registrationDate)
        result.failed.futureValue mustEqual InternalIssueError
      }

    }

    "readSubscriptionV2AndSave" when {

      val plrRef = "XEPLR0000000001"

      val v2Period = models.subscription.AccountingPeriodV2(
        startDate = LocalDate.of(2024, 1, 6),
        endDate = LocalDate.of(2025, 4, 6),
        dueDate = LocalDate.of(2024, 4, 6),
        canAmendStartDate = true,
        canAmendEndDate = true
      )

      val v2Data = models.subscription.SubscriptionDataV2(
        formBundleNumber = "119000004323",
        upeDetails = models.subscription.UpeDetails(
          safeId = None,
          customerIdentification1 = None,
          customerIdentification2 = None,
          organisationName = "Org Ltd",
          registrationDate = LocalDate.of(2024, 1, 31),
          domesticOnly = true,
          filingMember = false
        ),
        upeCorrespAddressDetails = models.subscription.UpeCorrespAddressDetails(
          addressLine1 = "1 High St",
          addressLine2 = None,
          addressLine3 = None,
          addressLine4 = None,
          postCode = None,
          countryCode = "GB"
        ),
        primaryContactDetails = models.subscription.ContactDetailsType("Contact", None, "c@example.com"),
        secondaryContactDetails = None,
        filingMemberDetails = None,
        accountingPeriod = Seq(v2Period),
        accountStatus = None
      )

      val application = applicationBuilder()
        .overrides(bind[SubscriptionConnector].toInstance(mockSubscriptionConnector))
        .build()
      val service = application.injector.instanceOf[SubscriptionService]

      "fetch V2 data from connector, convert to SubscriptionLocalData, save and return it" in
        running(application) {
          when(mockSubscriptionConnector.readSubscriptionV2(eqTo("id"), eqTo(plrRef))(using any(), any()))
            .thenReturn(Future.successful(v2Data))
          when(mockSubscriptionConnector.save(eqTo("id"), any())(using any()))
            .thenReturn(Future.successful(play.api.libs.json.Json.obj()))

          val result = service.readSubscriptionV2AndSave("id", plrRef).futureValue
          result.plrReference mustBe plrRef
          result.accountingPeriods mustBe Some(Seq(v2Period))
          result.subAccountingPeriod mustBe None
          result.organisationName mustBe Some("Org Ltd")
          result.registrationDate mustBe Some(LocalDate.of(2024, 1, 31))
        }

      "populate UPE identification and filing member fields from V2 data" in {
        val v2DataWithDetails = v2Data.copy(
          upeDetails = v2Data.upeDetails.copy(
            customerIdentification1 = Some("CRN123"),
            customerIdentification2 = Some("UTR456")
          ),
          filingMemberDetails = Some(
            models.subscription.FilingMemberDetails(
              safeId = "XL6967739016188",
              customerIdentification1 = Some("FM_CRN"),
              customerIdentification2 = Some("FM_UTR"),
              organisationName = "Filing Member Ltd"
            )
          )
        )
        running(application) {
          when(mockSubscriptionConnector.readSubscriptionV2(eqTo("id"), eqTo(plrRef))(using any(), any()))
            .thenReturn(Future.successful(v2DataWithDetails))
          when(mockSubscriptionConnector.save(eqTo("id"), any())(using any()))
            .thenReturn(Future.successful(play.api.libs.json.Json.obj()))

          val result = service.readSubscriptionV2AndSave("id", plrRef).futureValue
          result.upeCustomerIdentification1 mustBe Some("CRN123")
          result.upeCustomerIdentification2 mustBe Some("UTR456")
          result.upeFilingMember mustBe Some(false)
          result.filingMemberDetails mustBe Some(
            models.subscription.FilingMemberDetails("XL6967739016188", Some("FM_CRN"), Some("FM_UTR"), "Filing Member Ltd")
          )
        }
      }

      "propagate failure when save fails" in
        running(application) {
          when(mockSubscriptionConnector.readSubscriptionV2(eqTo("id"), eqTo(plrRef))(using any(), any()))
            .thenReturn(Future.successful(v2Data))
          when(mockSubscriptionConnector.save(eqTo("id"), any())(using any()))
            .thenReturn(Future.failed(InternalIssueError))

          service.readSubscriptionV2AndSave("id", plrRef).failed.futureValue mustBe InternalIssueError
        }

      "propagate failure when readSubscriptionV2 fails" in
        running(application) {
          when(mockSubscriptionConnector.readSubscriptionV2(any(), any())(using any(), any()))
            .thenReturn(Future.failed(InternalIssueError))

          service.readSubscriptionV2AndSave("id", plrRef).failed.futureValue mustBe InternalIssueError
        }
    }

    "amendAccountingPeriods" when {

      val plrRef = "XEPLR0000000001"

      val affectedPeriod = AccountingPeriodV2(
        startDate = LocalDate.of(2024, 1, 1),
        endDate = LocalDate.of(2024, 12, 31),
        dueDate = LocalDate.of(2025, 3, 31),
        canAmendStartDate = true,
        canAmendEndDate = true
      )

      val newPeriod = AccountingPeriod(
        startDate = LocalDate.of(2024, 6, 1),
        endDate = LocalDate.of(2025, 5, 31)
      )

      val updatedV2Period = AccountingPeriodV2(
        startDate = LocalDate.of(2024, 6, 1),
        endDate = LocalDate.of(2025, 5, 31),
        dueDate = LocalDate.of(2025, 9, 30),
        canAmendStartDate = true,
        canAmendEndDate = true
      )

      val updatedV2Data = SubscriptionDataV2(
        formBundleNumber = "119000004323",
        upeDetails = UpeDetails(None, None, None, "Org Ltd", LocalDate.of(2024, 1, 31), domesticOnly = true, filingMember = false),
        upeCorrespAddressDetails = UpeCorrespAddressDetails("1 High St", None, None, None, None, "GB"),
        primaryContactDetails = ContactDetailsType("Contact", None, "c@example.com"),
        secondaryContactDetails = None,
        filingMemberDetails = None,
        accountingPeriod = Seq(updatedV2Period),
        accountStatus = None
      )

      val application = applicationBuilder()
        .overrides(bind[SubscriptionConnector].toInstance(mockSubscriptionConnector))
        .build()
      val service = application.injector.instanceOf[SubscriptionService]

      "call amendSubscriptionV2, re-read v2, and return updated periods" in
        running(application) {
          when(mockSubscriptionConnector.amendSubscriptionV2(any(), any[AmendSubscriptionV2])(using any[HeaderCarrier]))
            .thenReturn(Future.successful(Done))
          when(mockSubscriptionConnector.readSubscriptionV2(eqTo("id"), eqTo(plrRef))(using any(), any()))
            .thenReturn(Future.successful(updatedV2Data))
          when(mockSubscriptionConnector.save(eqTo("id"), any())(using any()))
            .thenReturn(Future.successful(Json.obj()))

          service.amendAccountingPeriods("id", plrRef, emptySubscriptionLocalData, Seq(affectedPeriod), newPeriod).futureValue mustBe Seq(
            updatedV2Period
          )
        }

      "propagate failure when amendSubscriptionV2 fails" in
        running(application) {
          when(mockSubscriptionConnector.amendSubscriptionV2(any(), any[AmendSubscriptionV2])(using any[HeaderCarrier]))
            .thenReturn(Future.failed(UnexpectedResponse))

          service
            .amendAccountingPeriods("id", plrRef, emptySubscriptionLocalData, Seq(affectedPeriod), newPeriod)
            .failed
            .futureValue mustBe UnexpectedResponse
        }

      "propagate failure when re-read after amend fails" in
        running(application) {
          when(mockSubscriptionConnector.amendSubscriptionV2(any(), any[AmendSubscriptionV2])(using any[HeaderCarrier]))
            .thenReturn(Future.successful(Done))
          when(mockSubscriptionConnector.readSubscriptionV2(any(), any())(using any(), any()))
            .thenReturn(Future.failed(InternalIssueError))

          service
            .amendAccountingPeriods("id", plrRef, emptySubscriptionLocalData, Seq(affectedPeriod), newPeriod)
            .failed
            .futureValue mustBe InternalIssueError
        }
    }
  }
}
