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

package controllers.subscription

import base.SpecBase
import connectors.UserAnswersConnectors
import forms.CaptureContactAddressFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.SubscriptionPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.CaptureContactAddressView

import scala.concurrent.Future

class CaptureContactAddressControllerSpec extends SpecBase {

  val formProvider = new CaptureContactAddressFormProvider()

  "CaptureContactAddress Controller" when {

    "must return OK for NFM and the correct view for a GET" in {

      val userAnswersWithNominatedFilingMemberWithSub =
        userAnswersNfmNoId.set(SubscriptionPage, validSubscriptionData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNominatedFilingMemberWithSub)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.CaptureContactAddressController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CaptureContactAddressView]

        status(result) mustEqual OK
        contentAsString(result) contains view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must return OK for NFM with ID For LimitedComp and the correct view for a GET" in {

      val userAnswersWithNominatedFilingMemberForLimitedCompWithSub =
        userAnswersWithIdForLimitedCompForFm.set(SubscriptionPage, validSubscriptionData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNominatedFilingMemberForLimitedCompWithSub)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.CaptureContactAddressController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CaptureContactAddressView]

        status(result) mustEqual OK
        contentAsString(result) contains view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must return OK for NFM with ID For LLP and the correct view for a GET" in {

      val userAnswersWithNominatedFilingMemberForLLPWithSub =
        userAnswersWithIdForLLPForFm.set(SubscriptionPage, validSubscriptionData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNominatedFilingMemberForLLPWithSub)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.CaptureContactAddressController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CaptureContactAddressView]

        status(result) mustEqual OK
        contentAsString(result) contains view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must return OK for UPE and the correct view for a GET" in {

      val userAnswersForUPEWithSub =
        userAnswersWithNoId.set(SubscriptionPage, validSubscriptionData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersForUPEWithSub)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.CaptureContactAddressController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CaptureContactAddressView]

        status(result) mustEqual OK
        contentAsString(result) contains view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswersWithNominatedFilingMemberWithSub =
        userAnswersNfmNoId.set(SubscriptionPage, validSubscriptionData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNominatedFilingMemberWithSub))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.CaptureContactAddressController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("addressLine1", "27 house"),
              ("addressLine2", "Drive"),
              ("addressLine3", "Newcastle"),
              ("town_city", "North east"),
              ("region", "North east"),
              ("postcode", "NE3 2TR"),
              ("country", "GB")
            )

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[CaptureContactAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must redirect to CheckYourAnswers page when Yes is selected" in {

      val userAnswersWithNominatedFilingMemberWithSub =
        userAnswersNfmNoId.set(SubscriptionPage, validSubscriptionData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNominatedFilingMemberWithSub))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.subscription.routes.CaptureContactAddressController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(
            ("value", "true"),
            ("addressLine1", "27 house"),
            ("addressLine2", "Drive"),
            ("addressLine3", "Newcastle"),
            ("town_city", "North east"),
            ("region", "North east"),
            ("postcode", "NE3 2TR"),
            ("country", "GB")
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }

    }

    "must redirect Subscription Address page when No is selected" in {

      val userAnswersWithNominatedFilingMemberWithSub =
        userAnswersNfmNoId.set(SubscriptionPage, validSubscriptionData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNominatedFilingMemberWithSub))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.subscription.routes.CaptureContactAddressController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(
            ("value", "false"),
            ("addressLine1", "27 house"),
            ("addressLine2", "Drive"),
            ("addressLine3", "Newcastle"),
            ("town_city", "North east"),
            ("region", "North east"),
            ("postcode", "NE3 2TR"),
            ("country", "GB")
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.SubscriptionAddressController.onSubmit(NormalMode).url
      }

    }

  }
}
