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
import models.rfm.RegistrationDate
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
  val startDate    = LocalDate.of(2023, 12, 31)
  "GroupRegistrationDateReport Controller" when {

    "must redirect to correct view when rfm feature false" in {

      val application = applicationBuilder(userAnswers = None)
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> false
          ): _*
        )
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.GroupRegistrationDateReportController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }

    }

    "must return OK and the correct view for Group Registration Date " in {
      val application = applicationBuilder(userAnswers = None)
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.GroupRegistrationDateReportController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[GroupRegistrationDateReportView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET if page has previously been answered" in {

      val date = RegistrationDate(startDate)
      val ua   = emptyUserAnswers.setOrException(RfmRegistrationDatePage, date)
      val application = applicationBuilder(Some(ua))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.GroupRegistrationDateReportController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[GroupRegistrationDateReportView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(date), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must redirect to the group check your answers page when valid data is submitted" in {

      val application = applicationBuilder()
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> true
          ): _*
        )
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.rfm.routes.GroupRegistrationDateReportController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(
            "rfmRegistrationDate.day"   -> "31",
            "rfmRegistrationDate.month" -> "12",
            "rfmRegistrationDate.year"  -> "2023"
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }

    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> true
          ): _*
        )
        .build()

      val request =
        FakeRequest(POST, controllers.rfm.routes.GroupRegistrationDateReportController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = formProvider().bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[GroupRegistrationDateReportView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

  }
}
