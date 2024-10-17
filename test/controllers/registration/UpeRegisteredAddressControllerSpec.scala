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

package controllers.registration

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.UpeRegisteredAddressFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{UpeNameRegistrationPage, UpeRegisteredAddressPage, UpeRegisteredInUKPage}
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class UpeRegisteredAddressControllerSpec extends SpecBase {
  val formProvider = new UpeRegisteredAddressFormProvider()

  val defaultUa: UserAnswers = emptyUserAnswers
    .set(UpeRegisteredInUKPage, true)
    .success
    .value
    .set(UpeNameRegistrationPage, "Name")
    .success
    .value

  val textOver35Chars = "ThisAddressIsOverThirtyFiveCharacters"

  def application: Application = applicationBuilder(Some(defaultUa)).build()
  def applicationOverride: Application = applicationBuilder(Some(defaultUa))
    .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
    .build()

  def getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, controllers.registration.routes.UpeRegisteredAddressController.onPageLoad(NormalMode).url)
  def postRequest(alterations: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = {
    val address = Map(
      "addressLine1" -> "27 House",
      "addressLine2" -> "Street",
      "addressLine3" -> "Newcastle",
      "addressLine4" -> "North east",
      "postalCode"   -> "NE5 2TR",
      "countryCode"  -> "GB"
    ) ++ alterations

    FakeRequest(POST, controllers.registration.routes.UpeRegisteredAddressController.onSubmit(NormalMode).url)
      .withFormUrlEncodedBody(address.toSeq: _*)
  }

  when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

  "UpeRegisteredAddressController" when {

    ".onPageLoad" should {

      "return OK and the correct view for a GET with no previous answer" in {

        running(application) {
          val result = route(application, getRequest).value
          status(result) mustEqual OK

          contentAsString(result) must include("What is the registered office address of")
          contentAsString(result) must include("Address line 1")
          contentAsString(result) must include("Address line 2 (optional)")
          contentAsString(result) must include("Town or city")
          contentAsString(result) must include("Region (optional)")
          contentAsString(result) must include("Enter text and then choose from the list.")
        }
      }

      "return OK and the correct view for a GET with previous answers with" when {

        "a UK address" in {

          val ua = defaultUa.set(UpeRegisteredAddressPage, ukAddress).success.value
          val application: Application = applicationBuilder(Some(ua)).build()

          running(application) {
            val result = route(application, getRequest).value
            status(result) mustEqual OK

            contentAsString(result) must include("1 drive")
            contentAsString(result) must include("la la land")
            contentAsString(result) must include("m19hgs")
            contentAsString(result) must include("""<option value="GB" selected>United Kingdom</option>""")
          }
        }

        "a non-UK address" in {

          val ua = defaultUa.set(UpeRegisteredAddressPage, postcodedNonUkAddress).success.value
          val application: Application = applicationBuilder(Some(ua)).build()

          running(application) {
            val result = route(application, getRequest).value
            status(result) mustEqual OK

            contentAsString(result) must include("132 My Street")
            contentAsString(result) must include("Kingston")
            contentAsString(result) must include("12401")
            contentAsString(result) must include("""<option value="US" selected>United States of America</option>""")
          }
        }
      }

      "include/not include UK in country list based on user answer in UpeRegisteredInUKPage" should {

        "include UK if UpeRegisteredInUKPage is true" in {

          running(application) {
            val result = route(application, getRequest).value
            status(result) mustEqual OK

            contentAsString(result) must include("""<option value="GB">United Kingdom</option>""")
          }
        }

        "not include UK if UpeRegisteredInUKPage is false" in {

          val ua = emptyUserAnswers
            .set(UpeRegisteredInUKPage, false)
            .success
            .value
            .set(UpeNameRegistrationPage, "Name")
            .success
            .value

          val customApplication = applicationBuilder(userAnswers = Some(ua)).build()

          running(customApplication) {
            val result = route(customApplication, getRequest).value
            status(result) mustEqual OK

            contentAsString(result) mustNot include("""<option value="GB">United Kingdom</option>""")
          }
        }
      }
    }

    ".onSubmit" should {

      "in a form with errors, include/not include UK in country list based on previous answers" should {

        "include UK if UpeRegisteredInUKPage is true" in {

          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

          running(application) {
            val result = route(application, postRequest("postalCode" -> textOver35Chars)).value

            contentAsString(result) must include("""<option value="GB" selected>United Kingdom</option>""")
          }
        }

        "not include UK if UpeRegisteredInUKPage is false" in {

          val ua = emptyUserAnswers
            .set(UpeRegisteredInUKPage, false)
            .success
            .value
            .set(UpeNameRegistrationPage, "Name")
            .success
            .value

          val customApplication = applicationBuilder(Some(ua))
            .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
            .build()

          running(customApplication) {
            val result = route(customApplication, postRequest("postalCode" -> textOver35Chars)).value

            contentAsString(result) mustNot include("""<option value="GB">United Kingdom</option>""")
          }
        }
      }

      "redirect to next page and status should be OK if valid data is used when country code is GB" in {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        running(applicationOverride) {
          val result = route(applicationOverride, postRequest()).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.registration.routes.UpeContactNameController.onPageLoad(NormalMode).url
        }
      }

      "return errors if invalid data is submitted" when {

        "a UK address is submitted" when {

          "empty form" in {
            running(application) {
              val result = route(
                application,
                postRequest(
                  "addressLine1" -> "",
                  "addressLine2" -> "",
                  "addressLine3" -> "",
                  "addressLine4" -> "",
                  "postalCode"   -> "",
                  "countryCode"  -> "GB"
                )
              ).value

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) must include("Enter the first line of the address")
              contentAsString(result) must include("Enter the town or city")
              contentAsString(result) must include("Enter a full UK postcode")
            }
          }

          "invalid length" in {
            running(application) {
              val result = route(
                application,
                postRequest(
                  "addressLine1" -> textOver35Chars,
                  "addressLine2" -> textOver35Chars,
                  "addressLine3" -> textOver35Chars,
                  "addressLine4" -> textOver35Chars,
                  "postalCode"   -> textOver35Chars,
                  "countryCode"  -> "GB"
                )
              ).value

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) must include("First line of the address must be 35 characters or less")
              contentAsString(result) must include("Second line of the address must be 35 characters or less")
              contentAsString(result) must include("Town or city must be 35 characters or less")
              contentAsString(result) must include("Region must be 35 characters or less")
              contentAsString(result) must include("Enter a full UK postcode")
            }
          }
        }

        "a non-UK address is submitted" when {
          "empty form" in {

            running(application) {
              val result = route(
                application,
                postRequest(
                  "addressLine1" -> "",
                  "addressLine2" -> "",
                  "addressLine3" -> "",
                  "addressLine4" -> "",
                  "postalCode"   -> "",
                  "countryCode"  -> "PL"
                )
              ).value

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) must include("Enter the first line of the address")
              contentAsString(result) must include("Enter the town or city")
              contentAsString(result) must include("Enter the postcode")
            }
          }

          "invalid length" in {
            running(application) {
              val result = route(
                application,
                postRequest(
                  "addressLine1" -> textOver35Chars,
                  "addressLine2" -> textOver35Chars,
                  "addressLine3" -> textOver35Chars,
                  "addressLine4" -> textOver35Chars,
                  "postalCode"   -> textOver35Chars,
                  "countryCode"  -> "PL"
                )
              ).value

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) must include("First line of the address must be 35 characters or less")
              contentAsString(result) must include("Second line of the address must be 35 characters or less")
              contentAsString(result) must include("Town or city must be 35 characters or less")
              contentAsString(result) must include("Region must be 35 characters or less")
              contentAsString(result) must include("Postcode must be 10 characters or less")
            }
          }
        }
      }
    }
  }
}
