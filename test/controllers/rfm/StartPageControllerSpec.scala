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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.rfm.StartPageView

class StartPageControllerSpec extends SpecBase {

  "StartPage Controller" when {

    "must return OK and the correct view when rfm feature true" in {

      val application = applicationBuilder(userAnswers = None)
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> true
          ): _*
        )
        .build()
      running(application) {
        val request = FakeRequest(GET, routes.StartPageController.onPageLoad.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[StartPageView]
        status(result) mustEqual OK
        val content = contentAsString(result)
        content mustEqual view()(request, appConfig(application), messages(application)).toString
      }

    }

    "must redirect to correct view when rfm feature false" in {

      val application = applicationBuilder(userAnswers = None)
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> false
          ): _*
        )
        .build()
      running(application) {
        val request = FakeRequest(GET, routes.StartPageController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ErrorController.pageNotFoundLoad.url
      }

    }

  }

}
