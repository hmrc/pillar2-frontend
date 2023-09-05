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
import forms.{IsNFMUKBasedFormProvider, UseContactPrimaryFormProvider}
import models.fm.FilingMember
import models.{NfmRegisteredInUkConfirmation, NfmRegistrationConfirmation, NormalMode, UseContactPrimary, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{NominatedFilingMemberPage, SubscriptionPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.RowStatus
import views.html.fmview.IsNFMUKBasedView
import views.html.subscriptionview.UseContactPrimaryView

import scala.concurrent.Future

class UseContactPrimaryControllerSpec extends SpecBase {

  val formProvider = new UseContactPrimaryFormProvider()

  "UseContact Primary Controller" when {

    "must return OK and the correct view for a GET" in {
      val userAnswersWithNominatedFilingMemberWithSub =
        userAnswersNfmNoId.set(SubscriptionPage, validSubscriptionData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNominatedFilingMemberWithSub)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET with upe" in {
      val userAnswersWithUpeWithSub =
        userAnswersWithNoId.set(SubscriptionPage, validSubscriptionData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithUpeWithSub)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value
        val view   = application.injector.instanceOf[UseContactPrimaryView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode, "TestName", "test@test.com", "1234567")(
          request,
          appConfig(application),
          messages(application)
        ).toString

      }
    }
    "redirect to ask primary contact name  if nfm and  upe contact not available" in {
      val userAnswersWithNominatedFilingMemberWithSub =
        userAnswersNfmYesId.set(SubscriptionPage, validSubscriptionData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNominatedFilingMemberWithSub)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode).url
      }
    }

    "must return NOT_FOUND if previous page not defined" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual NOT_FOUND
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
          FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UseContactPrimaryView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must redirect to next page when Yes is selected" in {

      val userAnswersWithNominatedFilingMemberWithSub =
        userAnswersNfmNoId.set(SubscriptionPage, validSubscriptionData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNominatedFilingMemberWithSub))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", UseContactPrimary.Yes.toString))

        val boundForm = formProvider().bind(Map("value" -> UseContactPrimary.Yes.toString))

        val view = application.injector.instanceOf[UseContactPrimaryView]

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }

    }

    "must redirect to next page when Yes is selected with UPE default contact details" in {

      val userAnswersWithUpeMemberWithSub =
        userAnswersWithNoId.set(SubscriptionPage, validSubscriptionData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithUpeMemberWithSub))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", UseContactPrimary.Yes.toString))

        val boundForm = formProvider().bind(Map("value" -> UseContactPrimary.Yes.toString))

        val view = application.injector.instanceOf[UseContactPrimaryView]

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }

    }

    "must redirect to UnderConstruction When No is submitted" in {

      val userAnswersWithNominatedFilingMemberWithSub =
        userAnswersNfmNoId.set(SubscriptionPage, validSubscriptionData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNominatedFilingMemberWithSub))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", UseContactPrimary.No.toString))

        val boundForm = formProvider().bind(Map("value" -> UseContactPrimary.No.toString))

        val view = application.injector.instanceOf[UseContactPrimaryView]

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode).url
      }

    }

    "must redirect to UnderConstruction When No is submitted with upe contact details" in {

      val userAnswersWithUpeContactWithSub =
        userAnswersWithNoId.set(SubscriptionPage, validSubscriptionData()).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithUpeContactWithSub))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", UseContactPrimary.No.toString))

        val boundForm = formProvider().bind(Map("value" -> UseContactPrimary.No.toString))

        val view = application.injector.instanceOf[UseContactPrimaryView]

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode).url
      }

    }

  }
}
