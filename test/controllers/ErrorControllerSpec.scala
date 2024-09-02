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

class ErrorControllerSpec extends SpecBase {

  "Error Controller" when {
    "must return not found error view for a GET " in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(GET, controllers.routes.ErrorController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual 404
        contentAsString(result) must include("There is problem with the page.")
      }
    }

    "must return not found load error view for a GET " in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(GET, controllers.routes.ErrorController.pageNotFoundLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual 404
        contentAsString(result) must include("Page not found")
        contentAsString(result) must include("If you typed the web address, check it is correct.")
        contentAsString(result) must include("If you pasted the web address, check you copied the entire address.")
      }
    }

  }

}
