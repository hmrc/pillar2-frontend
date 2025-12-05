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
import connectors.UserAnswersConnectors
import forms.NFMRegisteredInUKConfirmationFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.RfmUkBasedPage
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.rfm.UkBasedFilingMemberView

import scala.concurrent.Future

class UkBasedFilingMemberControllerSpec extends SpecBase {

  val formProvider = new NFMRegisteredInUKConfirmationFormProvider()

  "RFM UK Based Filing Member controller" when {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.UkBasedFilingMemberController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[UkBasedFilingMemberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "must populate the view correctly when the question has been previously answered" in {
      val userAnswers = emptyUserAnswers.setOrException(RfmUkBasedPage, true)
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.UkBasedFilingMemberController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[UkBasedFilingMemberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(true), NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(using any())).thenReturn(Future.successful(emptyUserAnswers.data))
        val request = FakeRequest(POST, controllers.rfm.routes.UkBasedFilingMemberController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", "true"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must return Bad Request and show specific error message when no option is selected" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, controllers.rfm.routes.UkBasedFilingMemberController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", ""))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Select yes if the new nominated filing member is registered in the UK")
      }
    }
  }
}
