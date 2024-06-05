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
import connectors._
import models.EnrolmentRequest.{AllocateEnrolmentParameters, KnownFactsParameters, KnownFactsResponse}
import models.registration.RegistrationInfo
import models.rfm.CorporatePosition
import models.subscription._
import models.{EnrolmentRequest, GroupIds, Identifier, InternalIssueError, Verifier}
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{never, verify, when}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionServiceSpec extends SpecBase {

  val id           = "testId"
  val plrReference = "testPlrRef"
  val expectedGroupIdReturned: Future[Some[GroupIds]] =
    Future.successful(Some(GroupIds(principalGroupIds = "groupID", delegatedGroupIds = Seq.empty)))
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
          when(mockRegistrationConnector.registerUltimateParent(any())(any())).thenReturn(Future.successful("upeID"))
          when(mockRegistrationConnector.registerFilingMember(any())(any())).thenReturn(Future.successful("fmID"))
          when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentConnector.enrolAndActivate(any())(any())).thenReturn(Future.successful(Done))
          when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(any())).thenReturn(Future.successful(None))
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))

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
          when(mockRegistrationConnector.registerUltimateParent(any())(any())).thenReturn(Future.successful("upeID"))
          when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentConnector.enrolAndActivate(any())(any())).thenReturn(Future.successful(Done))
          when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(any())).thenReturn(Future.successful(None))
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))

          val result = service.createSubscription(userAnswer)
          result.futureValue mustBe "ID"
        }
      }

      "Return success response and do not call register connector when non uk upe and fm is already set" in {
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
          when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentConnector.enrolAndActivate(any())(any())).thenReturn(Future.successful(Done))
          when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(any())).thenReturn(Future.successful(None))
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))

          val result = service.createSubscription(userAnswer)

          verify(mockRegistrationConnector, never()).registerUltimateParent(any())(any())
          verify(mockRegistrationConnector, never()).registerFilingMember(any())(any())

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
          when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentConnector.enrolAndActivate(any())(any())).thenReturn(Future.successful(Done))
          when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(any())).thenReturn(Future.successful(None))
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
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
          when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentConnector.enrolAndActivate(any())(any())).thenReturn(Future.successful(Done))
          when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(any())).thenReturn(Future.successful(None))
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
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
          when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.failed(models.InternalIssueError))
          when(mockEnrolmentConnector.enrolAndActivate(any())(any())).thenReturn(Future.successful(Done))
          when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(any())).thenReturn(Future.successful(None))
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
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
          when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentConnector.enrolAndActivate(any())(any())).thenReturn(Future.failed(models.InternalIssueError))
          when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(any())).thenReturn(Future.successful(None))
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
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
          when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful("ID"))
          when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(any())).thenReturn(expectedGroupIdReturned)
          when(mockEnrolmentConnector.enrolAndActivate(any())(any())).thenReturn(Future.failed(models.InternalIssueError))
          when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswer)))
          val result = service.createSubscription(userAnswer)
          result.failed.futureValue mustBe models.DuplicateSubmissionError
        }
      }

    }

    "readAndCacheSubscription" when {

      "return SubscriptionData object when the connector returns valid data and transformation is successful" in {
        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()
        running(application) {
          when(mockSubscriptionConnector.readSubscriptionAndCache(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(Some(subscriptionData)))
          val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
          val result = service.readAndCacheSubscription(ReadSubscriptionRequestParameters(id, plrReference)).futureValue

          result mustBe subscriptionData
        }
      }

      "return InternalIssueError when the connector returns None" in {
        implicit val hc: HeaderCarrier = HeaderCarrier()
        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()
        running(application) {
          when(mockSubscriptionConnector.readSubscriptionAndCache(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(None))
          val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
          val result = service.readAndCacheSubscription(ReadSubscriptionRequestParameters(id, plrReference)).failed.futureValue

          result mustBe models.InternalIssueError
        }
      }

      "handle exceptions thrown by the connector" in {

        val requestParameters = ReadSubscriptionRequestParameters(id, plrReference)
        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()

        running(application) {
          when(mockSubscriptionConnector.readSubscriptionAndCache(any[ReadSubscriptionRequestParameters])(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.failed(new RuntimeException("Connection error")))
          val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
          val resultFuture = service.readAndCacheSubscription(requestParameters)

          resultFuture.failed.futureValue shouldBe a[RuntimeException]
        }
      }
    }

    "readSubscription" when {

      "return SubscriptionDAta object when the connector returns valid data and transformation is successful" in {

        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()
        running(application) {
          when(mockSubscriptionConnector.readSubscription(any())(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(Some(subscriptionData)))
          val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
          val result = service.readSubscription("plr").futureValue

          result mustBe subscriptionData
        }
      }

      "return InternalIssueError when the connector returns None" in {
        implicit val hc: HeaderCarrier = HeaderCarrier()
        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()
        running(application) {
          when(mockSubscriptionConnector.readSubscription(any())(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(None))
          val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
          val result = service.readSubscription("plr").failed.futureValue

          result mustBe models.InternalIssueError
        }
      }

      "handle exceptions thrown by the connector" in {

        val application = applicationBuilder()
          .overrides(
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
          )
          .build()

        running(application) {
          when(mockSubscriptionConnector.readSubscription(any())(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.failed(new RuntimeException("Connection error")))
          val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
          val resultFuture = service.readSubscription("plr")

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
          when(mockSubscriptionConnector.readSubscription(any())(any(), any())).thenReturn(Future.successful(Some(subscriptionData)))
          when(mockSubscriptionConnector.amendSubscription(any(), any[AmendSubscription])(any[HeaderCarrier]))
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
          when(mockSubscriptionConnector.readSubscription(any())(any[HeaderCarrier], any[ExecutionContext]))
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
          when(mockSubscriptionConnector.readSubscription(any())(any[HeaderCarrier], any[ExecutionContext]))
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
        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))

        val userAnswers = emptyUserAnswers.setOrException(RfmUkBasedPage, true)
        val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        when(mockSubscriptionConnector.amendSubscription(any(), any())(any())).thenReturn(Future.successful(Done))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]

        service.amendFilingMemberDetails("id", amendData).futureValue mustEqual Done
        verify(mockUserAnswersConnectors).remove(eqTo(emptyUserAnswers.id))(any())
      }

      "return failure if amend subscription fails" in {
        val application = applicationBuilder().overrides(
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        when(mockSubscriptionConnector.amendSubscription(any(), any())(any())).thenReturn(Future.failed(InternalIssueError))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]

        service.amendFilingMemberDetails("id", amendData).failed.futureValue mustEqual InternalIssueError
      }

      "return failure if removing data fails" in {

        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.failed(new RuntimeException("Connection error")))

        val userAnswers = emptyUserAnswers.setOrException(RfmUkBasedPage, true)
        val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
          bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
        )
        when(mockSubscriptionConnector.amendSubscription(any(), any())(any())).thenReturn(Future.successful(Done))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]

        service.amendFilingMemberDetails("id", amendData).failed.futureValue mustBe a[RuntimeException]
      }
    }
    "deallocateEnrolment" when {
      "get old filing member group id from tax enrolment and use that to deallocate pillar 2 enrolment" in {
        val application = applicationBuilder().overrides(
          bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector),
          bind[TaxEnrolmentConnector].toInstance(mockTaxEnrolmentConnector)
        )
        when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(any())).thenReturn(expectedGroupIdReturned)
        when(mockTaxEnrolmentConnector.revokeEnrolment(any(), any())(any())).thenReturn(Future.successful(Done))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]

        service.deallocateEnrolment("plrReference").futureValue mustEqual Done
      }

      "call service enrolment connector and returns failure if no group id is returned" in {
        val application = applicationBuilder().overrides(
          bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector),
          bind[TaxEnrolmentConnector].toInstance(mockTaxEnrolmentConnector)
        )
        when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(any())).thenReturn(Future.successful(None))
        when(mockTaxEnrolmentConnector.revokeEnrolment(any(), any())(any())).thenReturn(Future.successful(Done))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]

        service.deallocateEnrolment("plrReference").failed.futureValue mustEqual models.InternalIssueError
      }

      "call tax enrolment connector and returns failure if revoking group enrolment fails" in {
        val application = applicationBuilder().overrides(
          bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector),
          bind[TaxEnrolmentConnector].toInstance(mockTaxEnrolmentConnector)
        )
        when(mockEnrolmentStoreProxyConnector.getGroupIds(any())(any())).thenReturn(expectedGroupIdReturned)
        when(mockTaxEnrolmentConnector.revokeEnrolment(any(), any())(any())).thenReturn(Future.failed(InternalIssueError))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]

        service.deallocateEnrolment("plrReference").failed.futureValue mustEqual models.InternalIssueError
      }
    }

    "allocateEnrolment" when {
      val enrolmentInfo = AllocateEnrolmentParameters(userId = "id", verifiers = Seq(Verifier("nonUkPostCode", "somePostCode")))
      "return done if tax enrolment has successfully allocated an enrolment to a group" in {
        val application = applicationBuilder().overrides(
          bind[TaxEnrolmentConnector].toInstance(mockTaxEnrolmentConnector)
        )
        when(mockTaxEnrolmentConnector.allocateEnrolment(any(), any(), any[AllocateEnrolmentParameters])(any())).thenReturn(Future.successful(Done))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]

        service.allocateEnrolment("groupdID", "plr", enrolmentInfo).futureValue mustEqual Done
      }

      "return failure object if enrolment allocation fails in tax enrolment" in {
        val application = applicationBuilder().overrides(
          bind[TaxEnrolmentConnector].toInstance(mockTaxEnrolmentConnector)
        )
        when(mockTaxEnrolmentConnector.allocateEnrolment(any(), any(), any[AllocateEnrolmentParameters])(any()))
          .thenReturn(Future.failed(InternalIssueError))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]

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
        when(mockEnrolmentStoreProxyConnector.getKnownFacts(any[KnownFactsParameters])(any())).thenReturn(knownFactsResponse)
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        val result = service.getUltimateParentEnrolmentInformation(subscriptionData, "plrId", "id")
        result.futureValue mustBe allocateEnrolmentParameters
      }

      "return failed result if call to enrolment store fails" in {
        val application = applicationBuilder().overrides(
          bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
        )
        when(mockEnrolmentStoreProxyConnector.getKnownFacts(any[KnownFactsParameters])(any())).thenReturn(Future.failed(InternalIssueError))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
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
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))
        when(mockRegistrationConnector.registerNewFilingMember(any())(any())).thenReturn(Future.successful("someSafeId"))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        val expectedResult = amendData.copy(upeDetails = amendData.upeDetails.copy(filingMember = false))
        val result = service.createAmendObjectForReplacingFilingMember(
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
        when(mockRegistrationConnector.registerNewFilingMember(any())(any())).thenReturn(Future.successful("someSafeId"))
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))
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
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.obj()))
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
        verify(mockUserAnswersConnectors).save(eqTo(userAnswers.id), eqTo(userAnswers.data))(any())
      }
      "throw an exception if new fm corporate position is chosen but no RfmUkBasedPage value can be found" in {

        val userAnswers = emptyUserAnswers
          .setOrException(RfmUkBasedPage, false)
          .setOrException(RfmNameRegistrationPage, "Company")
          .setOrException(RfmSafeIdPage, "someSafeId")
        val application = applicationBuilder(Some(userAnswers))
          .overrides(
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]

        val result = service.createAmendObjectForReplacingFilingMember(
          subscriptionData,
          replaceFilingMemberData.copy(corporatePosition = CorporatePosition.NewNfm),
          userAnswers
        )
        result.map { e =>
          e mustEqual a[Exception]
        }
      }

      "throw failure if registering non-uk based filing member fails" in {
        val userAnswers = emptyUserAnswers.setOrException(RfmUkBasedPage, false).setOrException(RfmNameRegistrationPage, "Company")
        val application = applicationBuilder().overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector)).build()
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        when(mockRegistrationConnector.registerNewFilingMember(any())(any())).thenReturn(Future.failed(InternalIssueError))
        val result = service.createAmendObjectForReplacingFilingMember(
          subscriptionData,
          replaceFilingMemberData.copy(corporatePosition = CorporatePosition.NewNfm),
          userAnswers
        )
        result.failed.futureValue mustEqual models.InternalIssueError
      }
    }

    "matchingPillar2Records" when {
      "return true if the pillar2 records in FE and BE database match" in {
        val userAnswers = emptyUserAnswers.setOrException(RfmPillar2ReferencePage, "matchingPillar2Id")
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()
        when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswers)))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        val result = service.matchingPillar2Records("id", "matchingPillar2Id")
        result.futureValue mustEqual true
      }
      "return false if pillar2 records in FE and BE database do not match" in {
        val userAnswers = emptyUserAnswers.setOrException(RfmPillar2ReferencePage, "pillar2Backend")
        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()
        when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(userAnswers)))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        val result = service.matchingPillar2Records("id", "pillar2Frontend")
        result.futureValue mustEqual false
      }
      "return false if no data can be found in the BE database" in {
        val application = applicationBuilder()
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()
        when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(None))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        val result = service.matchingPillar2Records("id", "pillar2Frontend")
        result.futureValue mustEqual false
      }
      "return failed results if call to BE database fails" in {
        val application = applicationBuilder()
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()
        when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.failed(InternalIssueError))
        val service: SubscriptionService = application.injector.instanceOf[SubscriptionService]
        val result = service.matchingPillar2Records("id", "pillar2Frontend")
        result.failed.futureValue mustEqual InternalIssueError
      }
    }
  }
}
