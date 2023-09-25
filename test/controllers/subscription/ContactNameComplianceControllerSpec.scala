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
import forms.ContactNameComplianceFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.SubscriptionPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.ContactNameComplianceView

import scala.concurrent.Future

class ContactNameComplianceControllerSpec extends SpecBase {

  val formProvider = new ContactNameComplianceFormProvider()

  "Contact Name Compliance controller" when {

    "must return OK and the correct view for a GET" in {
      val userAnswersWithNominatedFilingMemberWithSub =
        userAnswersNfmNoId.set(SubscriptionPage, validSubscriptionDataWithUsePrimaryName()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNominatedFilingMemberWithSub)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactNameComplianceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must return NOT_FOUND if previous page not defined" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContactNameComplianceView]

        status(result) mustEqual NOT_FOUND
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswersWithNominatedFilingMemberWithSub =
        userAnswersNfmNoId.set(SubscriptionPage, validSubscriptionDataWithUsePrimaryName()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNominatedFilingMemberWithSub)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ContactNameComplianceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must redirect to UnderConstruction When No is submited" in {

      val userAnswersWithNominatedFilingMemberWithSub =
        userAnswersNfmNoId.set(SubscriptionPage, validSubscriptionDataWithUsePrimaryName()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNominatedFilingMemberWithSub))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.subscription.routes.ContactNameComplianceController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", "Primary Contact name"))

        val boundForm = formProvider().bind(Map("value" -> "Primary Contact name"))

        val view = application.injector.instanceOf[ContactNameComplianceView]

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.ContactEmailAddressController.onPageLoad(NormalMode).url
      }

    }

  }
}
