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
import models.rfm.CorporatePosition
import models.subscription.{AmendSubscription, NewFilingMemberDetail, SubscriptionData}
import models.{InternalIssueError, UnexpectedResponse, UserAnswers, Verifier}
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{verify, when}
import pages._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import utils.FutureConverter.FutureOps
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.Future

class RfmContactCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Controller" must {

    "redirect to correct view when rfm feature false" in {
      val application = applicationBuilder(userAnswers = Some(rfmID))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> false
          ): _*
        )
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ErrorController.pageNotFoundLoad.url
      }
    }
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
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK

        contentAsString(result) must include("Filing member details")
        contentAsString(result) must include("Ultimate parent entity (UPE)")
        contentAsString(result) must include("First contact")
        contentAsString(result) must include("Contact name")
        contentAsString(result) must include("Email address")
        contentAsString(result) must include("Telephone contact")
        contentAsString(result) must include("Telephone number")
        contentAsString(result) must include("Second contact")
        contentAsString(result) must include("Second contact name")
        contentAsString(result) must include("Second contact email address")
        contentAsString(result) must include("Can we contact by telephone?")
        contentAsString(result) must include("Second contact telephone number")
        contentAsString(result) must include("Contact address")
        contentAsString(result) must include("Address")
        contentAsString(result) must include("the first contact name")
        contentAsString(result) must include("the first contact email address")
        contentAsString(result) must include("can we contact the first contact by telephone")
        contentAsString(result) must include("the telephone number for the first contact")
        contentAsString(result) must include("the telephone number for the first contact")
        contentAsString(result) must include("do you have a second contact")
        contentAsString(result) must include("the second contact name")
        contentAsString(result) must include("the second contact email address")
        contentAsString(result) must include("can we contact the second contact by telephone")
        contentAsString(result) must include("the telephone number for the second contact")
        contentAsString(result) must include("Now submit your details to replace the current filing member")
        contentAsString(result) must include(
          "By submitting these details, you are confirming that the information is correct and complete to the best of your knowledge."
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
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK

        contentAsString(result) must include("Filing member details")
        contentAsString(result) must include("New nominated filing member")
        contentAsString(result) must include("Name")
        contentAsString(result) must include("Address")
        contentAsString(result) must include("First contact")
        contentAsString(result) must include("Contact name")
        contentAsString(result) must include("Email address")
        contentAsString(result) must include("Telephone contact")
        contentAsString(result) must include("Telephone number")
        contentAsString(result) must include("Second contact")
        contentAsString(result) must include("Second contact name")
        contentAsString(result) must include("Second contact email address")
        contentAsString(result) must include("Can we contact by telephone?")
        contentAsString(result) must include("Second contact telephone number")
        contentAsString(result) must include("Contact address")
        contentAsString(result) must include("Address")
        contentAsString(result) must include("the first contact name")
        contentAsString(result) must include("the first contact email address")
        contentAsString(result) must include("can we contact the first contact by telephone")
        contentAsString(result) must include("the telephone number for the first contact")
        contentAsString(result) must include("the telephone number for the first contact")
        contentAsString(result) must include("do you have a second contact")
        contentAsString(result) must include("the second contact name")
        contentAsString(result) must include("the second contact email address")
        contentAsString(result) must include("can we contact the second contact by telephone")
        contentAsString(result) must include("the telephone number for the second contact")
        contentAsString(result) must include("Now submit your details to replace the current filing member")
        contentAsString(result) must include(
          "By submitting these details, you are confirming that the information is correct and complete to the best of your knowledge."
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
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK

        contentAsString(result) must include("Filing member details")
        contentAsString(result) must include("New nominated filing member")
        contentAsString(result) must include(
          "Company"
        )
        contentAsString(result) must include("ABC Limited")
        contentAsString(result) must include(
          "Company Registration Number"
        )
        contentAsString(result) must include("1234")
        contentAsString(result) must include(
          "Unique Taxpayer Reference"
        )
        contentAsString(result) must include("First contact")
        contentAsString(result) must include("Contact name")
        contentAsString(result) must include("Email address")
        contentAsString(result) must include("Telephone contact")
        contentAsString(result) must include("Telephone number")
        contentAsString(result) must include("Second contact")
        contentAsString(result) must include("Second contact name")
        contentAsString(result) must include("Second contact email address")
        contentAsString(result) must include("Can we contact by telephone?")
        contentAsString(result) must include("Second contact telephone number")
        contentAsString(result) must include("Contact address")
        contentAsString(result) must include("Address")
        contentAsString(result) must include("the first contact name")
        contentAsString(result) must include("the first contact email address")
        contentAsString(result) must include("can we contact the first contact by telephone")
        contentAsString(result) must include("the telephone number for the first contact")
        contentAsString(result) must include("the telephone number for the first contact")
        contentAsString(result) must include("do you have a second contact")
        contentAsString(result) must include("the second contact name")
        contentAsString(result) must include("the second contact email address")
        contentAsString(result) must include("can we contact the second contact by telephone")
        contentAsString(result) must include("the telephone number for the second contact")
        contentAsString(result) must include("Now submit your details to replace the current filing member")
        contentAsString(result) must include(
          "By submitting these details, you are confirming that the information is correct and complete to the best of your knowledge."
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
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK

        contentAsString(result) must include("Filing member details")
        contentAsString(result) must include("New nominated filing member")
        contentAsString(result) must include("name")
        contentAsString(result) must include("First contact")
        contentAsString(result) must include("Contact name")
        contentAsString(result) must include("Email address")
        contentAsString(result) must include("Telephone contact")
        contentAsString(result) must include("Telephone number")
        contentAsString(result) must include("Second contact")
        contentAsString(result) must include("Second contact name")
        contentAsString(result) must include("Second contact email address")
        contentAsString(result) must include("Can we contact by telephone?")
        contentAsString(result) must include("Second contact telephone number")
        contentAsString(result) must include("Contact address")
        contentAsString(result) must include("Address")
      }
    }

    "onSubmit" should {
      val defaultRfmData              = rfmPrimaryAndSecondaryContactData.setOrException(RfmPillar2ReferencePage, "plrReference")
      lazy val journeyRecovery        = controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url
      lazy val incompleteData         = controllers.rfm.routes.RfmIncompleteDataController.onPageLoad.url
      lazy val amendApiFailure        = controllers.rfm.routes.AmendApiFailureController.onPageLoad.url
      val allocateEnrolmentParameters = AllocateEnrolmentParameters(userId = "id", verifiers = Seq(Verifier("postCode", "M199999"))).toFuture
      "redirect to the confirmation page in case of a successful replace filing member and data is removed" in {
        val completeUserAnswers = defaultRfmData.setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)
        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService), bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()
        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
          when(mockUserAnswersConnectors.remove(any())(any())).thenReturn(Future.successful(Done))
          when(mockSubscriptionService.deallocateEnrolment(any())(any())).thenReturn(Future.successful(Done))
          when(mockSubscriptionService.getUltimateParentEnrolmentInformation(any[SubscriptionData], any(), any())(any()))
            .thenReturn(allocateEnrolmentParameters)
          when(mockSubscriptionService.allocateEnrolment(any(), any(), any[AllocateEnrolmentParameters])(any())).thenReturn(Future.successful(Done))
          when(
            mockSubscriptionService.createAmendObjectForReplacingFilingMember(any[SubscriptionData], any[NewFilingMemberDetail], any[UserAnswers])(
              any()
            )
          ).thenReturn(Future.successful(amendData))
          when(mockSubscriptionService.amendFilingMemberDetails(any(), any[AmendSubscription])(any())).thenReturn(Future.successful(Done))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmConfirmationController.onPageLoad.url
          verify(mockUserAnswersConnectors).remove(eqTo(emptyUserAnswers.id))(any())
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
      "redirect to Journey recovery if no group ID is found for the new filing member" in {
        val ua = defaultRfmData.setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)
        val application = applicationBuilder(userAnswers = Some(ua), groupID = None)
          .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
          .build()
        running(application) {
          when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual journeyRecovery
        }
      }
      "redirect to journey recovery page if new filing member uk basedpage value cannot be found" in {
        val application = applicationBuilder(userAnswers = Some(rfmNoID))
          .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
          .build()
        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
          when(mockSubscriptionService.deallocateEnrolment(any())(any())).thenReturn(Future.successful(Done))
          when(mockSubscriptionService.allocateEnrolment(any(), any(), any[AllocateEnrolmentParameters])(any())).thenReturn(Future.successful(Done))
          when(
            mockSubscriptionService.createAmendObjectForReplacingFilingMember(any[SubscriptionData], any[NewFilingMemberDetail], any[UserAnswers])(
              any()
            )
          ).thenReturn(Future.failed(new Exception("no rfm uk based")))
          when(mockSubscriptionService.amendFilingMemberDetails(any(), any[AmendSubscription])(any())).thenReturn(Future.successful(Done))
          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual journeyRecovery
        }
      }
      "redirect to under construction page if registering new filing member fails" in {
        val application = applicationBuilder(userAnswers = Some(rfmNoID.setOrException(RfmPillar2ReferencePage, "id")), groupID = Some("id"))
          .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
          .build()
        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
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
          when(mockSubscriptionService.amendFilingMemberDetails(any(), any[AmendSubscription])(any())).thenReturn(Future.successful(Done))
          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual amendApiFailure
        }
      }
      "redirect to under construction page if deallocating old filing member fails" in {
        val application = applicationBuilder(userAnswers = Some(rfmNoID.setOrException(RfmPillar2ReferencePage, "id")))
          .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
          .build()
        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
          when(mockSubscriptionService.deallocateEnrolment(any())(any())).thenReturn(Future.failed(InternalIssueError))
          when(mockSubscriptionService.allocateEnrolment(any(), any(), any[AllocateEnrolmentParameters])(any())).thenReturn(Future.successful(Done))
          when(
            mockSubscriptionService.createAmendObjectForReplacingFilingMember(any[SubscriptionData], any[NewFilingMemberDetail], any[UserAnswers])(
              any()
            )
          ).thenReturn(Future.successful(amendData))
          when(mockSubscriptionService.amendFilingMemberDetails(any(), any[AmendSubscription])(any())).thenReturn(Future.successful(Done))
          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual amendApiFailure
        }
      }
      "redirect to under construction page if allocating an enrolment to the new filing member fails" in {
        val application = applicationBuilder(userAnswers = Some(rfmNoID.setOrException(RfmPillar2ReferencePage, "id")))
          .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
          .build()
        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
          when(mockSubscriptionService.deallocateEnrolment(any())(any())).thenReturn(Future.successful(Done))
          when(mockSubscriptionService.allocateEnrolment(any(), any(), any[AllocateEnrolmentParameters])(any()))
            .thenReturn(Future.failed(InternalIssueError))
          when(mockSubscriptionService.getUltimateParentEnrolmentInformation(any[SubscriptionData], any(), any())(any()))
            .thenReturn(allocateEnrolmentParameters)
          when(
            mockSubscriptionService.createAmendObjectForReplacingFilingMember(any[SubscriptionData], any[NewFilingMemberDetail], any[UserAnswers])(
              any()
            )
          ).thenReturn(Future.successful(amendData))
          when(mockSubscriptionService.amendFilingMemberDetails(any(), any[AmendSubscription])(any())).thenReturn(Future.successful(Done))
          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual amendApiFailure
        }
      }
      "redirect to amend api failure page if amend filing details fails" in {
        val completeUserAnswers = defaultRfmData
          .setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)
          .setOrException(RfmPillar2ReferencePage, "id")

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
          .build()
        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
          when(mockSubscriptionService.deallocateEnrolment(any())(any())).thenReturn(Future.successful(Done))
          when(mockSubscriptionService.allocateEnrolment(any(), any(), any[AllocateEnrolmentParameters])(any())).thenReturn(Future.successful(Done))
          when(mockSubscriptionService.getUltimateParentEnrolmentInformation(any[SubscriptionData], any(), any())(any()))
            .thenReturn(allocateEnrolmentParameters)
          when(
            mockSubscriptionService.createAmendObjectForReplacingFilingMember(any[SubscriptionData], any[NewFilingMemberDetail], any[UserAnswers])(
              any()
            )
          ).thenReturn(Future.successful(amendData))
          when(mockSubscriptionService.amendFilingMemberDetails(any(), any[AmendSubscription])(any())).thenReturn(Future.failed(UnexpectedResponse))
          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual amendApiFailure
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
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
          val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmCannotReturnAfterConfirmationController.onPageLoad.url
        }
      }

      "redirect to under construction page if read subscription fails" in {
        val application = applicationBuilder(userAnswers = Some(rfmNoID.setOrException(RfmPillar2ReferencePage, "id")))
          .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
          .build()
        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.failed(InternalIssueError))
          when(mockSubscriptionService.deallocateEnrolment(any())(any())).thenReturn(Future.successful(Done))
          when(mockSubscriptionService.allocateEnrolment(any(), any(), any[AllocateEnrolmentParameters])(any())).thenReturn(Future.successful(Done))
          when(
            mockSubscriptionService.createAmendObjectForReplacingFilingMember(any[SubscriptionData], any[NewFilingMemberDetail], any[UserAnswers])(
              any()
            )
          ).thenReturn(Future.successful(amendData))
          when(mockSubscriptionService.amendFilingMemberDetails(any(), any[AmendSubscription])(any())).thenReturn(Future.successful(Done))
          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual amendApiFailure
        }
      }

      "redirect to under construction page if amend subscription fails" in {
        val application = applicationBuilder(userAnswers = Some(rfmNoID.setOrException(RfmPillar2ReferencePage, "id")))
          .overrides(bind[SubscriptionService].toInstance(mockSubscriptionService))
          .build()
        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmContactCheckYourAnswersController.onSubmit.url)
          when(mockSubscriptionService.readSubscription(any())(any())).thenReturn(Future.successful(subscriptionData))
          when(mockSubscriptionService.deallocateEnrolment(any())(any())).thenReturn(Future.successful(Done))
          when(mockSubscriptionService.getUltimateParentEnrolmentInformation(any[SubscriptionData], any(), any())(any()))
            .thenReturn(allocateEnrolmentParameters)
          when(mockSubscriptionService.allocateEnrolment(any(), any(), any[AllocateEnrolmentParameters])(any())).thenReturn(Future.successful(Done))
          when(
            mockSubscriptionService.createAmendObjectForReplacingFilingMember(any[SubscriptionData], any[NewFilingMemberDetail], any[UserAnswers])(
              any()
            )
          ).thenReturn(Future.successful(amendData))
          when(mockSubscriptionService.amendFilingMemberDetails(any(), any[AmendSubscription])(any())).thenReturn(Future.failed(InternalIssueError))
          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual amendApiFailure
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
