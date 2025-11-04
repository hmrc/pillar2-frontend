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

package controllers.btn

import base.SpecBase
import models.obligationsandsubmissions._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.BTNChooseAccountingPeriodPage
import play.api.inject._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.ObligationsAndSubmissionsService
import utils.DateTimeUtils.LocalDateOps
import views.html.btn.BTNConfirmationView

import java.time.{LocalDate, ZonedDateTime}
import scala.concurrent.Future

class BTNConfirmationControllerSpec extends SpecBase {

  "BTNConfirmationController" when {

    "onPageLoad" should {

      "must return OK and the correct view for a GET" in {
        val obligationsData = ObligationsAndSubmissionsSuccess(
          processingDate = ZonedDateTime.now(),
          accountingPeriodDetails = Seq.empty
        )

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), subscriptionLocalData = Some(someSubscriptionLocalData))
          .configure("features.phase2ScreensEnabled" -> true)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
          )
          .build()

        val currentDate: String = LocalDate.now.toDateFormat
        val date:        String = someSubscriptionLocalData.subAccountingPeriod.startDate.toDateFormat

        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any()))
            .thenReturn(Future.successful(obligationsData))

          val request = FakeRequest(GET, controllers.btn.routes.BTNConfirmationController.onPageLoad.url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[BTNConfirmationView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(Some("OrgName"), currentDate, date, isAgent = false, showUnderEnquiryWarning = false)(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }

      "must show underEnquiry warning when chosen accounting period has underEnquiry flag set to true" in {
        val chosenPeriod = AccountingPeriodDetails(
          startDate = LocalDate.now().minusYears(1),
          endDate = LocalDate.now(),
          dueDate = LocalDate.now().plusMonths(6),
          underEnquiry = true,
          obligations = Seq(Obligation(ObligationType.UKTR, ObligationStatus.Open, canAmend = true, Seq.empty))
        )

        val userAnswers = emptyUserAnswers.setOrException(BTNChooseAccountingPeriodPage, chosenPeriod)

        val obligationsData = ObligationsAndSubmissionsSuccess(
          processingDate = ZonedDateTime.now(),
          accountingPeriodDetails = Seq(chosenPeriod)
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers), subscriptionLocalData = Some(someSubscriptionLocalData))
          .configure("features.phase2ScreensEnabled" -> true)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
          )
          .build()

        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
          when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any()))
            .thenReturn(Future.successful(obligationsData))

          val request = FakeRequest(GET, controllers.btn.routes.BTNConfirmationController.onPageLoad.url)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[BTNConfirmationView]
          val currentDate: String = LocalDate.now.toDateFormat
          val date:        String = someSubscriptionLocalData.subAccountingPeriod.startDate.toDateFormat

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(Some("OrgName"), currentDate, date, isAgent = false, showUnderEnquiryWarning = true)(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }

      "must show underEnquiry warning when a subsequent accounting period has underEnquiry flag set to true" in {
        val subsequentPeriod = AccountingPeriodDetails(
          startDate = LocalDate.now().minusMonths(6),
          endDate = LocalDate.now().plusMonths(6),
          dueDate = LocalDate.now().plusMonths(12),
          underEnquiry = true,
          obligations = Seq(Obligation(ObligationType.UKTR, ObligationStatus.Open, canAmend = true, Seq.empty))
        )

        val chosenPeriod = AccountingPeriodDetails(
          startDate = LocalDate.now().minusYears(1),
          endDate = LocalDate.now().minusMonths(6),
          dueDate = LocalDate.now(),
          underEnquiry = false,
          obligations = Seq(Obligation(ObligationType.UKTR, ObligationStatus.Open, canAmend = true, Seq.empty))
        )

        val userAnswers = emptyUserAnswers.setOrException(BTNChooseAccountingPeriodPage, chosenPeriod)

        val obligationsData = ObligationsAndSubmissionsSuccess(
          processingDate = ZonedDateTime.now(),
          accountingPeriodDetails = Seq(subsequentPeriod, chosenPeriod)
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers), subscriptionLocalData = Some(someSubscriptionLocalData))
          .configure("features.phase2ScreensEnabled" -> true)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
          )
          .build()

        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
          when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any()))
            .thenReturn(Future.successful(obligationsData))

          val request = FakeRequest(GET, controllers.btn.routes.BTNConfirmationController.onPageLoad.url)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[BTNConfirmationView]
          val currentDate: String = LocalDate.now.toDateFormat
          val date:        String = someSubscriptionLocalData.subAccountingPeriod.startDate.toDateFormat

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(Some("OrgName"), currentDate, date, isAgent = false, showUnderEnquiryWarning = true)(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }

      "must not show underEnquiry warning when neither chosen nor subsequent periods have underEnquiry flag set" in {
        val chosenPeriod = AccountingPeriodDetails(
          startDate = LocalDate.now().minusYears(1),
          endDate = LocalDate.now(),
          dueDate = LocalDate.now().plusMonths(6),
          underEnquiry = false,
          obligations = Seq(Obligation(ObligationType.UKTR, ObligationStatus.Open, canAmend = true, Seq.empty))
        )

        val subsequentPeriod = AccountingPeriodDetails(
          startDate = LocalDate.now().minusYears(2),
          endDate = LocalDate.now().minusYears(1),
          dueDate = LocalDate.now().minusMonths(6),
          underEnquiry = false,
          obligations = Seq(Obligation(ObligationType.UKTR, ObligationStatus.Open, canAmend = true, Seq.empty))
        )

        val userAnswers = emptyUserAnswers.setOrException(BTNChooseAccountingPeriodPage, chosenPeriod)

        val obligationsData = ObligationsAndSubmissionsSuccess(
          processingDate = ZonedDateTime.now(),
          accountingPeriodDetails = Seq(chosenPeriod, subsequentPeriod)
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers), subscriptionLocalData = Some(someSubscriptionLocalData))
          .configure("features.phase2ScreensEnabled" -> true)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
          )
          .build()

        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(userAnswers)))
          when(mockObligationsAndSubmissionsService.handleData(any(), any(), any())(any()))
            .thenReturn(Future.successful(obligationsData))

          val request = FakeRequest(GET, controllers.btn.routes.BTNConfirmationController.onPageLoad.url)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[BTNConfirmationView]
          val currentDate: String = LocalDate.now.toDateFormat
          val date:        String = someSubscriptionLocalData.subAccountingPeriod.startDate.toDateFormat

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(Some("OrgName"), currentDate, date, isAgent = false, showUnderEnquiryWarning = false)(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }

      "must redirect to dashboard for onPageLoad when phase2ScreensEnabled is false" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .configure("features.phase2ScreensEnabled" -> false)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ObligationsAndSubmissionsService].toInstance(mockObligationsAndSubmissionsService)
          )
          .build()

        running(application) {
          when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))

          val request = FakeRequest(GET, controllers.btn.routes.BTNConfirmationController.onPageLoad.url)
          val result  = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.HomepageController.onPageLoad.url
        }
      }
    }
  }
}
