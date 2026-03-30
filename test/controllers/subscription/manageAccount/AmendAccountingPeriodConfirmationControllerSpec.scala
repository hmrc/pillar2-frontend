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
import models.subscription.AccountingPeriodV2
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{AmendAPConfirmationTimestampPage, OriginalAccountingPeriodsPage, UpdatedAccountingPeriodsPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository

import java.time.LocalDate
import scala.concurrent.Future

class AmendAccountingPeriodConfirmationControllerSpec extends SpecBase {

  private val originalPeriods: Seq[AccountingPeriodV2] = Seq(
    AccountingPeriodV2(
      LocalDate.of(2025, 1, 1),
      LocalDate.of(2025, 12, 31),
      LocalDate.of(2026, 3, 31),
      canAmendStartDate = true,
      canAmendEndDate = true
    )
  )

  private val updatedPeriods: Seq[AccountingPeriodV2] = Seq(
    AccountingPeriodV2(
      LocalDate.of(2025, 1, 1),
      LocalDate.of(2025, 12, 31),
      LocalDate.of(2026, 3, 31),
      canAmendStartDate = true,
      canAmendEndDate = true
    ),
    AccountingPeriodV2(
      LocalDate.of(2026, 1, 2),
      LocalDate.of(2026, 12, 31),
      LocalDate.of(2027, 3, 31),
      canAmendStartDate = true,
      canAmendEndDate = true
    ),
    AccountingPeriodV2(
      LocalDate.of(2026, 1, 1),
      LocalDate.of(2026, 1, 1),
      LocalDate.of(2026, 3, 31),
      canAmendStartDate = true,
      canAmendEndDate = true
    )
  )

  private val timestamp = "25 March 2026 at 2:25pm"

  private def userAnswersWithConfirmationData: UserAnswers =
    UserAnswers("id")
      .setOrException(OriginalAccountingPeriodsPage, originalPeriods)
      .setOrException(UpdatedAccountingPeriodsPage, updatedPeriods)
      .setOrException(AmendAPConfirmationTimestampPage, timestamp)

  private def buildApp(userAnswers: Option[UserAnswers]) = {
    when(mockSessionRepository.get(any())).thenReturn(Future.successful(userAnswers))
    applicationBuilder(
      subscriptionLocalData = Some(emptySubscriptionLocalData),
      additionalData = Map("features.amendMultipleAccountingPeriods" -> true)
    ).overrides(
      bind[SessionRepository].toInstance(mockSessionRepository)
    ).build()
  }

  "AmendAccountingPeriodConfirmationController onPageLoad" when {

    "renders the confirmation page with new periods when session data is present" in {
      val application = buildApp(Some(userAnswersWithConfirmationData))
      running(application) {
        val request = FakeRequest(
          GET,
          controllers.subscription.manageAccount.routes.AmendAccountingPeriodConfirmationController.onPageLoad().url
        )
        val result = route(application, request).value
        status(result) mustEqual OK
        val body = contentAsString(result)
        body must include(messages(application)("amendAccountingPeriod.confirmation.heading"))
        body must include(timestamp)
        body must include(messages(application)("amendAccountingPeriod.confirmation.whatHasChanged"))
        body must include(messages(application)("amendAccountingPeriod.confirmation.newPeriod.title"))
        body must include(messages(application)("amendAccountingPeriod.confirmation.gapPeriods"))
      }
    }

    "renders new periods in descending end date order" in {
      val application = buildApp(Some(userAnswersWithConfirmationData))
      running(application) {
        val request = FakeRequest(
          GET,
          controllers.subscription.manageAccount.routes.AmendAccountingPeriodConfirmationController.onPageLoad().url
        )
        val result = route(application, request).value
        status(result) mustEqual OK
        val body      = contentAsString(result)
        val firstIdx  = body.indexOf("31 December 2026")
        val secondIdx = body.indexOf("1 January 2026")
        firstIdx  must be >= 0
        secondIdx must be >= 0
        firstIdx  must be < secondIdx
      }
    }

    "does not show gap period message when only one new period exists" in {
      val singleNewUpdated = originalPeriods ++ Seq(
        AccountingPeriodV2(
          LocalDate.of(2026, 1, 1),
          LocalDate.of(2026, 12, 31),
          LocalDate.of(2027, 3, 31),
          canAmendStartDate = true,
          canAmendEndDate = true
        )
      )
      val ua = UserAnswers("id")
        .setOrException(OriginalAccountingPeriodsPage, originalPeriods)
        .setOrException(UpdatedAccountingPeriodsPage, singleNewUpdated)
        .setOrException(AmendAPConfirmationTimestampPage, timestamp)

      val application = buildApp(Some(ua))
      running(application) {
        val request = FakeRequest(
          GET,
          controllers.subscription.manageAccount.routes.AmendAccountingPeriodConfirmationController.onPageLoad().url
        )
        val result = route(application, request).value
        status(result) mustEqual OK
        val body = contentAsString(result)
        body must include(messages(application)("amendAccountingPeriod.confirmation.heading"))
        body must not include messages(application)("amendAccountingPeriod.confirmation.gapPeriods")
      }
    }

    "redirects to journey recovery when session data is missing" in {
      val application = buildApp(None)
      running(application) {
        val request = FakeRequest(
          GET,
          controllers.subscription.manageAccount.routes.AmendAccountingPeriodConfirmationController.onPageLoad().url
        )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirects to journey recovery when required pages are missing from user answers" in {
      val incompleteUa = UserAnswers("id")
        .setOrException(OriginalAccountingPeriodsPage, originalPeriods)
      val application = buildApp(Some(incompleteUa))
      running(application) {
        val request = FakeRequest(
          GET,
          controllers.subscription.manageAccount.routes.AmendAccountingPeriodConfirmationController.onPageLoad().url
        )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
