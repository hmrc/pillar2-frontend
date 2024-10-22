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
import forms.NfmRegisteredAddressFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{FmNameRegistrationPage, FmRegisteredAddressPage, FmRegisteredInUKPage}
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class NfmRegisteredAddressControllerSpec extends SpecBase {
  val formProvider = new NfmRegisteredAddressFormProvider()
  val defaultUa: UserAnswers = emptyUserAnswers
    .set(FmRegisteredInUKPage, true)
    .success
    .value
    .set(FmNameRegistrationPage, "Name")
    .success
    .value

  val textOver35Chars = "ThisAddressIsOverThirtyFiveCharacters"

  def application: Application = applicationBuilder(Some(defaultUa)).build()
  def applicationOverride: Application = applicationBuilder(Some(defaultUa))
    .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
    .build()

  def getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, controllers.fm.routes.NfmRegisteredAddressController.onPageLoad(NormalMode).url)
  def postRequest(alterations: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = {
    val address = Map(
      "addressLine1" -> "27 House",
      "addressLine2" -> "Street",
      "addressLine3" -> "Newcastle",
      "addressLine4" -> "North east",
      "postalCode"   -> "NE5 2TR",
      "countryCode"  -> "GB"
    ) ++ alterations

    FakeRequest(POST, controllers.fm.routes.NfmRegisteredAddressController.onSubmit(NormalMode).url)
      .withFormUrlEncodedBody(address.toSeq: _*)
  }

  "NfmRegisteredAddressController" when {

    ".onPageLoad" should {

      "return OK and the correct view for a GET if Nfm access is enabled and no previous data is found" in {

        running(application) {
          val result = route(application, getRequest).value

          status(result) mustEqual OK
          contentAsString(result) must include("Name")
        }
      }

      "return OK and the correct view for a GET if Nfm access is enabled and page has previously been answered" in {
        val ua = defaultUa
          .set(FmRegisteredAddressPage, nonUkAddress)
          .success
          .value

        val customApplication = applicationBuilder(Some(ua)).build()

        running(customApplication) {
          val result = route(customApplication, getRequest).value
          status(result) mustEqual OK
          contentAsString(result) must include("la la land")
        }
      }

      "include/not include UK in country list based on user answer in NfmUkBasedPage" should {

        "include UK if NfmUkBasedPage is true" in {

          running(application) {
            val result = route(application, getRequest).value
            status(result) mustEqual OK

            contentAsString(result) must include("""<option value="GB">United Kingdom</option>""")
          }
        }

        "not include UK if NfmUkBasedPage is false" in {

          val ua = emptyUserAnswers
            .set(FmRegisteredInUKPage, false)
            .success
            .value
            .set(FmNameRegistrationPage, "Name")
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
      "redirect to next page with valid data onSubmit" in {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        running(applicationOverride) {
          val result = route(applicationOverride, postRequest()).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.fm.routes.NfmContactNameController.onPageLoad(NormalMode).url
        }
      }

      "in a form with errors, include/not include UK in country list based on previous answers" should {
        "include UK if NfmUkBasedPage is true" in {

          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

          running(application) {
            val result = route(application, postRequest("postalCode" -> textOver35Chars)).value

            contentAsString(result) must include("""<option value="GB" selected>United Kingdom</option>""")
          }
        }

        "not include UK if NfmUkBasedPage is false" in {

          val ua = emptyUserAnswers
            .set(FmRegisteredInUKPage, false)
            .success
            .value
            .set(FmNameRegistrationPage, "Name")
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
}
