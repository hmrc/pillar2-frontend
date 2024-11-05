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

package controllers.bta

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.bta.EACDStubView

class EACDStubControllerSpec extends SpecBase {

  "EACD Stub Controller" must {

    "must return OK and the correct view for a GET - bta feature true" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure(
          Seq(
            "features.btaAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.bta.routes.EACDStubController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EACDStubView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, appConfig(), messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET - bta feature false" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure(
          Seq(
            "features.btaAccessEnabled" -> false
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.bta.routes.EACDStubController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.ErrorController.pageNotFoundLoad.url
      }
    }

  }
}
