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
import connectors.SubscriptionConnector
import models.subscription.{DisplayAccountingPeriod, SubscriptionLocalData}
import models.{InternalIssueError, MneOrDomestic}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import pages.SubAccountingPeriodPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future

class ManageGroupDetailsCheckYourAnswersControllerSpec extends SpecBase {

  private val amendablePeriod = DisplayAccountingPeriod(
    startDate = LocalDate.of(2025, 1, 1),
    endDate = LocalDate.of(2025, 12, 31),
    dueDate = LocalDate.of(2026, 3, 31),
    canAmendStartDate = true,
    canAmendEndDate = true
  )

  private val microPeriod = DisplayAccountingPeriod(
    startDate = LocalDate.of(2024, 4, 1),
    endDate = LocalDate.of(2024, 9, 30),
    dueDate = LocalDate.of(2024, 12, 31),
    canAmendStartDate = true,
    canAmendEndDate = true
  )

  private val nonAmendablePeriod = DisplayAccountingPeriod(
    startDate = LocalDate.of(2023, 1, 1),
    endDate = LocalDate.of(2023, 12, 31),
    dueDate = LocalDate.of(2024, 3, 31),
    canAmendStartDate = false,
    canAmendEndDate = false
  )

  private val localDataWithPeriods: SubscriptionLocalData =
    emptySubscriptionLocalData.copy(accountingPeriods = Some(Seq(amendablePeriod, microPeriod)))

  private val localDataWithMixedPeriods: SubscriptionLocalData =
    emptySubscriptionLocalData.copy(accountingPeriods = Some(Seq(amendablePeriod, nonAmendablePeriod)))

  private val localDataWithNoAmendablePeriods: SubscriptionLocalData =
    emptySubscriptionLocalData.copy(accountingPeriods = Some(Seq(nonAmendablePeriod)))

  private val localDataWithoutPeriods: SubscriptionLocalData =
    emptySubscriptionLocalData.copy(accountingPeriods = None)

  private def buildApp(
    subscriptionLocalData: Option[SubscriptionLocalData],
    multiPeriodFlag:       Boolean = false
  ) =
    applicationBuilder(
      subscriptionLocalData = subscriptionLocalData,
      additionalData = Map("features.amendMultipleAccountingPeriods" -> multiPeriodFlag)
    )
      .overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[SubscriptionService].toInstance(mockSubscriptionService),
        bind[SubscriptionConnector].toInstance(mockSubscriptionConnector)
      )
      .build()

  "onPageLoad" when {

    "no subscription cache is present" must {
      "redirect to Journey Recovery" in {
        val application = buildApp(subscriptionLocalData = None)
        running(application) {
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }
    }

    "subscription cache present and sessionRepository returns None" must {
      "redirect to Journey Recovery" in {
        val application = buildApp(subscriptionLocalData = Some(emptySubscriptionLocalData))
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }
    }

    "feature flag is false" must {
      "render the single-period CYA view" in {
        val application = buildApp(subscriptionLocalData = Some(emptySubscriptionLocalData), multiPeriodFlag = false)
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Group details")
          contentAsString(result) must not include "Accounting periods"
        }
      }
    }

    "feature flag is true and accountingPeriods already cached" must {
      "render the multi-period view without calling the V2 service" in {
        val application = buildApp(subscriptionLocalData = Some(localDataWithPeriods), multiPeriodFlag = true)
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Accounting periods")
          contentAsString(result) must include("Current period")
          contentAsString(result) must include("Previous period")
        }
      }

      "show cards and Change links only for amendable periods" in {
        val application = buildApp(subscriptionLocalData = Some(localDataWithMixedPeriods), multiPeriodFlag = true)
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          val body    = contentAsString(result)
          body must include("select-period/0")
          body must not include "31 December 2023"
          body must not include "select-period/1"
        }
      }
    }

    "feature flag is true and accountingPeriods not yet cached" must {
      "call fetchDisplaySubscriptionV2AndSave and render multi-period view" in {
        val application = buildApp(subscriptionLocalData = Some(localDataWithoutPeriods), multiPeriodFlag = true)
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockSubscriptionService.fetchDisplaySubscriptionV2AndSave(any(), any())(using any()))
            .thenReturn(Future.successful(localDataWithPeriods))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Accounting periods")
        }
      }

      "fall back to single-period view when V2 service fails" in {
        val application = buildApp(subscriptionLocalData = Some(localDataWithoutPeriods), multiPeriodFlag = true)
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockSubscriptionService.fetchDisplaySubscriptionV2AndSave(any(), any())(using any()))
            .thenReturn(Future.failed(InternalIssueError))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("Group details")
          contentAsString(result) must not include "Accounting periods"
        }
      }
    }

    "feature flag is true and there are no amendable periods" must {
      "render empty state message" in {
        val application = buildApp(subscriptionLocalData = Some(localDataWithNoAmendablePeriods), multiPeriodFlag = true)
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("There are no accounting periods available to amend")
          contentAsString(result) must not include "select-period/"
        }
      }
    }

    "location rendering" must {
      "show UK and non-UK location text when feature flag is on" in {
        val ukAndOtherData = localDataWithPeriods.copy(subMneOrDomestic = MneOrDomestic.UkAndOther)
        val application    = buildApp(subscriptionLocalData = Some(ukAndOtherData), multiPeriodFlag = true)
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("In the UK and outside the UK")
        }
      }

      "show UK and non-UK location text when feature flag is off" in {
        val ukAndOtherData = emptySubscriptionLocalData.copy(subMneOrDomestic = MneOrDomestic.UkAndOther)
        val application    = buildApp(subscriptionLocalData = Some(ukAndOtherData), multiPeriodFlag = false)
        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url)
          val result  = route(application, request).value
          status(result) mustEqual OK
          contentAsString(result) must include("In the UK and outside the UK")
        }
      }
    }

  }

  "selectPeriod" when {

    "a valid index is provided" must {
      "save the selected period to cache and redirect to GroupAccountingPeriodController" in {
        val application = buildApp(subscriptionLocalData = Some(localDataWithPeriods), multiPeriodFlag = true)
        running(application) {
          val expectedUpdated = localDataWithPeriods
            .setOrException(SubAccountingPeriodPage, amendablePeriod.toAccountingPeriod)
          when(mockSubscriptionConnector.save(eqTo("id"), any())(using any()))
            .thenReturn(Future.successful(Json.toJson(expectedUpdated)))

          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.selectPeriod(0).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(
            controllers.subscription.manageAccount.routes.GroupAccountingPeriodController.onPageLoad().url
          )
          verify(mockSubscriptionConnector).save(eqTo("id"), any())(using any[HeaderCarrier])
        }
      }
    }

    "an out-of-range index is provided" must {
      "redirect to Journey Recovery" in {
        val application = buildApp(subscriptionLocalData = Some(localDataWithPeriods), multiPeriodFlag = true)
        running(application) {
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.selectPeriod(99).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }
    }

    "accountingPeriods is empty" must {
      "redirect to Journey Recovery" in {
        val application = buildApp(subscriptionLocalData = Some(emptySubscriptionLocalData), multiPeriodFlag = true)
        running(application) {
          val request = FakeRequest(GET, routes.ManageGroupDetailsCheckYourAnswersController.selectPeriod(0).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
        }
      }
    }
  }
}
