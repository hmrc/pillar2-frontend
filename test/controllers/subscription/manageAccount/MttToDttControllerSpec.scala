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

package controllers.subscription.manageAccount

import base.SpecBase
import connectors.UserAnswersConnectors
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.manageAccount.MttToDttView

class MttToDttControllerSpec extends SpecBase {

  "MttToDtt Controller" should {
    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(subscriptionLocalData = Some(someSubscriptionLocalData))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.manageAccount.routes.MttToDttController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MttToDttView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(isAgent = false)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
  }
}
