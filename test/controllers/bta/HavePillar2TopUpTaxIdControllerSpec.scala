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
import connectors.UserAnswersConnectors
import forms.{ContactUPEByTelephoneFormProvider, HavePillar2TopUpTaxIdFormProvider}
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{subAddSecondaryContactPage, subHavePillar2TopUpTaxIdPage, subPrimaryContactNamePage, subPrimaryEmailPage, subSecondaryContactNamePage, upeContactEmailPage, upeContactNamePage, upePhonePreferencePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.bta.HavePillar2TopUpTaxIdView
import views.html.registrationview.ContactUPEByTelephoneView
import views.html.subscriptionview.{AddSecondaryContactView, SecondaryTelephonePreferenceView}

import scala.concurrent.Future

class HavePillar2TopUpTaxIdControllerontrollerSpec extends SpecBase {

  val form         = new HavePillar2TopUpTaxIdFormProvider()
  val formProvider = form()

  "Have Pillar two TopUp Tax Id  Controller" when {

    "return OK and the correct view for a GET" in {
      val ua = emptyUserAnswers
      val application = applicationBuilder(userAnswers = Some(ua))
        .configure(
          Seq(
            "features.btaAccessEnabled" -> true
          ): _*
        )
        .build()
      running(application) {
        val request = FakeRequest(GET, controllers.bta.routes.HavePillar2TopUpTaxIdController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HavePillar2TopUpTaxIdView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider, NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString

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
        val request = FakeRequest(GET, controllers.bta.routes.HavePillar2TopUpTaxIdController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }
    }

    "redirect to the user to EACD Frontend management page to confirm identifier/verifier page when valid data is submitted with value YES" in {

      val ua = emptyUserAnswers
        .set(subHavePillar2TopUpTaxIdPage, true)
        .success
        .value
      val application = applicationBuilder(Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .configure(
          Seq(
            "features.btaAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.bta.routes.HavePillar2TopUpTaxIdController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual appConfig.eacdHomePageUrl
      }
    }

    " redirect to en-eligible page when NO is selected " in {
      val ua = emptyUserAnswers.set(subHavePillar2TopUpTaxIdPage, false).success.value
      val application = applicationBuilder(Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .configure(
          Seq(
            "features.btaAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.bta.routes.HavePillar2TopUpTaxIdController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "false"))
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.bta.routes.NoPlrIdGuidanceController.onPageLoad.url
      }
    }
    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .setOrException(subHavePillar2TopUpTaxIdPage, true)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .configure(
          Seq(
            "features.btaAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.bta.routes.HavePillar2TopUpTaxIdController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual OK

      }
    }
    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder()
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .configure(
          Seq(
            "features.btaAccessEnabled" -> true
          ): _*
        )
        .build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.bta.routes.HavePillar2TopUpTaxIdController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))
        val boundForm = formProvider.bind(Map("value" -> ""))
        val view      = application.injector.instanceOf[HavePillar2TopUpTaxIdView]
        val result    = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

  }
}
