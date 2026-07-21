/*
 * Copyright 2026 HM Revenue & Customs
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
import models.UserAnswers
import models.longrunningsubmissions.LongRunningSubmission
import models.subscription.{AccountingPeriod, AccountingPeriodDisplay, AmendAccountingPeriodStatus, SubscriptionLocalData}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.{AmendAccountingPeriodStatusPage, NewAccountingPeriodPage, OriginalAccountingPeriodsPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{ReferenceNumberService, SubscriptionService}
import utils.DateTimeUtils.*

import java.time.LocalDate
import scala.concurrent.Future

class AmendAccountingPeriodCYAControllerSpec extends SpecBase {

  private val newPeriod: AccountingPeriod =
    AccountingPeriod(LocalDate.of(2021, 9, 28), LocalDate.of(2022, 10, 3))

  private val allPeriods: Seq[AccountingPeriodDisplay] = Seq(
    AccountingPeriodDisplay(
      startDate = Some(LocalDate.of(2021, 9, 28)),
      endDate = Some(LocalDate.of(2022, 9, 27)),
      dueDate = Some(LocalDate.of(2022, 12, 31)),
      canAmendStartDate = Some(false),
      canAmendEndDate = Some(true)
    ),
    AccountingPeriodDisplay(
      startDate = Some(LocalDate.of(2022, 9, 28)),
      endDate = Some(LocalDate.of(2023, 9, 27)),
      dueDate = Some(LocalDate.of(2023, 12, 31)),
      canAmendStartDate = Some(true),
      canAmendEndDate = Some(true)
    )
  )

  private def baseLocalData = emptySubscriptionLocalData.copy(accountingPeriods = Some(allPeriods))

  private def buildApp(userAnswers: Option[UserAnswers], localData: Option[SubscriptionLocalData] = Some(baseLocalData)) = {
    when(mockSessionRepository.get(any())).thenReturn(Future.successful(userAnswers))
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
    applicationBuilder(subscriptionLocalData = localData)
      .overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[SubscriptionService].toInstance(mockSubscriptionService),
        bind[ReferenceNumberService].toInstance(mockReferenceNumberService)
      )
      .build()
  }

  "AmendAccountingPeriodCYAController onPageLoad" when {

    "redirects to journey recovery when NewAccountingPeriodPage not in session" in {
      val application = buildApp(userAnswers = None)
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.AmendAccountingPeriodCYAController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirects to journey recovery when accountingPeriods missing from local data" in {
      val ua          = UserAnswers("id").setOrException(NewAccountingPeriodPage, newPeriod)
      val application = buildApp(userAnswers = Some(ua), localData = Some(emptySubscriptionLocalData))
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.AmendAccountingPeriodCYAController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "renders CYA page with new period and no predicted micro-periods when no surrounding APs exist" in {
      val ua          = UserAnswers("id").setOrException(NewAccountingPeriodPage, newPeriod)
      val application = buildApp(userAnswers = Some(ua))
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.AmendAccountingPeriodCYAController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        val body = contentAsString(result)
        body must include(messages(application)("amendAccountingPeriodCYA.heading"))
        body must include("New accounting period - 1 year and 6 days")
        body must not include "This will create an accounting period of"
      }
    }

    "renders CYA page with predicted gapBefore when prior AP exists and new start is after earliest start" in {
      val priorAP = AccountingPeriodDisplay(
        startDate = Some(LocalDate.of(2020, 9, 28)),
        endDate = Some(LocalDate.of(2021, 9, 27)),
        dueDate = Some(LocalDate.of(2021, 12, 31)),
        canAmendStartDate = Some(false),
        canAmendEndDate = Some(true)
      )
      val periodsWithPrior    = priorAP +: allPeriods
      val newPeriodLaterStart = AccountingPeriod(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 10, 3))
      val ua                  = UserAnswers("id").setOrException(NewAccountingPeriodPage, newPeriodLaterStart)
      val application         = buildApp(
        userAnswers = Some(ua),
        localData = Some(emptySubscriptionLocalData.copy(accountingPeriods = Some(periodsWithPrior)))
      )
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.AmendAccountingPeriodCYAController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        val body = contentAsString(result)
        body must include("Your change has left a gap in your accounting history.")
        body must include("We have created an additional accounting period of 3 months and 4 days to fill the gap.")
      }
    }

    "renders CYA page with predicted gapAfter when next AP exists and new end is before latest end" in {
      val nextAP = AccountingPeriodDisplay(
        startDate = Some(LocalDate.of(2023, 9, 28)),
        endDate = Some(LocalDate.of(2024, 9, 27)),
        dueDate = Some(LocalDate.of(2024, 12, 31)),
        canAmendStartDate = Some(true),
        canAmendEndDate = Some(true)
      )
      val periodsWithNext             = allPeriods :+ nextAP
      val newPeriodWithEarlierEndDate = AccountingPeriod(LocalDate.of(2022, 9, 28), LocalDate.of(2023, 6, 30))
      val ua                          = UserAnswers("id").setOrException(NewAccountingPeriodPage, newPeriodWithEarlierEndDate)
      val application                 = buildApp(
        userAnswers = Some(ua),
        localData = Some(emptySubscriptionLocalData.copy(accountingPeriods = Some(periodsWithNext)))
      )
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.AmendAccountingPeriodCYAController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        val body = contentAsString(result)
        body must include("Your change has left a gap in your accounting history.")
        body must include("We have created an additional accounting period of 2 months and 27 days to fill the gap.")
      }
    }

    "shows no predicted period when new period covers the full original range" in {
      val fullCover   = AccountingPeriod(LocalDate.of(2021, 9, 28), LocalDate.of(2023, 9, 27))
      val ua          = UserAnswers("id").setOrException(NewAccountingPeriodPage, fullCover)
      val application = buildApp(userAnswers = Some(ua))
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.AmendAccountingPeriodCYAController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        val body = contentAsString(result)
        body must include(messages(application)("amendAccountingPeriodCYA.heading"))
        body must not include "This will create an accounting period of"
      }
    }

    "renders full 12 month open-ended period that covers today" in {
      val todayDate      = today
      val existingPeriod = AccountingPeriodDisplay(
        startDate = Some(todayDate.minusMonths(4)),
        endDate = Some(todayDate.minusMonths(2)),
        dueDate = Some(todayDate.plusMonths(1)),
        canAmendStartDate = Some(false),
        canAmendEndDate = Some(true)
      )
      val newOpenEndedPeriod = AccountingPeriod(startDate = todayDate.minusMonths(3), endDate = todayDate.minusDays(10))

      val ua          = UserAnswers("id").setOrException(NewAccountingPeriodPage, newOpenEndedPeriod)
      val application = buildApp(
        userAnswers = Some(ua),
        localData = Some(emptySubscriptionLocalData.copy(accountingPeriods = Some(Seq(existingPeriod))))
      )

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.AmendAccountingPeriodCYAController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustEqual OK

        val body = contentAsString(result)
        body must include("1 year")
      }
    }
  }

  "AmendAccountingPeriodCYAController onSubmit" when {

    "redirects to waiting room when valid data is present" in {
      val ua          = UserAnswers("id").setOrException(NewAccountingPeriodPage, newPeriod)
      val application = buildApp(userAnswers = Some(ua))
      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.AmendAccountingPeriodCYAController.onSubmit().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.WaitingRoomController
          .onPageLoad(LongRunningSubmission.AmendAccountingPeriod)
          .url
      }
    }

    "saves original periods and sets status to InProgress in session" in {
      val ua          = UserAnswers("id").setOrException(NewAccountingPeriodPage, newPeriod)
      val application = buildApp(userAnswers = Some(ua))
      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.AmendAccountingPeriodCYAController.onSubmit().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository, org.mockito.Mockito.atLeastOnce()).set(captor.capture())
        val firstSavedAnswers = captor.getAllValues.get(0)
        firstSavedAnswers.get(OriginalAccountingPeriodsPage) mustBe Some(allPeriods)
        firstSavedAnswers.get(AmendAccountingPeriodStatusPage) mustBe Some(AmendAccountingPeriodStatus.InProgress)
      }
    }

    "redirects to journey recovery when NewAccountingPeriodPage not in session" in {
      val application = buildApp(userAnswers = Some(UserAnswers("id")))
      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.AmendAccountingPeriodCYAController.onSubmit().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirects to journey recovery when no periods in local data" in {
      val ua          = UserAnswers("id").setOrException(NewAccountingPeriodPage, newPeriod)
      val application = buildApp(userAnswers = Some(ua), localData = Some(emptySubscriptionLocalData))
      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.AmendAccountingPeriodCYAController.onSubmit().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
