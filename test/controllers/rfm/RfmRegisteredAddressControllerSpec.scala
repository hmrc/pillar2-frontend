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
import forms.RfmRegisteredAddressFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{RfmNameRegistrationPage, RfmRegisteredAddressPage, RfmUkBasedPage}
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.Future

class RfmRegisteredAddressControllerSpec extends SpecBase {
  val formProvider = new RfmRegisteredAddressFormProvider()
  val defaultUa: UserAnswers = emptyUserAnswers
    .set(RfmUkBasedPage, true)
    .success
    .value
    .set(RfmNameRegistrationPage, "Name")
    .success
    .value

  val textOver35Chars = "ThisAddressIsOverThirtyFiveCharacters"

  def application:         Application = applicationBuilder(Some(defaultUa)).build()
  def applicationOverride: Application = applicationBuilder(Some(defaultUa))
    .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
    .build()

  def getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, controllers.rfm.routes.RfmRegisteredAddressController.onPageLoad(NormalMode).url)

  def postRequest(alterations: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = {
    val address = Map(
      "addressLine1" -> "27 House",
      "addressLine2" -> "Street",
      "addressLine3" -> "Newcastle",
      "addressLine4" -> "North east",
      "postalCode"   -> "NE5 2TR",
      "countryCode"  -> "GB"
    ) ++ alterations

    FakeRequest(POST, controllers.rfm.routes.RfmRegisteredAddressController.onSubmit(NormalMode).url)
      .withFormUrlEncodedBody(address.toSeq*)
  }

  "RFMRegisteredAddressController" when {

    ".onPageLoad" should {

      "return OK and the correct view for a GET if RFM access is enabled and no previous data is found" in
        running(application) {
          val result = route(application, getRequest).value

          status(result) mustEqual OK
          contentAsString(result) must include("Name")
        }

      "return OK and the correct view for a GET if RFM access is enabled and page has previously been answered" in {
        val ua = defaultUa
          .set(RfmRegisteredAddressPage, nonUkAddress)
          .success
          .value

        val customApplication = applicationBuilder(Some(ua)).build()

        running(customApplication) {
          val result = route(customApplication, getRequest).value
          status(result) mustEqual OK
          contentAsString(result) must include("la la land")
        }
      }

      "redirect to JourneyRecoveryController if previous page not answered" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {
          val result = route(application, getRequest).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url)
        }
      }

      "include/not include UK in country list based on user answer in RfmUkBasedPage" should {

        "include UK if RfmUkBasedPage is true" in
          running(application) {
            val result = route(application, getRequest).value
            status(result) mustEqual OK

            contentAsString(result).replaceAll("\\s", "") must include("""<option value="GB">United Kingdom</option>""".replaceAll("\\s", ""))
          }

        "not include UK if RfmUkBasedPage is false" in {

          val ua = emptyUserAnswers
            .set(RfmUkBasedPage, false)
            .success
            .value
            .set(RfmNameRegistrationPage, "Name")
            .success
            .value

          val customApplication = applicationBuilder(userAnswers = Some(ua)).build()

          running(customApplication) {
            val result = route(customApplication, getRequest).value
            status(result) mustEqual OK

            contentAsString(result).replaceAll("\\s", "") mustNot include("""<option value="GB">United Kingdom</option>""".replaceAll("\\s", ""))
          }
        }
      }
    }

    ".onSubmit" should {
      "redirect to NoIdCheckYourAnswersController with valid data onSubmit" in {
        when(mockUserAnswersConnectors.save(any(), any())(using any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        running(applicationOverride) {
          val result = route(applicationOverride, postRequest()).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rfm.routes.RfmCheckYourAnswersController.onPageLoad(NormalMode).url
        }
      }

      "redirect to JourneyRecoveryController if previous page not answered OnSubmit" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.rfm.routes.RfmRegisteredAddressController.onSubmit(NormalMode).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url)
        }
      }

      "in a form with errors, include/not include UK in country list based on previous answers" should {
        "include UK if RfmUkBasedPage is true" in {

          when(mockUserAnswersConnectors.save(any(), any())(using any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

          running(application) {
            val result = route(application, postRequest("postalCode" -> textOver35Chars)).value

            contentAsString(result).replaceAll("\\s", "") must include(
              """<option value="GB" selected>United Kingdom</option>""".replaceAll("\\s", "")
            )
          }
        }

        "not include UK if RfmUkBasedPage is false" in {

          val ua = emptyUserAnswers
            .set(RfmUkBasedPage, false)
            .success
            .value
            .set(RfmNameRegistrationPage, "Name")
            .success
            .value

          val customApplication = applicationBuilder(Some(ua))
            .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
            .build()

          running(customApplication) {
            val result = route(customApplication, postRequest("postalCode" -> textOver35Chars)).value

            contentAsString(result).replaceAll("\\s", "") mustNot include("""<option value="GB">United Kingdom</option>""".replaceAll("\\s", ""))
          }
        }
      }

      "return errors if invalid data is submitted" when {
        "a UK address is submitted" when {

          "empty form" in
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

          "invalid length" in
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
              contentAsString(result) must include("The first line of the address must be 35 characters or less")
              contentAsString(result) must include("The second line of the address must be 35 characters or less")
              contentAsString(result) must include("The town or city must be 35 characters or less")
              contentAsString(result) must include("The region must be 35 characters or less")
              contentAsString(result) must include("Enter a full UK postcode")
            }

          "a non-UK address is submitted" when {
            "empty form" in
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

            "invalid length" in
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
                contentAsString(result) must include("The first line of the address must be 35 characters or less")
                contentAsString(result) must include("The second line of the address must be 35 characters or less")
                contentAsString(result) must include("The town or city must be 35 characters or less")
                contentAsString(result) must include("The region must be 35 characters or less")
                contentAsString(result) must include("Postcode must be 10 characters or less")
              }
          }

        }
      }
    }
  }
}
