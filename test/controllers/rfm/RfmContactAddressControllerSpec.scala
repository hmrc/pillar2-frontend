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
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{RfmContactAddressPage, RfmPrimaryContactNamePage}
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class RfmContactAddressControllerSpec extends SpecBase {
  val formProvider = new RfmContactAddressFormProvider()

  val defaultUa:   UserAnswers = emptyUserAnswers.set(RfmPrimaryContactNamePage, "Name").success.value
  def application: Application = applicationBuilder(userAnswers = Some(defaultUa)).build()
  def applicationOverride: Application = {
    when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

    applicationBuilder(userAnswers = Some(defaultUa))
      .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
      .build()
  }

  def getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, controllers.rfm.routes.RfmContactAddressController.onPageLoad(NormalMode).url)
  def postRequest(alterations: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = {
    val address = Map(
      "addressLine1" -> "27 House",
      "addressLine2" -> "Street",
      "addressLine3" -> "Newcastle",
      "addressLine4" -> "North east",
      "postalCode"   -> "NE5 2TR",
      "countryCode"  -> "GB"
    ) ++ alterations

    FakeRequest(POST, routes.RfmContactAddressController.onSubmit(NormalMode).url)
      .withFormUrlEncodedBody(address.toSeq: _*)
  }

  val textOver35Chars = "ThisAddressIsOverThirtyFiveCharacters"

  "RfmContactAddress Controller" should {

    "return OK and the correct view for a GET with no previous answer" in {

      running(application) {
        val result = route(application, getRequest).value

        status(result) mustEqual OK
        contentAsString(result) must include("What address do you want to use as the filing member’s contact address?")
        contentAsString(result) must include("Address line 1")
        contentAsString(result) must include("Town or city")
        contentAsString(result) must include("Region (optional)")
        contentAsString(result) must include("Postcode (if applicable)")
        contentAsString(result) must include("Enter text and then choose from the list.")
      }
    }

    "return OK and populate the view correctly when the question has been previously answered" in {
      val uaWithAddress = defaultUa.set(RfmContactAddressPage, nonUkAddress).success.value
      val application   = applicationBuilder(userAnswers = Some(uaWithAddress)).build()

      running(application) {
        val result = route(application, getRequest).value
        status(result) mustEqual OK

        contentAsString(result) must include("1 drive")
        contentAsString(result) must include("la la land")
      }
    }

    "display next page and status should be ok if valid data is used when country code is GB" in {
      running(applicationOverride) {
        val result = route(applicationOverride, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url

      }
    }

    "return form with errors when postcode is invalid" should {

      "UK address" when {
        "empty postcode" in {
          val result = route(application, postRequest("postalCode" -> "")).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) must include("Enter a full UK postcode")
        }

        "invalid format/length" in {
          val postcodeExceeding35Chars = route(application, postRequest("postalCode" -> textOver35Chars)).value
          val invalidUkPostcode        = route(application, postRequest("postalCode" -> "W111 1RC")).value

          status(postcodeExceeding35Chars) mustEqual BAD_REQUEST
          status(invalidUkPostcode) mustEqual BAD_REQUEST

          contentAsString(postcodeExceeding35Chars) must include("Enter a full UK postcode")
          contentAsString(invalidUkPostcode)        must include("Enter a full UK postcode")
        }
      }

      "non-UK address" when {
        "invalid length" in {
          val result = route(
            application,
            postRequest(
              "postalCode"  -> textOver35Chars,
              "countryCode" -> "PL"
            )
          ).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) must include("Postcode must be 10 characters or less")
        }
      }
    }

    "display error page and status should be Bad request if address line1 is mora than 35 characters" in {
      running(application) {
        val result = route(application, postRequest("addressLine1" -> textOver35Chars, "addressLine2" -> textOver35Chars)).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("The first line of the address must be 35 characters or less")
        contentAsString(result) must include("The second line of the address must be 35 characters or less")
      }
    }
  }
}
