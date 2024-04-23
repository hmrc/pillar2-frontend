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
import forms.RfmContactAddressFormProvider
import models.{NonUKAddress, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{RfmContactAddressPage, RfmPrimaryContactNamePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.InputOption

import scala.concurrent.Future

class RfmContactAddressControllerSpec extends SpecBase {
  val formProvider = new RfmContactAddressFormProvider()
  val countryList  = List(InputOption("AD", "Andorra", None))
  "RfmContactAddress Controller" when {

    "return OK and the correct view for a GET with no previous answer" in {
      val ua = emptyUserAnswers.set(RfmPrimaryContactNamePage, "Name").success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactAddressController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "What address do you want to use as the filing member&#x27;s contact address?"
        )
        contentAsString(result) must include(
          "Address line 1"
        )
        contentAsString(result) must include(
          "Town or city"
        )
        contentAsString(result) must include(
          "Region (optional)"
        )
        contentAsString(result) must include(
          "Postal code (if applicable)"
        )
        contentAsString(result) must include(
          "Enter text and then choose from the list."
        )
      }
    }

    "must return OK and populate the view correctly when the question has been previously answered" in {
      val contactAddress = NonUKAddress("Address line first drive", Some("Address line 2"), "Home Town", Some("region"), Some("ne5 2dh"), "AT")
      val ua = emptyUserAnswers
        .set(RfmPrimaryContactNamePage, "name")
        .success
        .value
        .set(RfmContactAddressPage, contactAddress)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactAddressController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) must include(
          "Address line first drive"
        )
        contentAsString(result) must include(
          "Home Town"
        )

      }
    }

    "must redirect to correct view when rfm feature false" in {
      val ua = emptyUserAnswers
        .setOrException(RfmPrimaryContactNamePage, "sad")
      val application = applicationBuilder(userAnswers = Some(ua))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> false
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmContactAddressController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }
    }

    "display next  page and status should be ok if valid data is used  when country code is GB" in {
      val userAnswersWitNameReg = emptyUserAnswers.set(RfmPrimaryContactNamePage, "Alex").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswersWitNameReg))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, routes.RfmContactAddressController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("addressLine1", "27 house"),
              ("addressLine2", "Drive"),
              ("addressLine3", "Newcastle"),
              ("addressLine4", "North east"),
              ("postalCode", "Ne5 2TR"),
              ("countryCode", "GB")
            )

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url

      }
    }

    "display error page and status should be Bad request if invalid post code is used  when country code is GB" in {
      val userAnswersWitNameReg = emptyUserAnswers.set(RfmPrimaryContactNamePage, "Alex").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswersWitNameReg))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, routes.RfmContactAddressController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("addressLine1", "27 house"),
              ("addressLine2", "Drive"),
              ("addressLine3", "Newcastle"),
              ("addressLine4", "North east"),
              ("postalCode", "hhhhhhhhhhhh"),
              ("countryCode", "GB")
            )

        val result = route(application, request).value
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include(
          "Enter a full UK postal code"
        )

      }
    }

    "display error page and status should be Bad request if address line1 is mora than 35 characters" in {
      val userAnswersWitNameReg = emptyUserAnswers.set(RfmPrimaryContactNamePage, "Alex").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswersWitNameReg))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val badHouse = "27 house" * 120
        val request =
          FakeRequest(POST, routes.RfmContactAddressController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              (
                "addressLine1",
                badHouse
              ),
              ("addressLine2", badHouse),
              ("addressLine3", "Newcastle"),
              ("addressLine4", "North east"),
              ("postalCode", "ne5 2th"),
              ("countryCode", "GB")
            )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include(
          "First line of the address must be 35 characters or less"
        )
        contentAsString(result) must include(
          "Second line of the address must be 35 characters or less"
        )
      }
    }

  }
}
