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

package controllers.rfm

import base.SpecBase
import connectors.UserAnswersConnectors
import models.EnrolmentRequest.AllocateEnrolmentParameters
import models._
import models.rfm.CorporatePosition
import models.rfm.RfmStatus.{FailException, FailedInternalIssueError, SuccessfullyCompleted}
import models.subscription.{AmendSubscription, NewFilingMemberDetail, SubscriptionData}
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{atLeastOnce, verify, when}
import pages._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import services.audit.AuditService
import utils.FutureConverter.FutureOps
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate
import scala.concurrent.Future

class RfmContactCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Controller" must {

    "return OK and the correct view if an answer is provided to every New RFM ID journey questions - Upe" in {
      val sessionRepositoryUserAnswers = UserAnswers("id")
      val application = applicationBuilder(userAnswers = Some(rfmUpe))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()
      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionRepositoryUserAnswers)))
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK

        contentAsString(result) must include("Filing member details")
        contentAsString(result) must include("Ultimate Parent Entity (UPE)")
        contentAsString(result) must include("Primary contact")
        contentAsString(result) must include("Contact name")
        contentAsString(result) must include("Email address")
        contentAsString(result) must include("Phone number")
        contentAsString(result) must include("Second contact")
        contentAsString(result) must include("Second contact name")
        contentAsString(result) must include("Second contact email address")
        contentAsString(result) must include("Can we contact the secondary contact by phone?")
        contentAsString(result) must include("Second contact phone number")
        contentAsString(result) must include("Contact address")
        contentAsString(result) must include("Address")
        contentAsString(result) must include("the primary contact name")
        contentAsString(result) must include("the primary contact email address")
        contentAsString(result) must include("can we contact the primary contact by phone")
        contentAsString(result) must include("the phone number for the primary contact")
        contentAsString(result) must include("do you have a secondary contact")
        contentAsString(result) must include("the secondary contact name")
        contentAsString(result) must include("the secondary contact email address")
        contentAsString(result) must include("can we contact the secondary contact by phone")
        contentAsString(result) must include("the phone number for the secondary contact")
        contentAsString(result) must include("Do you need to keep a record of your answers?")
        contentAsString(result) must include("If you need to keep a record of your answers, you can:")
        contentAsString(result) must include("Print this page")
        contentAsString(result) must include("Download as PDF")
        contentAsString(result) must include("Now submit your details to replace the current filing member")
        contentAsString(result) must include(
          "By submitting these details, you are confirming that you are able to act as a new filing member for your group and the information is correct and complete to the best of your knowledge."
        )
      }
    }

    "return OK and the correct view if an answer is provided to every New RFM ID journey questions - NoId" in {
      val sessionRepositoryUserAnswers = UserAnswers("id")
      val application = applicationBuilder(userAnswers = Some(rfmNoID))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()
      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionRepositoryUserAnswers)))
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK

        contentAsString(result) must include("Filing member details")
        contentAsString(result) must include("New nominated filing member")
        contentAsString(result) must include("Name")
        contentAsString(result) must include("Address")
        contentAsString(result) must include("Primary contact")
        contentAsString(result) must include("Contact name")
        contentAsString(result) must include("Email address")
        contentAsString(result) must include("Phone number")
        contentAsString(result) must include("Secondary contact")
        contentAsString(result) must include("Second contact name")
        contentAsString(result) must include("Second contact email address")
        contentAsString(result) must include("Can we contact the secondary contact by phone?")
        contentAsString(result) must include("Second contact phone number")
        contentAsString(result) must include("Contact address")
        contentAsString(result) must include("Address")
        contentAsString(result) must include("the primary contact name")
        contentAsString(result) must include("the primary contact email address")
        contentAsString(result) must include("can we contact the primary contact by phone")
        contentAsString(result) must include("the phone number for the primary contact")
        contentAsString(result) must include("the phone number for the primary contact")
        contentAsString(result) must include("do you have a secondary contact")
        contentAsString(result) must include("the secondary contact name")
        contentAsString(result) must include("the secondary contact email address")
        contentAsString(result) must include("can we contact the secondary contact by phone")
        contentAsString(result) must include("the phone number for the secondary contact")
        contentAsString(result) must include("Do you need to keep a record of your answers?")
        contentAsString(result) must include("If you need to keep a record of your answers, you can:")
        contentAsString(result) must include("Print this page")
        contentAsString(result) must include("Download as PDF")
        contentAsString(result) must include("Now submit your details to replace the current filing member")
        contentAsString(result) must include(
          "By submitting these details, you are confirming that you are able to act as a new filing member for your group and the information is correct and complete to the best of your knowledge."
        )
      }
    }

    "return OK and the correct view if an answer is provided to every New RFM ID journey questions" in {
      val sessionRepositoryUserAnswers = UserAnswers("id")
      val application = applicationBuilder(userAnswers = Some(rfmID))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()
      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionRepositoryUserAnswers)))
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK

        contentAsString(result) must include("Filing member details")
        contentAsString(result) must include("New nominated filing member")
        contentAsString(result) must include("Company")
        contentAsString(result) must include("ABC Limited")
        contentAsString(result) must include("Company Registration Number")
        contentAsString(result) must include("1234")
        contentAsString(result) must include("Unique Taxpayer Reference")
        contentAsString(result) must include("Primary contact")
        contentAsString(result) must include("Contact name")
        contentAsString(result) must include("Email address")
        contentAsString(result) must include("Phone number")
        contentAsString(result) must include("Secondary contact")
        contentAsString(result) must include("Second contact name")
        contentAsString(result) must include("Second contact email address")
        contentAsString(result) must include("Can we contact the secondary contact by phone?")
        contentAsString(result) must include("Second contact phone number")
        contentAsString(result) must include("Contact address")
        contentAsString(result) must include("Address")
        contentAsString(result) must include("the primary contact name")
        contentAsString(result) must include("the primary contact email address")
        contentAsString(result) must include("can we contact the primary contact by phone")
        contentAsString(result) must include("the phone number for the primary contact")
        contentAsString(result) must include("the phone number for the primary contact")
        contentAsString(result) must include("do you have a secondary contact")
        contentAsString(result) must include("the secondary contact name")
        contentAsString(result) must include("the secondary contact email address")
        contentAsString(result) must include("can we contact the secondary contact by phone")
        contentAsString(result) must include("the phone number for the secondary contact")
        contentAsString(result) must include("Do you need to keep a record of your answers?")
        contentAsString(result) must include("If you need to keep a record of your answers, you can:")
        contentAsString(result) must include("Print this page")
        contentAsString(result) must include("Download as PDF")
        contentAsString(result) must include("Now submit your details to replace the current filing member")
        contentAsString(result) must include(
          "By submitting these details, you are confirming that you are able to act as a new filing member for your group and the information is correct and complete to the best of your knowledge."
        )
      }
    }

    "return OK and the correct view if an answer is provided to every New RFM No ID journey questions" in {
      val sessionRepositoryUserAnswers = UserAnswers("id")
      val application = applicationBuilder(userAnswers = Some(rfmNoID))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()
      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionRepositoryUserAnswers)))
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK

        contentAsString(result) must include("Filing member details")
        contentAsString(result) must include("New nominated filing member")
        contentAsString(result) must include("name")
        contentAsString(result) must include("Primary contact")
        contentAsString(result) must include("Contact name")
        contentAsString(result) must include("Email address")
        contentAsString(result) must include("Phone number")
        contentAsString(result) must include("Secondary contact")
        contentAsString(result) must include("Second contact name")
        contentAsString(result) must include("Second contact email address")
        contentAsString(result) must include("Can we contact the secondary contact by phone?")
        contentAsString(result) must include("Second contact phone number")
        contentAsString(result) must include("Contact address")
        contentAsString(result) must include("Address")
        contentAsString(result) must include("Do you need to keep a record of your answers?")
        contentAsString(result) must include("If you need to keep a record of your answers, you can:")
        contentAsString(result) must include("Print this page")
        contentAsString(result) must include("Download as PDF")
        contentAsString(result) must include("Now submit your details to replace the current filing member")
        contentAsString(result) must include(
          "By submitting these details, you are confirming that you are able to act as a new filing member for your group and the information is correct and complete to the best of your knowledge."
        )
      }
    }

    "redirect to 'cannot return after confirmation' page once nfm submitted successfully and user attempts to go back" in {
      val sessionRepositoryUserAnswers = UserAnswers("id").setOrException(PlrReferencePage, "someID")
      val application = applicationBuilder(None)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()
      running(application) {
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionRepositoryUserAnswers)))
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmCannotReturnAfterConfirmationController.onPageLoad.url
      }
    }

    "onSubmit" should {

      val defaultRfmData = rfmPrimaryAndSecondaryContactData
        .setOrException(RfmUkBasedPage, false)
        .setOrException(RfmPillar2ReferencePage, "plrReference")
        .setOrException(RfmRegistrationDatePage, LocalDate.now())
        .setOrException(RfmCorporatePositionPage, CorporatePosition.NewNfm)
        .setOrException(RfmNameRegistrationPage, "joe")
        .setOrException(RfmRegisteredAddressPage, NonUKAddress("line1", None, "line3", None, None, countryCode = "US"))

      lazy val incompleteData         = controllers.rfm.routes.RfmIncompleteDataController.onPageLoad.url
      val allocateEnrolmentParameters = AllocateEnrolmentParameters(userId = "id", verifiers = Seq(Verifier("postCode", "M199999"))).toFuture

      "redirect to waiting page in case of a successful replace filing member for upe and save the api response in the backend" ignore {
        val completeUserAnswers =
          defaultRfmData.setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)
        val sessionData = emptyUserAnswers
          .setOrException(RfmStatusPage, SuccessfullyCompleted)
          .setOrException(PlrReferencePage, "plrReference")
        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
        when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
        when(
          mockSubscriptionService.createAmendObjectForReplacingFilingMember(any[SubscriptionData], any[NewFilingMemberDetail], any[UserAnswers])(
            any()
          )
        ).thenReturn(Future.successful(amendData))
        when(mockSubscriptionService.amendFilingMemberDetails(any(), any[AmendSubscription])(any())).thenReturn(Future.successful(Done))
        when(mockSubscriptionService.deallocateEnrolment(any())(any())).thenReturn(Future.successful(Done))
        when(mockSubscriptionService.getUltimateParentEnrolmentInformation(any[SubscriptionData], any(), any())(any()))
          .thenReturn(allocateEnrolmentParameters)
        when(mockSubscriptionService.allocateEnrolment(any(), any(), any[AllocateEnrolmentParameters])(any())).thenReturn(Future.successful(Done))
        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))

        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmWaitingRoomController.onPageLoad().url
          verify(mockSessionRepository, atLeastOnce()).set(any[UserAnswers])
        }
      }

      "redirect to waiting page in case of a successful replace filing member for NewNfm and save the api response in the backend" ignore {
        val completeUserAnswers = defaultRfmData
          .setOrException(RfmCorporatePositionPage, CorporatePosition.NewNfm)
        val sessionData = emptyUserAnswers
          .setOrException(RfmStatusPage, SuccessfullyCompleted)
          .setOrException(PlrReferencePage, "plrReference")
        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[AuditService].toInstance(mockAuditService)
          )
          .build()
        when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
        when(
          mockSubscriptionService.createAmendObjectForReplacingFilingMember(any[SubscriptionData], any[NewFilingMemberDetail], any[UserAnswers])(
            any()
          )
        ).thenReturn(Future.successful(amendData))
        when(mockSubscriptionService.amendFilingMemberDetails(any(), any[AmendSubscription])(any())).thenReturn(Future.successful(Done))
        when(mockSubscriptionService.deallocateEnrolment(any())(any())).thenReturn(Future.successful(Done))
        when(mockSubscriptionService.getUltimateParentEnrolmentInformation(any[SubscriptionData], any(), any())(any()))
          .thenReturn(allocateEnrolmentParameters)
        when(mockSubscriptionService.allocateEnrolment(any(), any(), any[AllocateEnrolmentParameters])(any())).thenReturn(Future.successful(Done))
        when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))

        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmWaitingRoomController.onPageLoad().url
          verify(mockSessionRepository, atLeastOnce()).set(any[UserAnswers])
        }
      }

      "redirect to incomplete task error page if no contact detail is found for the new filing member" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual incompleteData
        }
      }

      "redirect to waiting page in case of a FailException, when no group ID is found for the new filing member, and save the api response in the backend" ignore {
        val ua = defaultRfmData.setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)
        val sessionData = emptyUserAnswers
          .setOrException(RfmStatusPage, FailException)
        val application = applicationBuilder(userAnswers = Some(ua), groupID = None)
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
        when(
          mockSubscriptionService.createAmendObjectForReplacingFilingMember(any[SubscriptionData], any[NewFilingMemberDetail], any[UserAnswers])(
            any()
          )
        ).thenReturn(Future.successful(amendData))
        when(mockSubscriptionService.amendFilingMemberDetails(any(), any[AmendSubscription])(any())).thenReturn(Future.successful(Done))
        when(mockSubscriptionService.deallocateEnrolment(any())(any())).thenReturn(Future.successful(Done))
        when(mockSubscriptionService.getUltimateParentEnrolmentInformation(any[SubscriptionData], any(), any())(any()))
          .thenReturn(allocateEnrolmentParameters)
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))

        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmWaitingRoomController.onPageLoad().url
          verify(mockSessionRepository).set(eqTo(sessionData))
        }
      }

      "redirect to waiting page in case of a FailException, when new filing member uk based page value cannot be found, and save the api response in the backend" ignore {
        val ua = rfmNoID
        val sessionData = emptyUserAnswers
          .setOrException(RfmStatusPage, FailException)
        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
        when(
          mockSubscriptionService.createAmendObjectForReplacingFilingMember(any[SubscriptionData], any[NewFilingMemberDetail], any[UserAnswers])(
            any()
          )
        ).thenReturn(Future.failed(new Exception("no rfm uk based")))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmWaitingRoomController.onPageLoad().url
          verify(mockSessionRepository).set(eqTo(sessionData))
        }
      }

      "redirect to waiting page in case of a InternalIssueError, if registering new filing member fails, and save the api response in the backend" ignore {
        val ua = rfmNoID
          .setOrException(RfmPillar2ReferencePage, "id")
          .setOrException(RfmRegistrationDatePage, LocalDate.now())
        val sessionData = emptyUserAnswers
          .setOrException(RfmStatusPage, FailedInternalIssueError)
        val application = applicationBuilder(userAnswers = Some(ua), groupID = Some("id"))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
        when(mockSubscriptionService.deallocateEnrolment(any())(any())).thenReturn(Future.successful(Done))
        when(mockSubscriptionService.getUltimateParentEnrolmentInformation(any[SubscriptionData], any(), any())(any()))
          .thenReturn(allocateEnrolmentParameters)
        when(mockSubscriptionService.allocateEnrolment(any(), any(), any[AllocateEnrolmentParameters])(any())).thenReturn(Future.successful(Done))
        when(
          mockSubscriptionService.createAmendObjectForReplacingFilingMember(any[SubscriptionData], any[NewFilingMemberDetail], any[UserAnswers])(
            any()
          )
        ).thenReturn(Future.failed(InternalIssueError))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))

        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmWaitingRoomController.onPageLoad().url
          verify(mockSessionRepository).set(eqTo(sessionData))
        }
      }

      "redirect to waiting page in case of a InternalIssueError, if deallocating old filing member fails, and save the api response in the backend" ignore {
        val ua = rfmNoID
          .setOrException(RfmPillar2ReferencePage, "id")
          .setOrException(RfmRegistrationDatePage, LocalDate.now())
        val sessionData = emptyUserAnswers
          .setOrException(RfmStatusPage, FailedInternalIssueError)
        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
        when(
          mockSubscriptionService.createAmendObjectForReplacingFilingMember(any[SubscriptionData], any[NewFilingMemberDetail], any[UserAnswers])(
            any()
          )
        ).thenReturn(Future.successful(amendData))
        when(mockSubscriptionService.amendFilingMemberDetails(any(), any[AmendSubscription])(any())).thenReturn(Future.successful(Done))
        when(mockSubscriptionService.deallocateEnrolment(any())(any())).thenReturn(Future.failed(InternalIssueError))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))

        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmWaitingRoomController.onPageLoad().url
          verify(mockSessionRepository).set(eqTo(sessionData))
        }
      }

      "redirect to waiting page in case of a FailException, if allocating an enrolment to the new filing member fails, and save the api response in the backend" ignore {
        val ua = rfmNoID
          .setOrException(RfmPillar2ReferencePage, "id")
          .setOrException(RfmRegistrationDatePage, LocalDate.now())
        val sessionData = emptyUserAnswers
          .setOrException(RfmStatusPage, FailedInternalIssueError)
        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
        when(
          mockSubscriptionService.createAmendObjectForReplacingFilingMember(any[SubscriptionData], any[NewFilingMemberDetail], any[UserAnswers])(
            any()
          )
        ).thenReturn(Future.successful(amendData))
        when(mockSubscriptionService.amendFilingMemberDetails(any(), any[AmendSubscription])(any())).thenReturn(Future.successful(Done))
        when(mockSubscriptionService.deallocateEnrolment(any())(any())).thenReturn(Future.successful(Done))
        when(mockSubscriptionService.getUltimateParentEnrolmentInformation(any[SubscriptionData], any(), any())(any()))
          .thenReturn(allocateEnrolmentParameters)
        when(mockSubscriptionService.allocateEnrolment(any(), any(), any[AllocateEnrolmentParameters])(any()))
          .thenReturn(Future.failed(InternalIssueError))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmWaitingRoomController.onPageLoad().url
          verify(mockSessionRepository).set(eqTo(sessionData))
        }
      }

      "redirect to waiting page in case of a FailException, if amend filing details fails with UnexpectedResponse, and save the api response in the backend" ignore {
        val completeUserAnswers = defaultRfmData
          .setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)
          .setOrException(RfmPillar2ReferencePage, "id")
        val sessionData = emptyUserAnswers
          .setOrException(RfmStatusPage, FailedInternalIssueError)
        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
        when(
          mockSubscriptionService.createAmendObjectForReplacingFilingMember(any[SubscriptionData], any[NewFilingMemberDetail], any[UserAnswers])(
            any()
          )
        ).thenReturn(Future.successful(amendData))
        when(mockSubscriptionService.amendFilingMemberDetails(any(), any[AmendSubscription])(any())).thenReturn(Future.failed(UnexpectedResponse))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmWaitingRoomController.onPageLoad().url
          verify(mockSessionRepository).set(eqTo(sessionData))
        }
      }

      "redirect to waiting page in case of a FailException, if read subscription fails, and save the api response in the backend" ignore {
        val ua = rfmNoID
          .setOrException(RfmPillar2ReferencePage, "id")
          .setOrException(RfmRegistrationDatePage, LocalDate.now())
        val sessionData = emptyUserAnswers
          .setOrException(RfmStatusPage, FailedInternalIssueError)
        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.failed(InternalIssueError))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmWaitingRoomController.onPageLoad().url
          verify(mockSessionRepository).set(eqTo(sessionData))
        }
      }

      "redirect to waiting page in case of a FailException, if amend filing details fails with InternalIssueError, and save the api response in the backend" ignore {
        val ua = rfmNoID
          .setOrException(RfmPillar2ReferencePage, "id")
          .setOrException(RfmRegistrationDatePage, LocalDate.now())
        val sessionData = emptyUserAnswers
          .setOrException(RfmStatusPage, FailedInternalIssueError)
        val application = applicationBuilder(userAnswers = Some(ua))
          .overrides(
            bind[SubscriptionService].toInstance(mockSubscriptionService),
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
        when(
          mockSubscriptionService.createAmendObjectForReplacingFilingMember(any[SubscriptionData], any[NewFilingMemberDetail], any[UserAnswers])(
            any()
          )
        ).thenReturn(Future.successful(amendData))
        when(mockSubscriptionService.amendFilingMemberDetails(any(), any[AmendSubscription])(any())).thenReturn(Future.failed(InternalIssueError))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(sessionData)))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmWaitingRoomController.onPageLoad().url
          verify(mockSessionRepository).set(eqTo(sessionData))
        }
      }

      "redirect to incomplete data page if they have only partially completed their application" in {
        val application = applicationBuilder(userAnswers = None).build()
        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual incompleteData
        }
      }

    }

  }

}
