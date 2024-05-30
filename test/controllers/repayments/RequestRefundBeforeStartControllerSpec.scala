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

package controllers.repayments

import base.SpecBase
import models.UserAnswers
import play.api.Configuration
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.payment.RequestRefundBeforeStartView

class RequestRefundBeforeStartControllerSpec extends SpecBase {

  "Rfm Save Progress inform Controller" when {

    "return OK and the correct view for a GET" in {
      val testConfig = Configuration("features.requestRefundEnabled" -> true)
      val userAnswer = UserAnswers(userAnswersId)
      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .configure(testConfig)
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.repayments.routes.RequestRefundBeforeStartController.onPageLoad.url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[RequestRefundBeforeStartView]

        status(result) mustEqual OK
        contentAsString(result) must include("Request a refund")
        contentAsString(result) mustEqual view()(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to Under Construction page if requestRefundEnabled is disabled" in {
      val ua = emptyUserAnswers
      val application = applicationBuilder(userAnswers = Some(ua))
        .configure(
          Seq(
            "features.requestRefundEnabled" -> false
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.repayments.routes.RequestRefundBeforeStartController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.UnderConstructionController.onPageLoad.url)
      }
    }
  }
}
