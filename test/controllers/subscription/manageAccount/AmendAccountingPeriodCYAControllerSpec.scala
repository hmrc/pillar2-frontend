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
import models.subscription.{AccountingPeriod, AccountingPeriodV2}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.NewAccountingPeriodPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository

import java.time.LocalDate
import scala.concurrent.Future

class AmendAccountingPeriodCYAControllerSpec extends SpecBase {

  private val newPeriod: AccountingPeriod =
    AccountingPeriod(LocalDate.of(2021, 9, 28), LocalDate.of(2022, 10, 3))

  // Two original periods — Example 2 scenario from PIL-2857
  private val allPeriods: Seq[AccountingPeriodV2] = Seq(
    AccountingPeriodV2(
      LocalDate.of(2021, 9, 28),
      LocalDate.of(2022, 9, 27),
      LocalDate.of(2022, 12, 31),
      canAmendStartDate = false,
      canAmendEndDate = true
    ),
    AccountingPeriodV2(
      LocalDate.of(2022, 9, 28),
      LocalDate.of(2023, 9, 27),
      LocalDate.of(2023, 12, 31),
      canAmendStartDate = true,
      canAmendEndDate = true
    )
  )

  private def baseLocalData =
    emptySubscriptionLocalData.copy(accountingPeriods = Some(allPeriods))

  private def buildApp(userAnswers: Option[UserAnswers], localData: Option[models.subscription.SubscriptionLocalData] = Some(baseLocalData)) = {
    when(mockSessionRepository.get(any())).thenReturn(Future.successful(userAnswers))
    applicationBuilder(
      subscriptionLocalData = localData,
      additionalData = Map("features.amendMultipleAccountingPeriods" -> true)
    ).overrides(
      bind[SessionRepository].toInstance(mockSessionRepository)
    ).build()
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

    "renders CYA page with new period, duration and predicted micro-period (Scenario 3)" in {
      // New period: 2021-09-28 → 2022-10-03
      // Gap after: 2022-10-04 → 2023-09-27
      val ua          = UserAnswers("id").setOrException(NewAccountingPeriodPage, newPeriod)
      val application = buildApp(userAnswers = Some(ua))
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.AmendAccountingPeriodCYAController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        val body = contentAsString(result)
        body must include(messages(application)("amendAccountingPeriodCYA.heading"))
        body must include(messages(application)("amendAccountingPeriodCYA.newPeriod.title"))
        body must include("11")
        body must include("24")
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
        body must not include messages(application)("amendAccountingPeriodCYA.predictedPeriod.duration")
      }
    }

    "redirects to journey recovery when feature flag is off" in {
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
      val application = applicationBuilder(
        subscriptionLocalData = Some(baseLocalData),
        additionalData = Map("features.amendMultipleAccountingPeriods" -> false)
      ).overrides(bind[SessionRepository].toInstance(mockSessionRepository)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.AmendAccountingPeriodCYAController.onPageLoad().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.HomepageController.onPageLoad().url
      }
    }
  }

  "AmendAccountingPeriodCYAController onSubmit" when {

    "redirects to journey recovery (submission deferred to Ticket 7)" in {
      val ua          = UserAnswers("id").setOrException(NewAccountingPeriodPage, newPeriod)
      val application = buildApp(userAnswers = Some(ua))
      running(application) {
        val request = FakeRequest(POST, controllers.subscription.manageAccount.routes.AmendAccountingPeriodCYAController.onSubmit().url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
