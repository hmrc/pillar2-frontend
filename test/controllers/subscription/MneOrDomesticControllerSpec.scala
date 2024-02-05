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

package controllers.subscription

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.MneOrDomesticFormProvider
import models.{MneOrDomestic, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{NominateFilingMemberPage, subMneOrDomesticPage}
import play.api.inject
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.MneOrDomesticView

import scala.concurrent.Future

class MneOrDomesticControllerSpec extends SpecBase {

  val formProvider = new MneOrDomesticFormProvider()

  "MneOrDomestic Controller" when {

    "must return OK and the correct view for a GET when previous data is found" in {
      val userAnswer = emptyUserAnswers
        .setOrException(subMneOrDomesticPage, MneOrDomestic.Uk)
        .setOrException(NominateFilingMemberPage, false)

      val application = applicationBuilder(userAnswers = Some(userAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.MneOrDomesticController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MneOrDomesticView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(MneOrDomestic.Uk), NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET when no previous data is found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.setOrException(NominateFilingMemberPage, false))).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.MneOrDomesticController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[MneOrDomesticView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "redirect to bookmark page if previous page not answered" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.MneOrDomesticController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.MneOrDomesticController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", "invalid value"))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must redirect to accounting period page when valid data is submitted" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(inject.bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request = FakeRequest(POST, controllers.subscription.routes.MneOrDomesticController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody("value" -> "ukAndOther")
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.GroupAccountingPeriodController.onPageLoad(NormalMode).url
      }
    }

  }
}
