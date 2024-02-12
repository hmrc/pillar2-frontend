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

package controllers.eligibility

import base.SpecBase
import forms.GroupTerritoriesFormProvider
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.GroupTerritoriesView

class GroupTerritoriesControllerSpec extends SpecBase {

  val formProvider = new GroupTerritoriesFormProvider()

  "Group Territories Controller" when {

    "must return OK and the correct view for a GET when page previously not answered" in {
      val application = applicationBuilder(None).build()
      running(application) {
        val request =
          FakeRequest(GET, controllers.eligibility.routes.GroupTerritoriesController.onPageLoad.url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[GroupTerritoriesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider())(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val application = applicationBuilder(None).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.eligibility.routes.GroupTerritoriesController.onSubmit.url).withFormUrlEncodedBody(
            "value" -> "true"
          )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.eligibility.routes.BusinessActivityUKController.onPageLoad.url

      }
    }
    "must redirect to the next page when valid data is submitted with no selected" in {
      val application = applicationBuilder(None).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.eligibility.routes.GroupTerritoriesController.onSubmit.url).withFormUrlEncodedBody(
            "value" -> "false"
          )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.eligibility.routes.RegisteringNfmForThisGroupController.onPageLoad.url

      }
    }
    "return  BAD_REQUEST if invalid data is submitted " in {
      val application = applicationBuilder(None).build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.eligibility.routes.GroupTerritoriesController.onSubmit.url).withFormUrlEncodedBody(
            "value" -> ""
          )
        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
      }
    }
  }
}
