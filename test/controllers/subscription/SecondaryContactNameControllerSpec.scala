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
import forms.SecondaryContactNameFormProvider
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
import views.html.subscriptionview.SecondaryContactNameView

import scala.concurrent.Future

class SecondaryContactNameControllerSpec extends SpecBase {

  val formProvider = new SecondaryContactNameFormProvider()

  "SecondaryContactName Controller" when {

    "must return OK and the correct view for a GET" in {
      val subscription =
        Subscription(
          domesticOrMne = MneOrDomestic.Uk,
          groupDetailStatus = RowStatus.Completed,
          contactDetailsStatus = RowStatus.InProgress,
          useContactPrimary = Some(false)
        )
      val userAnswers = UserAnswers(userAnswersId).set(SubscriptionPage, subscription).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.SecondaryContactNameController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SecondaryContactNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val subscription =
        Subscription(
          domesticOrMne = MneOrDomestic.Uk,
          groupDetailStatus = RowStatus.Completed,
          contactDetailsStatus = RowStatus.InProgress,
          secondaryContactName = Some("answer"),
          useContactPrimary = Some(false)
        )

      val userAnswers = UserAnswers(userAnswersId).set(SubscriptionPage, subscription).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.SecondaryContactNameController.onPageLoad(NormalMode).url)

        val view = application.injector.instanceOf[SecondaryContactNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill("answer"), NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.SecondaryContactNameController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[SecondaryContactNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must redirect to Not Found page for a GET if no previous existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request     = FakeRequest(GET, controllers.subscription.routes.SecondaryContactNameController.onPageLoad(NormalMode).url)

      running(application) {
        val result =
          route(application, request).value

        status(result) mustEqual NOT_FOUND
      }
    }

    "must redirect to Journey Recovery for a POST if no previous existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request = FakeRequest(POST, controllers.subscription.routes.SecondaryContactNameController.onSubmit(NormalMode).url)
        .withFormUrlEncodedBody("value" -> "name")

      running(application) {
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to secondary contact email when the user enters a valid answer " in {
      val userAnswers = UserAnswers(userAnswersId)
        .set(
          SubscriptionPage,
          Subscription(
            MneOrDomestic.Uk,
            contactDetailsStatus = RowStatus.InProgress,
            groupDetailStatus = RowStatus.Completed,
            secondaryContactName = Some("someName")
          )
        )
        .success
        .value

      val application = applicationBuilder(Some(userAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      val request = FakeRequest(POST, controllers.subscription.routes.SecondaryContactNameController.onSubmit(NormalMode).url)
        .withFormUrlEncodedBody("value" -> "someName")

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.SecondaryContactEmailController.onPageLoad(NormalMode).url

      }
    }

  }
}
