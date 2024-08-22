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
import play.api.test.Helpers._
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
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
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
        contentAsString(result) mustEqual view(formProvider().fill(date), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must redirect to next page accounting period when valid data is submitted" in {
      val ua = emptyUserAnswers
        .setOrException(SubPrimaryContactNamePage, "TestName")

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.GroupAccountingPeriodController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              "startDate.day"   -> "1",
              "startDate.month" -> "1",
              "startDate.year"  -> "2024",
              "endDate.day"     -> "1",
              "endDate.month"   -> "6",
              "endDate.year"    -> "2024"
            )

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.GroupDetailCheckYourAnswersController.onPageLoad.url
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
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

  }
}
