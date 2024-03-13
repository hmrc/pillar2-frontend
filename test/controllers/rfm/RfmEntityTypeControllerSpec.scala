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
import controllers.routes
import forms.RfmEntityTypeFormProvider
import models.{NormalMode, UserAnswers}
import models.grs.{EntityType, RfmEntityType}
import pages.{RfmEntityTypePage, fmEntityTypePage, fmRegisteredInUKPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.RfmEntityTypeView

class RfmEntityTypeControllerSpec extends SpecBase {

  val formProvider = new RfmEntityTypeFormProvider()

  "RfmEntityTypeController Controller" when {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmEntityTypeController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RfmEntityTypeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(RfmEntityTypePage, RfmEntityType.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmEntityTypeController.onPageLoad(NormalMode).url)

        val view = application.injector.instanceOf[RfmEntityTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(RfmEntityType.values.head), NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been not answered" in {

      val ua = UserAnswers(userAnswersId).set(RfmEntityTypePage, RfmEntityType.values.head).success.value
      val userAnswers =
        ua.setOrException(RfmEntityTypePage, RfmEntityType.UkLimitedCompany)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmEntityTypeController.onPageLoad(NormalMode).url)

        val view = application.injector.instanceOf[RfmEntityTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(RfmEntityType.values.head), NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }
    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmEntityTypeController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = formProvider().bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[RfmEntityTypeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

  }
}
