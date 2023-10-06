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

package controllers.registration

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.UpeNameRegistrationFormProvider
import models.NormalMode
import models.grs.EntityType
import models.registration.{GrsResponse, PartnershipEntityRegistrationData, Registration}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.RegistrationPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.RowStatus
import views.html.registrationview.UpeNameRegistrationView

import scala.concurrent.Future

class UpeNameRegistrationControllerSpec extends SpecBase {

  val formProvider = new UpeNameRegistrationFormProvider()

  "UpeNameRegistration Controller" must {

    "must return OK and the correct view for a GET" in {

      val userAnswersWithoutNameReg =
        emptyUserAnswers.set(RegistrationPage, validWithoutIdRegData).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutNameReg)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.UpeNameRegistrationController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpeNameRegistrationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }

    }

    "must redirect to the next page when valid data is submitted" in {

      val userAnswer = emptyUserAnswers.set(RegistrationPage, validWithoutIdRegDataWithName).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswer))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, routes.UpeNameRegistrationController.onSubmit(NormalMode).url).withFormUrlEncodedBody(("value", "Test Name"))
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.registration.routes.UpeRegisteredAddressController.onPageLoad(NormalMode).url

      }
    }

    "journey recovery for GET" should {

      "redirected to journey recovery if they are uk based" in {
        val ukBased = Registration(isUPERegisteredInUK = true, isRegistrationStatus = RowStatus.InProgress)

        val userAnswers = emptyUserAnswers.set(RegistrationPage, ukBased).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
        running(application) {
          val request = FakeRequest(GET, routes.UpeNameRegistrationController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        }
      }
      "redirected to journey recovery if they have chosen an entity type" in {
        val withEntityType = Registration(
          isUPERegisteredInUK = true,
          isRegistrationStatus = RowStatus.InProgress,
          orgType = Some(EntityType.LimitedLiabilityPartnership)
        )

        val userAnswers = emptyUserAnswers.set(RegistrationPage, withEntityType).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
        running(application) {
          val request = FakeRequest(GET, routes.UpeNameRegistrationController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        }
      }

      "redirected to journey recovery if they have any GRS response is saved in the database" in {
        val withGRSData = Registration(
          isUPERegisteredInUK = true,
          isRegistrationStatus = RowStatus.InProgress,
          withIdRegData = Some(
            new GrsResponse(
              partnershipEntityRegistrationData = Some(Json.parse(validRegistrationWithIdResponseForLLP()).as[PartnershipEntityRegistrationData])
            )
          )
        )

        val userAnswers = emptyUserAnswers.set(RegistrationPage, withGRSData).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
        running(application) {
          val request = FakeRequest(GET, routes.UpeNameRegistrationController.onPageLoad(NormalMode).url)
          val result  = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        }
      }

    }

  }
}
