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

import akka.Done
import base.SpecBase
import controllers.actions.{AgentIdentifierAction, FakeIdentifierAction}
import models.subscription.{AccountingPeriod, DashboardInfo, SubscriptionLocalData}
import models.{MneOrDomestic, NonUKAddress, UnexpectedResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.inject
import play.api.inject.bind
import play.api.mvc.PlayBodyParsers
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SubscriptionService
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate
import scala.concurrent.Future

class ManageGroupDetailCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "Subscription Check Your Answers Controller" must {

    val startDate = LocalDate.of(2023, 12, 31)
    val endDate   = LocalDate.of(2025, 12, 31)
    val date      = AccountingPeriod(startDate, endDate)

    val amendSubUserAnswers = emptySubscriptionLocalData
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
      .setOrException(SubAccountingPeriodPage, date)
      .setOrException(FmDashboardPage, DashboardInfo("org name", LocalDate.of(2025, 12, 31)))
      .setOrException(NominateFilingMemberPage, false)

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

    "onPageLoad" should {
      "return OK and the correct view if an answer is provided to every question " in {
        val userAnswer = emptySubscriptionLocalData
          .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
          .setOrException(SubAccountingPeriodPage, date)

        val application = applicationBuilder(subscriptionLocalData = Some(userAnswer)).build()
        running(application) {
          val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Group details")
          contentAsString(result) must include("Where are the entities in your group located?")
        }
      }

      "return OK and the correct view for Agent if an answer is provided to every question " in {
        val userAnswer = emptySubscriptionLocalData
          .setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
          .setOrException(SubAccountingPeriodPage, date)

        val application = applicationBuilder(subscriptionLocalData = Some(userAnswer))
          .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
          .build()

        val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

        running(application) {
          when(mockAgentIdentifierAction.agentIdentify(any()))
            .thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

          val request = FakeRequest(
            GET,
            controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad(Some("XMPLR0123456789")).url
          )
          val result = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Group details")
          contentAsString(result) must include("Where are the entities in your group located?")
        }
      }

      "return OK and the correct view if an answer is provided to every question when UkAndOther  option is selected  " in {
        val userAnswer = emptySubscriptionLocalData
          .set(SubMneOrDomesticPage, MneOrDomestic.UkAndOther)
          .success
          .value
          .set(SubAccountingPeriodPage, date)
          .success
          .value
        val application = applicationBuilder(subscriptionLocalData = Some(userAnswer)).build()
        running(application) {
          val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Group details")
          contentAsString(result) must include("Where are the entities in your group located?")
        }
      }
    }

    "onSubmit" should {
      "redirect to journey recovery if no pillar2 reference is found" in {
        val application = applicationBuilder(subscriptionLocalData = Some(amendSubUserAnswers))
          .overrides(inject.bind[SubscriptionService].toInstance(mockSubscriptionService))
          .build()
        running(application) {
          val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onSubmit().url)
          val result  = route(application, request).value
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "redirect to error page if a an unexpected response is received from ETMP/BE" in {
        val application = applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData), enrolments = enrolments)
          .overrides(inject.bind[SubscriptionService].toInstance(mockSubscriptionService))
          .build()

        when(mockSubscriptionService.amendContactOrGroupDetails(any(), any(), any[SubscriptionLocalData])(any[HeaderCarrier]))
          .thenReturn(Future.failed(UnexpectedResponse))

        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onSubmit().url)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad.url
      }
    }

    "redirect Agent to error page if a an unexpected response is received from ETMP/BE" in {
      val application =
        applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData), enrolments = pillar2AgentEnrolmentWithDelegatedAuth.enrolments)
          .overrides(inject.bind[SubscriptionService].toInstance(mockSubscriptionService))
          .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
          .build()

      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

      when(mockSubscriptionService.amendSubscription(any(), any(), any[SubscriptionLocalData])(any[HeaderCarrier]))
        .thenReturn(Future.failed(UnexpectedResponse))

      val request = FakeRequest(
        POST,
        controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onSubmit(Some("XMPLR0123456789")).url
      )
      val result = route(application, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.ViewAmendSubscriptionFailedController.onPageLoad.url
    }

    "redirect to dashboard page if they successfully amend their data" in {
      val application = applicationBuilder(subscriptionLocalData = Some(amendSubUserAnswers), enrolments = enrolments)
        .overrides(inject.bind[SubscriptionService].toInstance(mockSubscriptionService))
        .build()
      running(application) {
        when(mockSubscriptionService.amendContactOrGroupDetails(any(), any(), any[SubscriptionLocalData])(any[HeaderCarrier]))
          .thenReturn(Future.successful(Done))
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onSubmit().url)
        val result  = route(application, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.DashboardController.onPageLoad().url
      }
    }

    "redirect Agent to dashboard page if they successfully amend their data" in {
      val application =
        applicationBuilder(subscriptionLocalData = Some(amendSubUserAnswers), enrolments = pillar2AgentEnrolmentWithDelegatedAuth.enrolments)
          .overrides(inject.bind[SubscriptionService].toInstance(mockSubscriptionService))
          .overrides(bind[AgentIdentifierAction].toInstance(mockAgentIdentifierAction))
          .build()

      val bodyParsers = application.injector.instanceOf[PlayBodyParsers]

      running(application) {
        when(mockAgentIdentifierAction.agentIdentify(any())).thenReturn(new FakeIdentifierAction(bodyParsers, pillar2AgentEnrolmentWithDelegatedAuth))

        when(mockSubscriptionService.amendSubscription(any(), any(), any[SubscriptionLocalData])(any[HeaderCarrier]))
          .thenReturn(Future.successful(Done))
        val request = FakeRequest(
          POST,
          controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onSubmit(Some("XMPLR0123456789")).url
        )
        val result = route(application, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.DashboardController.onPageLoad(Some("XMPLR0123456789"), agentView = true).url
      }
    }

  }
}
