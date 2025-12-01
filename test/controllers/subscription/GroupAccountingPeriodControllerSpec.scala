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

package controllers.subscription

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.GroupAccountingPeriodFormProvider
import models.subscription.AccountingPeriod
import models.{MneOrDomestic, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{SubAccountingPeriodPage, SubMneOrDomesticPage, SubPrimaryContactNamePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.subscriptionview.GroupAccountingPeriodView

import java.time.LocalDate
import scala.concurrent.Future
class GroupAccountingPeriodControllerSpec extends SpecBase {

  val formProvider = new GroupAccountingPeriodFormProvider()
  val startDate: LocalDate = LocalDate.of(2023, 12, 31)
  val endDate:   LocalDate = LocalDate.of(2025, 12, 31)
  "GroupAccountingPeriod Controller" when {

    "must return OK and the correct view for a GET if no previous data is found" in {
      val ua = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)

      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.GroupAccountingPeriodController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[GroupAccountingPeriodView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET if page has previously been answered" in {

      val date        = AccountingPeriod(startDate, endDate)
      val ua          = emptyUserAnswers.setOrException(SubAccountingPeriodPage, date).setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.GroupAccountingPeriodController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[GroupAccountingPeriodView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(date), NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "must redirect to next page accounting period when valid data is submitted" in {
      val ua = emptyUserAnswers
        .setOrException(SubPrimaryContactNamePage, "TestName")

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(using any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "1",
              "startDate.month" -> "1",
              "startDate.year"  -> "2024",
              "endDate.day"     -> "1",
              "endDate.month"   -> "06",
              "endDate.year"    -> "2024"
            )

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.GroupDetailCheckYourAnswersController.onPageLoad().url
      }
    }

    "must redirect to next page accounting period when valid data with string month is submitted" in {
      val ua = emptyUserAnswers
        .setOrException(SubPrimaryContactNamePage, "TestName")

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(using any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "1",
              "startDate.month" -> "Jan",
              "startDate.year"  -> "2024",
              "endDate.day"     -> "1",
              "endDate.month"   -> "June",
              "endDate.year"    -> "2024"
            )

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.GroupDetailCheckYourAnswersController.onPageLoad().url
      }
    }

    "redirect to bookmark page if previous page not answered" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.GroupAccountingPeriodController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = formProvider().bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[GroupAccountingPeriodView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid string month is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      val request =
        FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(
            "startDate.day"   -> "1",
            "startDate.month" -> "month",
            "startDate.year"  -> "2024",
            "endDate.day"     -> "1",
            "endDate.month"   -> "month",
            "endDate.year"    -> "2024"
          )

      running(application) {
        val boundForm =
          formProvider().bind(
            Map(
              "startDate.day"   -> "1",
              "startDate.month" -> "month",
              "startDate.year"  -> "2024",
              "endDate.day"     -> "1",
              "endDate.month"   -> "month",
              "endDate.year"    -> "2024"
            )
          )

        val view = application.injector.instanceOf[GroupAccountingPeriodView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "must return Bad Request and show specific error message when both start and end date are missing" in {
      val ua          = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "",
              "startDate.month" -> "",
              "startDate.year"  -> "",
              "endDate.day"     -> "",
              "endDate.month"   -> "",
              "endDate.year"    -> ""
            )
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Enter the start date of the group’s accounting period")
        contentAsString(result) must include("Enter the end date of the group’s accounting period")
      }
    }

    "must return Bad Request and show specific error message when start date is before minimum" in {
      val ua          = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "30",
              "startDate.month" -> "12",
              "startDate.year"  -> "2023",
              "endDate.day"     -> "31",
              "endDate.month"   -> "12",
              "endDate.year"    -> "2023"
            )
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Start date must be on or after 31 December 2023")
      }
    }

    "must return Bad Request and show specific error message when end date is before start date" in {
      val ua          = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "15",
              "startDate.month" -> "1",
              "startDate.year"  -> "2024",
              "endDate.day"     -> "14",
              "endDate.month"   -> "1",
              "endDate.year"    -> "2024"
            )
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("End date must be after the start date")
      }
    }

    "must return Bad Request and show specific error message when dates are invalid" in {
      val ua          = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "1",
              "startDate.month" -> "15",
              "startDate.year"  -> "2024",
              "endDate.day"     -> "12",
              "endDate.month"   -> "20",
              "endDate.year"    -> "2024"
            )
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Start date must be a real date")
        contentAsString(result) must include("End date must be a real date")
      }
    }

    "must return Bad Request and show mixed error messages for invalid start date and empty end date" in {
      val ua          = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "1",
              "startDate.month" -> "15", // Invalid month
              "startDate.year"  -> "2024",
              "endDate.day"     -> "", // Empty end date
              "endDate.month"   -> "",
              "endDate.year"    -> ""
            )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Start date must be a real date")
        contentAsString(result) must include("Enter the end date")
      }
    }

    "must return Bad Request and show mixed error messages for invalid start day and invalid end date" in {
      val ua          = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "AA", // Invalid day
              "startDate.month" -> "12",
              "startDate.year"  -> "2024",
              "endDate.day"     -> "12",
              "endDate.month"   -> "20", // Invalid month
              "endDate.year"    -> "2024"
            )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Start date must be a real date")
        contentAsString(result) must include("End date must be a real date")
      }
    }

    "must return Bad Request and show specific error message when start date missing month and year" in {
      val ua          = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "30",
              "startDate.month" -> "",
              "startDate.year"  -> "",
              "endDate.day"     -> "",
              "endDate.month"   -> "",
              "endDate.year"    -> ""
            )
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Start date must include a month and year")
        contentAsString(result) must include("Enter the end date of the group’s accounting period")
      }
    }

    "must return Bad Request and show specific error message when start date missing day and year" in {
      val ua          = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "",
              "startDate.month" -> "01",
              "startDate.year"  -> "",
              "endDate.day"     -> "",
              "endDate.month"   -> "",
              "endDate.year"    -> ""
            )
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Start date must include a day and year")
        contentAsString(result) must include("Enter the end date of the group’s accounting period")
      }
    }

    "must return Bad Request and show specific error message when start date missing day and month" in {
      val ua          = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "",
              "startDate.month" -> "",
              "startDate.year"  -> "2024",
              "endDate.day"     -> "",
              "endDate.month"   -> "",
              "endDate.year"    -> ""
            )
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Start date must include a day and month")
        contentAsString(result) must include("Enter the end date of the group’s accounting period")
      }
    }

    "must return Bad Request and show specific error message when start date missing only day" in {
      val ua          = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "",
              "startDate.month" -> "12",
              "startDate.year"  -> "2024",
              "endDate.day"     -> "",
              "endDate.month"   -> "",
              "endDate.year"    -> ""
            )
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Start date must include a day")
        contentAsString(result) must include("Enter the end date of the group’s accounting period")
      }
    }

    "must return Bad Request and show specific error message when start date missing only month" in {
      val ua          = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "10",
              "startDate.month" -> "",
              "startDate.year"  -> "2024",
              "endDate.day"     -> "",
              "endDate.month"   -> "",
              "endDate.year"    -> ""
            )
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Start date must include a month")
        contentAsString(result) must include("Enter the end date of the group’s accounting period")
      }
    }

    "must return Bad Request and show specific error message when start date missing only year" in {
      val ua          = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "10",
              "startDate.month" -> "12",
              "startDate.year"  -> "",
              "endDate.day"     -> "",
              "endDate.month"   -> "",
              "endDate.year"    -> ""
            )
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Start date must include a year")
        contentAsString(result) must include("Enter the end date of the group’s accounting period")
      }
    }

    "must return Bad Request and show specific error message when end date missing month and year" in {
      val ua          = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "",
              "startDate.month" -> "",
              "startDate.year"  -> "",
              "endDate.day"     -> "12",
              "endDate.month"   -> "",
              "endDate.year"    -> ""
            )
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Enter the start date of the group’s accounting period")
        contentAsString(result) must include("End date must include a month and year")
      }
    }

    "must return Bad Request and show specific error message when end date missing day and year" in {
      val ua          = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "",
              "startDate.month" -> "",
              "startDate.year"  -> "",
              "endDate.day"     -> "",
              "endDate.month"   -> "10",
              "endDate.year"    -> ""
            )
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Enter the start date of the group’s accounting period")
        contentAsString(result) must include("End date must include a day and year")
      }
    }

    "must return Bad Request and show specific error message when end date missing day and month" in {
      val ua          = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "",
              "startDate.month" -> "",
              "startDate.year"  -> "",
              "endDate.day"     -> "",
              "endDate.month"   -> "",
              "endDate.year"    -> "2024"
            )
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Enter the start date of the group’s accounting period")
        contentAsString(result) must include("End date must include a day and month")
      }
    }

    "must return Bad Request and show specific error message when end date missing only day" in {
      val ua          = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "",
              "startDate.month" -> "",
              "startDate.year"  -> "",
              "endDate.day"     -> "",
              "endDate.month"   -> "10",
              "endDate.year"    -> "2024"
            )
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Enter the start date of the group’s accounting period")
        contentAsString(result) must include("End date must include a day")
      }
    }

    "must return Bad Request and show specific error message when end date missing only month" in {
      val ua          = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "",
              "startDate.month" -> "",
              "startDate.year"  -> "",
              "endDate.day"     -> "40",
              "endDate.month"   -> "",
              "endDate.year"    -> "2024"
            )
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Enter the start date of the group’s accounting period")
        contentAsString(result) must include("End date must include a month")
      }
    }

    "must return Bad Request and show specific error message when end date missing only year" in {
      val ua          = emptyUserAnswers.setOrException(SubMneOrDomesticPage, MneOrDomestic.Uk)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "",
              "startDate.month" -> "",
              "startDate.year"  -> "",
              "endDate.day"     -> "40",
              "endDate.month"   -> "10",
              "endDate.year"    -> ""
            )
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Enter the start date of the group’s accounting period")
        contentAsString(result) must include("End date must include a year")
      }
    }
  }
}
