/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.subscription

import base.SpecBase
import forms.FmContactAddressFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.SubscriptionPage
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class FmContactAddressControllerSpec extends SpecBase {

  val formProvider = new FmContactAddressFormProvider()

  "FmContactAddress Controller" when {

    "must return OK and the correct view for a GET" in {
      val userAnswersSubCaptureNoPhone = emptyUserAnswers.set(SubscriptionPage, validSubData()).success.value
      val application                  = applicationBuilder(userAnswers = Some(userAnswersSubCaptureNoPhone)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.FmContactAddressController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual OK
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val userAnswersSubCaptureNoPhone = emptyUserAnswers.set(SubscriptionPage, validSubData()).success.value
      val application                  = applicationBuilder(userAnswers = Some(userAnswersSubCaptureNoPhone)).build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, routes.FmContactAddressController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("addressLine1", "27 house"),
              ("addressLine2", "Drive"),
              ("addressLine3", "Newcastle"),
              ("addressLine4", "North east"),
              ("postalCode", "NE3 2TR"),
              ("countryCode", "GB")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }
    }
  }
}
