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

package controllers.rfm

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.GroupRegistrationDateReportFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.RfmRegistrationDatePage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.rfm.GroupRegistrationDateReportView

import java.time.LocalDate
import scala.concurrent.Future

class GroupRegistrationDateReportControllerSpec extends SpecBase {

  val formProvider = new GroupRegistrationDateReportFormProvider()
  val startDate: LocalDate = LocalDate.of(2023, 12, 31)
  "GroupRegistrationDateReport Controller" when {

    "must return OK and the correct view for Group Registration Date " in {
      val application = applicationBuilder(userAnswers = None)
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.GroupRegistrationDateReportController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[GroupRegistrationDateReportView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET if page has previously been answered" in {

      val ua = emptyUserAnswers.setOrException(RfmRegistrationDatePage, startDate)
      val application = applicationBuilder(Some(ua))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.GroupRegistrationDateReportController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[GroupRegistrationDateReportView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(startDate), NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must redirect to the group check your answers page when valid data is submitted" in {

      val application = applicationBuilder()
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.rfm.routes.GroupRegistrationDateReportController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(
            "rfmRegistrationDate.day"   -> "31",
            "rfmRegistrationDate.month" -> "12",
            "rfmRegistrationDate.year"  -> "2023"
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad(NormalMode).url
      }

    }

    "must redirect to the group check your answers page when valid data with Month in string short format is submitted" in {

      val application = applicationBuilder()
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.rfm.routes.GroupRegistrationDateReportController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(
            "rfmRegistrationDate.day"   -> "31",
            "rfmRegistrationDate.month" -> "Dec",
            "rfmRegistrationDate.year"  -> "2023"
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad(NormalMode).url
      }

    }

    "must redirect to the group check your answers page when valid data with Month in string format is submitted" in {

      val application = applicationBuilder()
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.rfm.routes.GroupRegistrationDateReportController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(
            "rfmRegistrationDate.day"   -> "31",
            "rfmRegistrationDate.month" -> "December",
            "rfmRegistrationDate.year"  -> "2023"
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad(NormalMode).url
      }

    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      val request =
        FakeRequest(POST, controllers.rfm.routes.GroupRegistrationDateReportController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = formProvider().bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[GroupRegistrationDateReportView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid string month is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .build()

      val request =
        FakeRequest(POST, controllers.rfm.routes.GroupRegistrationDateReportController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(
            "rfmRegistrationDate.day"   -> "31",
            "rfmRegistrationDate.month" -> "month",
            "rfmRegistrationDate.year"  -> "2023"
          )

      running(application) {
        val boundForm =
          formProvider().bind(Map("rfmRegistrationDate.day" -> "31", "rfmRegistrationDate.month" -> "month", "rfmRegistrationDate.year" -> "2023"))

        val view = application.injector.instanceOf[GroupRegistrationDateReportView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

  }
}
