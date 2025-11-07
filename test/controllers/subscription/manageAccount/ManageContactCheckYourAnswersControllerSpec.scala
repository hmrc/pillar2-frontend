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

package controllers.subscription.manageAccount

import base.SpecBase
import controllers.actions.TestAuthRetrievals.Ops
import models._
import models.fm.{FilingMember, FilingMemberNonUKData}
import models.subscription.ManageContactDetailsStatus
import models.subscription._
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import pages._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.Credentials
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.Future

class ManageContactCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {
  val subDataWithAddress: SubscriptionLocalData = emptySubscriptionLocalData
    .setOrException(SubPrimaryContactNamePage, "name")
    .setOrException(SubPrimaryEmailPage, "email@hello.com")
    .setOrException(SubPrimaryPhonePreferencePage, true)
    .setOrException(SubPrimaryCapturePhonePage, "123213")
    .setOrException(SubSecondaryContactNamePage, "name")
    .setOrException(SubSecondaryEmailPage, "email@hello.com")
    .setOrException(SubSecondaryPhonePreferencePage, true)
    .setOrException(SubSecondaryCapturePhonePage, "123213")
    .setOrException(SubRegisteredAddressPage, NonUKAddress("this", None, "over", None, None, countryCode = "AR"))

  val subDataWithoutAddress: SubscriptionLocalData = emptySubscriptionLocalData
    .setOrException(SubPrimaryContactNamePage, "name")
    .setOrException(SubPrimaryEmailPage, "email@hello.com")
    .setOrException(SubPrimaryPhonePreferencePage, true)
    .setOrException(SubPrimaryCapturePhonePage, "123213")
    .setOrException(SubSecondaryContactNamePage, "name")
    .setOrException(SubSecondaryEmailPage, "email@hello.com")
    .setOrException(SubSecondaryPhonePreferencePage, true)
    .setOrException(SubSecondaryCapturePhonePage, "123213")

  val filingMember: FilingMember =
    FilingMember(
      isNfmRegisteredInUK = false,
      withoutIdRegData = Some(
        FilingMemberNonUKData(
          registeredFmName = "Nfm name ",
          contactName = "Ashley Smith",
          emailAddress = "test@test.com",
          phonePreference = true,
          phone = Some("122223444"),
          registeredFmAddress = nonUkAddress
        )
      )
    )
  val enrolments: Set[Enrolment] = Set(
    Enrolment(
      key = "HMRC-PILLAR2-ORG",
      identifiers = Seq(
        EnrolmentIdentifier("PLRID", "12345678"),
        EnrolmentIdentifier("UTR", "ABC12345")
      ),
      state = "activated"
    )
  )
  val startDate: LocalDate = LocalDate.of(2023, 12, 31)
  val endDate:   LocalDate = LocalDate.of(2025, 12, 31)
  val amendSubscription: SubscriptionLocalData = emptySubscriptionLocalData
    .setOrException(UpeRegisteredInUKPage, true)
    .setOrException(UpeNameRegistrationPage, "International Organisation Inc.")
    .setOrException(SubPrimaryContactNamePage, "Name")
    .setOrException(SubPrimaryEmailPage, "email@email.com")
    .setOrException(SubPrimaryPhonePreferencePage, true)
    .setOrException(SubPrimaryCapturePhonePage, "123456789")
    .setOrException(SubAddSecondaryContactPage, true)
    .setOrException(SubSecondaryContactNamePage, "second contact name")
    .setOrException(SubSecondaryEmailPage, "second@email.com")
    .setOrException(SubSecondaryPhonePreferencePage, true)
    .setOrException(SubSecondaryCapturePhonePage, "123456789")
    .setOrException(SubRegisteredAddressPage, NonUKAddress("this", None, "over", None, None, countryCode = "AR"))
    .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
    .setOrException(SubAccountingPeriodPage, AccountingPeriod(startDate, endDate, None))
    .setOrException(FmDashboardPage, DashboardInfo("org name", LocalDate.of(2025, 12, 31)))
    .setOrException(NominateFilingMemberPage, false)

  val id:           String = UUID.randomUUID().toString
  val providerId:   String = UUID.randomUUID().toString
  val providerType: String = UUID.randomUUID().toString

  "Contact Check Your Answers Controller" must {

    "return OK and the correct view if an answer is provided to every question" in {
      val mockSessionRepository = mock[SessionRepository]
      val userAnswers           = UserAnswers("id")
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(subscriptionLocalData = Some(subDataWithAddress))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include("Contact details")
        contentAsString(result) must include("Second contact")
        contentAsString(result) must include("Filing member contact address")
      }
    }

    "return OK and correct view if an Agent" in {
      val mockSessionRepository = mock[SessionRepository]
      val userAnswers           = UserAnswers("id")
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(subscriptionLocalData = Some(subDataWithAddress))
        .overrides(
          bind[AuthConnector].toInstance(mockAuthConnector),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include("Contact details")
        contentAsString(result) must include("Second contact")
        contentAsString(result) must include("Filing member contact address")
      }
    }

    "redirect to bookmark page if address page not answered" in {
      val application = applicationBuilder(subscriptionLocalData = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "redirect to waiting room when status is InProgress" in {
      val mockSessionRepository = mock[SessionRepository]
      val userAnswers = UserAnswers("id")
        .setOrException(ManageContactDetailsStatusPage, ManageContactDetailsStatus.InProgress)

      when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

      val application = applicationBuilder(
        userAnswers = Some(userAnswers),
        subscriptionLocalData = Some(subDataWithAddress)
      )
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual controllers.subscription.manageAccount.routes.ManageContactDetailsWaitingRoomController.onPageLoad.url
      }
    }

    "onSubmit" should {

      "set status to InProgress and redirect to waiting room immediately" in {
        val mockSessionRepository = mock[SessionRepository]
        val userAnswers           = UserAnswers("id")

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockSessionRepository.setSubmittedFlagIfNotSet(any(), any(), any(), any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val application = applicationBuilder(
          userAnswers = Some(userAnswers),
          subscriptionLocalData = Some(amendSubscription),
          enrolments = enrolments
        )
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(
            result
          ).value mustEqual controllers.subscription.manageAccount.routes.ManageContactDetailsWaitingRoomController.onPageLoad.url
          verify(mockSessionRepository).setSubmittedFlagIfNotSet(any(), any(), any(), any())
        }
      }

      "update status to SuccessfullyCompleted when background subscription update succeeds" ignore {
        val mockSessionRepository  = mock[SessionRepository]
        val userAnswers            = UserAnswers("id")
        val expectedInitialAnswers = userAnswers.setOrException(ManageContactDetailsStatusPage, ManageContactDetailsStatus.InProgress)
        val expectedFinalAnswers   = userAnswers.setOrException(ManageContactDetailsStatusPage, ManageContactDetailsStatus.SuccessfullyCompleted)

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockSubscriptionService.amendContactOrGroupDetails(any(), any(), any[SubscriptionLocalData])(any()))
          .thenReturn(Future.successful(Done))

        val application = applicationBuilder(
          userAnswers = Some(userAnswers),
          subscriptionLocalData = Some(amendSubscription),
          enrolments = enrolments
        )
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(
            result
          ).value mustEqual controllers.subscription.manageAccount.routes.ManageContactDetailsWaitingRoomController.onPageLoad.url

          verify(mockSessionRepository).set(org.mockito.ArgumentMatchers.eq(expectedInitialAnswers))
          verify(mockSessionRepository).set(org.mockito.ArgumentMatchers.eq(expectedFinalAnswers))
        }
      }

      "handle subscription service failure with InternalIssueError" in {
        val mockSessionRepository = mock[SessionRepository]
        val userAnswers           = UserAnswers("id")

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockSessionRepository.setSubmittedFlagIfNotSet(any(), any(), any(), any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockSubscriptionService.amendContactOrGroupDetails(any(), any(), any[SubscriptionLocalData])(any()))
          .thenReturn(Future.failed(InternalIssueError))

        val application = applicationBuilder(
          userAnswers = Some(userAnswers),
          subscriptionLocalData = Some(amendSubscription),
          enrolments = enrolments
        )
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(
            result
          ).value mustEqual controllers.subscription.manageAccount.routes.ManageContactDetailsWaitingRoomController.onPageLoad.url

          // Verify initial status update
          verify(mockSessionRepository).setSubmittedFlagIfNotSet(any(), any(), any(), any())
        }
      }

      "handle subscription service failure with UnexpectedResponse" in {
        val mockSessionRepository = mock[SessionRepository]
        val userAnswers           = UserAnswers("id")

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockSessionRepository.setSubmittedFlagIfNotSet(any(), any(), any(), any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockSubscriptionService.amendContactOrGroupDetails(any(), any(), any[SubscriptionLocalData])(any()))
          .thenReturn(Future.failed(UnexpectedResponse))

        val application = applicationBuilder(
          userAnswers = Some(userAnswers),
          subscriptionLocalData = Some(amendSubscription),
          enrolments = enrolments
        )
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(
            result
          ).value mustEqual controllers.subscription.manageAccount.routes.ManageContactDetailsWaitingRoomController.onPageLoad.url

          // Verify initial status update
          verify(mockSessionRepository).setSubmittedFlagIfNotSet(any(), any(), any(), any())
        }
      }

      "handle unexpected exception during submission" in {
        val mockSessionRepository             = mock[SessionRepository]
        val userAnswers                       = UserAnswers("id")
        val initialUserAnswersWithInProgress  = userAnswers.setOrException(ManageContactDetailsStatusPage, ManageContactDetailsStatus.InProgress)
        val finalUserAnswersWithFailException = userAnswers.setOrException(ManageContactDetailsStatusPage, ManageContactDetailsStatus.FailException)

        when(mockSessionRepository.get(userAnswers.id))
          .thenReturn(Future.successful(Some(userAnswers)))
          .thenReturn(Future.successful(Some(initialUserAnswersWithInProgress)))

        when(mockSessionRepository.setSubmittedFlagIfNotSet(any(), any(), any(), any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.set(finalUserAnswersWithFailException)).thenReturn(Future.successful(true)) // Mock for the final set

        when(mockSubscriptionService.amendContactOrGroupDetails(any(), any(), any[SubscriptionLocalData])(any()))
          .thenReturn(Future.failed(new RuntimeException("Unexpected error")))

        val application = applicationBuilder(
          userAnswers = Some(userAnswers),
          subscriptionLocalData = Some(amendSubscription),
          enrolments = enrolments
        )
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.ManageContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.ManageContactDetailsWaitingRoomController.onPageLoad.url

          verify(mockSessionRepository).setSubmittedFlagIfNotSet(any(), any(), any(), any())
          verify(mockSessionRepository).set(org.mockito.ArgumentMatchers.eq(finalUserAnswersWithFailException))
        }
      }

      "handle InternalIssueError during submission" in {
        val mockSessionRepository            = mock[SessionRepository]
        val userAnswers                      = UserAnswers("id")
        val initialUserAnswersWithInProgress = userAnswers.setOrException(ManageContactDetailsStatusPage, ManageContactDetailsStatus.InProgress)
        // Expect FailedInternalIssueError for InternalIssueError
        val finalUserAnswersWithFailedInternalIssueError =
          userAnswers.setOrException(ManageContactDetailsStatusPage, ManageContactDetailsStatus.FailedInternalIssueError)

        when(mockSessionRepository.get(userAnswers.id))
          .thenReturn(Future.successful(Some(userAnswers)))
          .thenReturn(Future.successful(Some(initialUserAnswersWithInProgress)))

        when(mockSessionRepository.setSubmittedFlagIfNotSet(any(), any(), any(), any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.set(finalUserAnswersWithFailedInternalIssueError)).thenReturn(Future.successful(true)) // Mock for the final set

        when(mockSubscriptionService.amendContactOrGroupDetails(any(), any(), any[SubscriptionLocalData])(any()))
          .thenReturn(Future.failed(InternalIssueError))

        val application = applicationBuilder(
          userAnswers = Some(userAnswers),
          subscriptionLocalData = Some(amendSubscription),
          enrolments = enrolments
        )
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.ManageContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.ManageContactDetailsWaitingRoomController.onPageLoad.url

          verify(mockSessionRepository).setSubmittedFlagIfNotSet(any(), any(), any(), any())
          verify(mockSessionRepository).set(org.mockito.ArgumentMatchers.eq(finalUserAnswersWithFailedInternalIssueError))
        }
      }

      "handle UnexpectedResponse during submission" in {
        val mockSessionRepository             = mock[SessionRepository]
        val userAnswers                       = UserAnswers("id")
        val initialUserAnswersWithInProgress  = userAnswers.setOrException(ManageContactDetailsStatusPage, ManageContactDetailsStatus.InProgress)
        val finalUserAnswersWithFailException = userAnswers.setOrException(ManageContactDetailsStatusPage, ManageContactDetailsStatus.FailException)

        when(mockSessionRepository.get(userAnswers.id))
          .thenReturn(Future.successful(Some(userAnswers)))
          .thenReturn(Future.successful(Some(initialUserAnswersWithInProgress)))

        when(mockSessionRepository.setSubmittedFlagIfNotSet(any(), any(), any(), any())).thenReturn(Future.successful(true))
        when(mockSessionRepository.set(finalUserAnswersWithFailException)).thenReturn(Future.successful(true)) // Mock for the final set

        when(mockSubscriptionService.amendContactOrGroupDetails(any(), any(), any[SubscriptionLocalData])(any()))
          .thenReturn(Future.failed(UnexpectedResponse))

        val application = applicationBuilder(
          userAnswers = Some(userAnswers),
          subscriptionLocalData = Some(amendSubscription),
          enrolments = enrolments
        )
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, routes.ManageContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.ManageContactDetailsWaitingRoomController.onPageLoad.url

          verify(mockSessionRepository).setSubmittedFlagIfNotSet(any(), any(), any(), any())
          verify(mockSessionRepository).set(org.mockito.ArgumentMatchers.eq(finalUserAnswersWithFailException))
        }
      }

      "prevent multiple-click submission by redirecting to waiting room when already submitted (idempotency)" in {
        val mockSessionRepository = mock[SessionRepository]
        val userAnswers           = UserAnswers("id")

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(mockSessionRepository.setSubmittedFlagIfNotSet(any(), any(), any(), any())).thenReturn(Future.successful(false))

        val application = applicationBuilder(
          userAnswers = Some(userAnswers),
          subscriptionLocalData = Some(amendSubscription),
          enrolments = enrolments
        )
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionService].toInstance(mockSubscriptionService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onSubmit.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(
            result
          ).value mustEqual controllers.subscription.manageAccount.routes.ManageContactDetailsWaitingRoomController.onPageLoad.url
          verify(mockSessionRepository).setSubmittedFlagIfNotSet(any(), any(), any(), any())
          verify(mockSubscriptionService, never()).amendContactOrGroupDetails(any(), any(), any[SubscriptionLocalData])(any())
        }
      }
    }

    "onPageLoad" should {
      "handle session repository failure gracefully" in {
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.get(any())).thenReturn(Future.failed(new Exception("Database error")))

        val application = applicationBuilder(subscriptionLocalData = Some(subDataWithAddress))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value

          whenReady(result.failed) { e =>
            e mustBe a[Exception]
            e.getMessage mustEqual "Database error"
          }
        }
      }

      "handle partial contact details" in {
        val mockSessionRepository = mock[SessionRepository]
        val userAnswers           = UserAnswers("id")

        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))

        val partialData = emptySubscriptionLocalData
          .setOrException(SubPrimaryContactNamePage, "name")
          .setOrException(SubPrimaryEmailPage, "email@hello.com")
        // Missing phone preference and phone number

        val application = applicationBuilder(
          subscriptionLocalData = Some(partialData),
          userAnswers = Some(userAnswers)
        )
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) must include("Contact details")
          contentAsString(result) must include("name")
          contentAsString(result) must include("email@hello.com")
        }
      }
    }
  }
}
