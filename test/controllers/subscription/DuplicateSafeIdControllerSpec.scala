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
import forms.DuplicateSafeIdFormProvider
import models.{NormalMode, UKAddress, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.DuplicateSafeIdView

import scala.concurrent.Future

class DuplicateSafeIdControllerSpec extends SpecBase {

  val formProvider = new DuplicateSafeIdFormProvider()

  val UkAddress: UKAddress = UKAddress("line1", None, "line3", None, "M123BS", countryCode = "US")
  val completeUpeJourney: UserAnswers = emptyUserAnswers
    .setOrException(UpeRegisteredInUKPage, false)
    .setOrException(UpeNameRegistrationPage, "name")
    .setOrException(UpeRegisteredAddressPage, UkAddress)
    .setOrException(UpeContactNamePage, "contact name")
    .setOrException(UpeContactEmailPage, "email@email.com")
    .setOrException(UpePhonePreferencePage, false)

  "Duplicate SafeId Controller" should {
    "return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(completeUpeJourney)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.DuplicateSafeIdController.onPageLoad.url)
        val view    = application.injector.instanceOf[DuplicateSafeIdView]
        val result  = route(application, request).value

        contentAsString(result) mustEqual view(formProvider())(request, applicationConfig, messages(application)).toString
        status(result) mustBe OK
      }
    }

    "redirect to journey recovery if upe details not provided" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.DuplicateSafeIdController.onPageLoad.url)
        val result  = route(application, request).value

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        status(result) mustBe SEE_OTHER
      }
    }

    "Bad request if no option is selected" in {
      val application = applicationBuilder().build()
      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.DuplicateSafeIdController.onSubmit.url).withFormUrlEncodedBody(
            "nominateFilingMember" -> "$$"
          )
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val result    = route(application, request).value
        val boundForm = formProvider().bind(Map("nominateFilingMember" -> "$$"))
        val view      = application.injector.instanceOf[DuplicateSafeIdView]
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, applicationConfig, messages(application)).toString()
      }
    }

    "must redirect to 'Is the NFM registered in UK' page when yes is submitted" in {
      val userAnswers = emptyUserAnswers.setOrException(NominateFilingMemberPage, true)
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.DuplicateSafeIdController.onSubmit.url)
            .withFormUrlEncodedBody(("nominateFilingMember", "true"))
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.fm.routes.IsNfmUKBasedController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to final CYA page when no is submitted" in {
      val userAnswers = emptyUserAnswers.setOrException(NominateFilingMemberPage, false)
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.DuplicateSafeIdController.onSubmit.url)
            .withFormUrlEncodedBody(("nominateFilingMember", "false"))
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.CheckYourAnswersController.onPageLoad.url
      }
    }

  }
}
