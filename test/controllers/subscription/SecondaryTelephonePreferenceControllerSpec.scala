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
import forms.SecondaryTelephonePreferenceFormProvider
import models.subscription.Subscription
import models.{MneOrDomestic, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.SubscriptionPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.RowStatus
import views.html.subscriptionview.SecondaryTelephonePreferenceView

import scala.concurrent.Future

class SecondaryTelephonePreferenceControllerSpec extends SpecBase {

  val form         = new SecondaryTelephonePreferenceFormProvider()
  val formProvider = form("true")
  "SecondaryTelephonePreference Controller" when {

    "must return OK and the correct view for a GET" in {

      val subscription =
        Subscription(
          domesticOrMne = MneOrDomestic.Uk,
          groupDetailStatus = RowStatus.Completed,
          contactDetailsStatus = RowStatus.InProgress
        )
      val userAnswers = UserAnswers(userAnswersId).set(SubscriptionPage, subscription).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.SecondaryTelephonePreferenceController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryTelephonePreferenceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val subscription =
        Subscription(
          domesticOrMne = MneOrDomestic.Uk,
          groupDetailStatus = RowStatus.Completed,
          contactDetailsStatus = RowStatus.InProgress,
          secondaryTelephonePreference = Some(true)
        )
      val userAnswers = UserAnswers(userAnswersId).set(SubscriptionPage, subscription).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.SecondaryTelephonePreferenceController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryTelephonePreferenceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider.fill(true), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val badAnswer   = "asdqwd" * 100
      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.SecondaryTelephonePreferenceController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", badAnswer))

        val boundForm = formProvider.bind(Map("value" -> badAnswer))

        val view = application.injector.instanceOf[SecondaryTelephonePreferenceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }
    //this
    "must redirect to telephone contact page if they answer yes " in {
      val userAnswers = UserAnswers(userAnswersId)
        .set(
          SubscriptionPage,
          Subscription(
            MneOrDomestic.UkAndOther,
            contactDetailsStatus = RowStatus.InProgress,
            groupDetailStatus = RowStatus.Completed,
            secondaryContactName = Some("name")
          )
        )
        .success
        .value

      val application = applicationBuilder(Some(userAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      val request = FakeRequest(POST, controllers.subscription.routes.SecondaryTelephonePreferenceController.onSubmit(NormalMode).url)
        .withFormUrlEncodedBody("value" -> "true")

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.SecondaryTelephoneController.onPageLoad(NormalMode).url

      }
    }
    "must redirect to Not Found page for a GET if no previous existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request     = FakeRequest(GET, controllers.subscription.routes.SecondaryTelephonePreferenceController.onPageLoad(NormalMode).url)

      running(application) {
        val result =
          route(application, request).value

        status(result) mustEqual NOT_FOUND
      }
    }

    "must redirect to Journey Recovery for a POST if no previous existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request = FakeRequest(POST, controllers.subscription.routes.SecondaryTelephonePreferenceController.onSubmit(NormalMode).url)
        .withFormUrlEncodedBody("value" -> "true")

      running(application) {
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
