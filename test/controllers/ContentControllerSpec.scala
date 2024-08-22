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

package controllers.contactdetails
import base.SpecBase
import connectors.UserAnswersConnectors
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.SubPrimaryContactNamePage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.ContentView

import scala.concurrent.Future
class ContentControllerSpec extends SpecBase {

  "StartPageRegistrationController" when {

    "must return OK and the correct view for a GET " in {
      val application = applicationBuilder().build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContentController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[ContentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to next page accounting period when valid data is submitted" in {
      val ua = emptyUserAnswers
      val application = applicationBuilder(userAnswers = Some(ua))
        .build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.ContentController.onSubmit(NormalMode).url)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.registration.routes.UPERegisteredInUKConfirmationController.onPageLoad(NormalMode).url
      }
    }
  }

}
