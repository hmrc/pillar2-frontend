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

package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.registrationview.{RegistrationFailedNfmView, RegistrationFailedRfmView, RegistrationFailedUpeView}

class GrsRegistrationFailedControllerSpec extends SpecBase {

  "GrsRegistrationFailed  Controller" when {

    "must return OK and the correct view for a GET for UPE" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.GrsRegistrationFailedController.onPageLoadUpe.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RegistrationFailedUpeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET for NFM" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.GrsRegistrationFailedController.onPageLoadNfm.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RegistrationFailedNfmView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET for RFM" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.GrsRegistrationFailedController.onPageLoadRfm.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RegistrationFailedRfmView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString
      }
    }
  }
}
