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

package controllers.subscription.manageAccount

import base.SpecBase
import connectors.SubscriptionConnector
import controllers.actions.TestAuthRetrievals.Ops
import forms.CaptureSubscriptionAddressFormProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.SubAddSecondaryContactPage
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.{AuthConnector, User}

import java.util.UUID
import scala.concurrent.Future

class CaptureSubscriptionAddressControllerSpec extends SpecBase {
  val formProvider = new CaptureSubscriptionAddressFormProvider()
  def randomUUID: String = UUID.randomUUID().toString

  def organisationApp: Application =
    applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData.setOrException(SubAddSecondaryContactPage, true)))
      .overrides(bind[SubscriptionConnector].toInstance(mockSubscriptionConnector))
      .build()

  def agentApp: Application = {
    when(mockAuthConnector.authorise[AgentRetrievalsType](any(), any())(any(), any()))
      .thenReturn(
        Future.successful(
          Some(randomUUID) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId = randomUUID, providerType = randomUUID))
        )
      )

    applicationBuilder(subscriptionLocalData = Some(emptySubscriptionLocalData))
      .overrides(bind[SubscriptionConnector].toInstance(mockSubscriptionConnector))
      .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
      .build()
  }

  def getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, controllers.subscription.manageAccount.routes.CaptureSubscriptionAddressController.onPageLoad().url)
  def postRequest(alterations: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = {
    when(mockSubscriptionConnector.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

    val address = Map(
      "addressLine1" -> "27 House",
      "addressLine2" -> "Street",
      "addressLine3" -> "Newcastle",
      "addressLine4" -> "North east",
      "postalCode"   -> "NE5 2TR",
      "countryCode"  -> "GB"
    ) ++ alterations

    FakeRequest(POST, controllers.subscription.manageAccount.routes.CaptureSubscriptionAddressController.onSubmit().url)
      .withFormUrlEncodedBody(address.toSeq*)
  }

  val textOver35Chars = "ThisAddressIsOverThirtyFiveCharacters"

  "CaptureSubscriptionAddressController for Organisations" should {

    "redirect to contact CYA when valid data is submitted" in {
      val result = route(organisationApp, postRequest()).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad().url
    }

    "must return OK and the correct view for a GET if page not previously answered" in {
      val result = route(organisationApp, getRequest).value

      status(result) mustEqual OK
    }

    "return form with errors when postcode is invalid" should {

      "UK address" when {
        "empty postcode" in {
          val result = route(organisationApp, postRequest("postalCode" -> "")).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) must include("Enter a full UK postcode")
        }

        "invalid format/length" in {
          val postcodeExceeding35Chars = route(organisationApp, postRequest("postalCode" -> textOver35Chars)).value
          val invalidUkPostcode        = route(organisationApp, postRequest("postalCode" -> "W111 1RC")).value

          status(postcodeExceeding35Chars) mustEqual BAD_REQUEST
          status(invalidUkPostcode) mustEqual BAD_REQUEST

          contentAsString(postcodeExceeding35Chars) must include("Enter a full UK postcode")
          contentAsString(invalidUkPostcode)        must include("Enter a full UK postcode")
        }
      }

      "non-UK address" when {
        "invalid length" in {
          val result = route(
            organisationApp,
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
      val result = route(organisationApp, postRequest("addressLine1" -> textOver35Chars)).value

      status(result) mustEqual BAD_REQUEST
    }
  }

  "CaptureSubscriptionAddressController for Agents" should {

    "must return OK and the correct view for a GET if page not previously answered" in {
      val result = route(agentApp, getRequest).value

      status(result) mustEqual OK
    }

    "redirect to contact CYA when valid data is submitted" in {
      val result = route(agentApp, postRequest()).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad().url
    }

    "return form with errors when postcode is invalid" should {

      "UK address" when {
        "empty postcode" in {
          val result = route(agentApp, postRequest("postalCode" -> "")).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) must include("Enter a full UK postcode")
        }

        "invalid format/length" in {
          val postcodeExceeding35Chars = route(organisationApp, postRequest("postalCode" -> textOver35Chars)).value
          val invalidUkPostcode        = route(organisationApp, postRequest("postalCode" -> "W111 1RC")).value

          status(postcodeExceeding35Chars) mustEqual BAD_REQUEST
          status(invalidUkPostcode) mustEqual BAD_REQUEST

          contentAsString(postcodeExceeding35Chars) must include("Enter a full UK postcode")
          contentAsString(invalidUkPostcode)        must include("Enter a full UK postcode")
        }
      }

      "non-UK address" when {
        "invalid format" in {
          val result = route(
            agentApp,
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
      val result = route(agentApp, postRequest("addressLine1" -> textOver35Chars)).value

      status(result) mustEqual BAD_REQUEST
    }
  }
}
