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
import forms.NewAccountingPeriodFormProvider
import generators.Generators
import models.subscription.*
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.NewAccountingPeriodPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.subscriptionview.manageAccount.NewAccountingPeriodView

import java.time.LocalDate
import scala.concurrent.Future

class NewAccountingPeriodControllerSpec extends SpecBase with Generators {

  val formProvider = new NewAccountingPeriodFormProvider()
  val startDate:    LocalDate = LocalDate.now
  val endDate:      LocalDate = LocalDate.now.plusYears(1)
  val plrReference: String    = "XMPLR0123456789"

  private val amendablePeriod = AccountingPeriodV2(
    startDate = LocalDate.of(2025, 7, 18),
    endDate = LocalDate.of(2025, 12, 31),
    dueDate = LocalDate.of(2026, 3, 31),
    canAmendStartDate = true,
    canAmendEndDate = true
  )

  val chosenAccountingPeriod: ChosenAccountingPeriod = ChosenAccountingPeriod(
    amendablePeriod.toAccountingPeriod,
    None,
    None
  )

  private val localDataWithAmendablePeriods: SubscriptionLocalData =
    emptySubscriptionLocalData.copy(subAccountingPeriod = Some(amendablePeriod.toAccountingPeriod), accountingPeriods = Some(Seq(amendablePeriod)))

  "NewAccountingPeriod Controller" must {

    "redirect to homepage when amendMultipleAccountingPeriods is false" in {
      val application = applicationBuilder(subscriptionLocalData = None)
        .configure("features.amendMultipleAccountingPeriods" -> false)
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.NewAccountingPeriodController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.HomepageController.onPageLoad().url)
      }
    }

    "allow request when amendMultipleAccountingPeriods is true and" must {
      "redirect to Journey Recovery" when {
        "no subscription cache is present" in {
          val application = applicationBuilder(subscriptionLocalData = None)
            .configure("features.amendMultipleAccountingPeriods" -> true)
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.NewAccountingPeriodController.onPageLoad(NormalMode).url)
            val result  = route(application, request).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
          }
        }

        "no user selected accounting period present" in {
          val subscriptionDataNoSelectedPeriod = someSubscriptionLocalData.copy(subAccountingPeriod = None)

          val application = applicationBuilder(subscriptionLocalData = Some(subscriptionDataNoSelectedPeriod))
            .configure("features.amendMultipleAccountingPeriods" -> true)
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.NewAccountingPeriodController.onPageLoad(NormalMode).url)
            val result  = route(application, request).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
          }
        }
      }

      "return OK and the correct view for a GET if no previous data is found" in {
        val application = applicationBuilder(subscriptionLocalData = Some(localDataWithAmendablePeriods))
          .configure("features.amendMultipleAccountingPeriods" -> true)
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.NewAccountingPeriodController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[NewAccountingPeriodView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            formProvider(chosenAccountingPeriod),
            chosenAccountingPeriod,
            isAgent = false,
            organisationName = None,
            plrReference = plrReference,
            mode = NormalMode
          )(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }

      "return OK and the correct view for a GET when a previous answer exists" in {
        val accountingPeriod = AccountingPeriod(startDate, endDate)
        val ua               = emptyUserAnswers
          .set(NewAccountingPeriodPage, accountingPeriod)
          .success
          .value

        val application = applicationBuilder(subscriptionLocalData = Some(localDataWithAmendablePeriods), userAnswers = Some(ua))
          .configure("features.amendMultipleAccountingPeriods" -> true)
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.NewAccountingPeriodController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value

          val view = application.injector.instanceOf[NewAccountingPeriodView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            formProvider(chosenAccountingPeriod),
            chosenAccountingPeriod,
            isAgent = false,
            organisationName = None,
            plrReference = plrReference,
            mode = NormalMode
          )(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }

      "must save and redirect to the check your answers page when a valid answer is submitted" in {
        val application = applicationBuilder(subscriptionLocalData = Some(localDataWithAmendablePeriods))
          .configure("features.amendMultipleAccountingPeriods" -> true)
          .build()

        running(application) {
          when(mockSessionRepository.get(any())) thenReturn Future.successful(Some(emptyUserAnswers))
          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val request =
            FakeRequest(POST, routes.NewAccountingPeriodController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody(
                "startDate.day"   -> "1",
                "startDate.month" -> "1",
                "startDate.year"  -> "2024",
                "endDate.day"     -> "31",
                "endDate.month"   -> "12",
                "endDate.year"    -> "2025"
              )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.subscription.manageAccount.routes.AmendAccountingPeriodCYAController.onPageLoad().url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        val application = applicationBuilder(subscriptionLocalData = Some(localDataWithAmendablePeriods))
          .configure("features.amendMultipleAccountingPeriods" -> true)
          .build()

        val request =
          FakeRequest(POST, routes.NewAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "invalid value"))

        running(application) {
          val boundForm = formProvider(chosenAccountingPeriod).bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[NewAccountingPeriodView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(
            boundForm,
            chosenAccountingPeriod = chosenAccountingPeriod,
            isAgent = false,
            organisationName = None,
            plrReference = plrReference,
            mode = NormalMode
          )(
            request,
            applicationConfig,
            messages(application)
          ).toString
        }
      }
    }
  }
}
