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

package controllers.fm

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.NominateFilingMemberYesNoFormProvider
import models.{NormalMode, UKAddress, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.fmview.NominateFilingMemberYesNoView

import scala.concurrent.Future

class NominateFilingMemberYesNoControllerSpec extends SpecBase {

  val formProvider = new NominateFilingMemberYesNoFormProvider()

  val UkAddress: UKAddress = UKAddress("this", None, "over", None, "m123hs", countryCode = "AR")
  val completeUpeJourney: UserAnswers = emptyUserAnswers
    .setOrException(UpeRegisteredInUKPage, false)
    .setOrException(UpeNameRegistrationPage, "name")
    .setOrException(UpeRegisteredAddressPage, UkAddress)
    .setOrException(UpeContactNamePage, "another name")
    .setOrException(UpeContactEmailPage, "email")
    .setOrException(UpePhonePreferencePage, false)

  "Nominate filing member Controller" must {

    "must return OK and the correct view for a GET if page previously not answered" in {
      val application = applicationBuilder(userAnswers = Some(completeUpeJourney)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NominateFilingMemberYesNoController.onPageLoad(NormalMode).url)
        val view    = application.injector.instanceOf[NominateFilingMemberYesNoView]
        val result  = route(application, request).value

        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
        status(result) mustBe OK
      }
    }

    "must return OK and the correct view for a GET if page previously answered" in {
      val application = applicationBuilder(userAnswers = Some(completeUpeJourney.setOrException(NominateFilingMemberPage, true))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NominateFilingMemberYesNoController.onPageLoad(NormalMode).url)
        val view    = application.injector.instanceOf[NominateFilingMemberYesNoView]
        val result  = route(application, request).value

        contentAsString(result) mustEqual view(formProvider().fill(true), NormalMode)(request, appConfig(application), messages(application)).toString
        status(result) mustBe OK
      }
    }

    "redirect to journey recovery if upe details not provided" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NominateFilingMemberYesNoController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        status(result) mustBe SEE_OTHER
      }
    }

    "Bad request if no option  is selected" in {
      val application = applicationBuilder().build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.fm.routes.NominateFilingMemberYesNoController.onSubmit(NormalMode).url).withFormUrlEncodedBody(
            "value" -> "$$"
          )
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val result    = route(application, request).value
        val boundForm = formProvider().bind(Map("value" -> "$$"))
        val view      = application.injector.instanceOf[NominateFilingMemberYesNoView]
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString()
      }
    }

    "must redirect to next page when valid data is submitted" in {
      val userAnswers = emptyUserAnswers.setOrException(NominateFilingMemberPage, true)
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.fm.routes.NominateFilingMemberYesNoController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("nominateFilingMember", "true"))
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.fm.routes.IsNfmUKBasedController.onPageLoad(NormalMode).url
      }
    }

  }
}
