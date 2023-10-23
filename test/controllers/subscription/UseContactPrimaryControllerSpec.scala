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
import forms.UseContactPrimaryFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.UseContactPrimaryView

import scala.concurrent.Future

class UseContactPrimaryControllerSpec extends SpecBase {

  val formProvider = new UseContactPrimaryFormProvider()
  val name         = "name"
  val email        = "email@gmail.com"
  val telephone    = "1221312"
  "UseContact Primary Controller" when {

    "must return OK and the correct view if filing member is nominated and they are not uk-based with no phone whilst page previously not answered" in {
      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, false)
        .success
        .value
        .set(fmContactNamePage, name)
        .success
        .value
        .set(fmContactEmailPage, email)
        .success
        .value
        .set(upeRegisteredInUKPage, false)
        .success
        .value
        .set(fmPhonePreferencePage, false)
        .success
        .value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode, name, email, None)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }
    "must return OK and the correct view if filing member is nominated and they are not uk-based whilst page previously not answered" in {
      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, false)
        .success
        .value
        .set(fmContactNamePage, name)
        .success
        .value
        .set(fmContactEmailPage, email)
        .success
        .value
        .set(upeRegisteredInUKPage, false)
        .success
        .value
        .set(fmPhonePreferencePage, true)
        .success
        .value
        .set(fmCapturePhonePage, telephone)
        .success
        .value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode, name, email, Some(telephone))(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }
    "must return OK and the correct view if filing member is nominated and they are not uk-based with no phone whilst page previously answered" in {
      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, false)
        .success
        .value
        .set(fmContactNamePage, name)
        .success
        .value
        .set(fmContactEmailPage, email)
        .success
        .value
        .set(upeRegisteredInUKPage, false)
        .success
        .value
        .set(fmPhonePreferencePage, false)
        .success
        .value
        .set(subUsePrimaryContactPage, false)
        .success
        .value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(false), NormalMode, name, email, None)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }
    "must return OK and the correct view if filing member is nominated and they are not uk-based whilst page previously answered" in {
      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, false)
        .success
        .value
        .set(fmContactNamePage, name)
        .success
        .value
        .set(fmContactEmailPage, email)
        .success
        .value
        .set(upeRegisteredInUKPage, false)
        .success
        .value
        .set(fmPhonePreferencePage, true)
        .success
        .value
        .set(fmCapturePhonePage, telephone)
        .success
        .value
        .set(subUsePrimaryContactPage, false)
        .success
        .value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(false), NormalMode, name, email, Some(telephone))(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view if filing member is nominated and they are uk-based with no phone whilst page previously not answered" in {
      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, true)
        .success
        .value
        .set(upeContactNamePage, name)
        .success
        .value
        .set(upeContactEmailPage, email)
        .success
        .value
        .set(upeRegisteredInUKPage, false)
        .success
        .value
        .set(upePhonePreferencePage, false)
        .success
        .value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode, name, email, None)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }
    "must return OK and the correct view if filing member is nominated and they are uk-based with phone whilst page previously not answered" in {
      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, true)
        .success
        .value
        .set(upeContactNamePage, name)
        .success
        .value
        .set(upeContactEmailPage, email)
        .success
        .value
        .set(upeRegisteredInUKPage, false)
        .success
        .value
        .set(upePhonePreferencePage, true)
        .success
        .value
        .set(upeCapturePhonePage, telephone)
        .success
        .value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode, name, email, Some(telephone))(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }
    "must return OK and the correct view if filing member is nominated and they are uk-based with no phone whilst page previously answered" in {
      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, true)
        .success
        .value
        .set(upeContactNamePage, name)
        .success
        .value
        .set(upeContactEmailPage, email)
        .success
        .value
        .set(upeRegisteredInUKPage, false)
        .success
        .value
        .set(upePhonePreferencePage, false)
        .success
        .value
        .set(subUsePrimaryContactPage, false)
        .success
        .value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(false), NormalMode, name, email, None)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }
    "must return OK and the correct view if filing member is nominated and they are uk-based with phone whilst page previously answered" in {
      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, true)
        .success
        .value
        .set(upeContactNamePage, name)
        .success
        .value
        .set(upeContactEmailPage, email)
        .success
        .value
        .set(upeRegisteredInUKPage, false)
        .success
        .value
        .set(upePhonePreferencePage, true)
        .success
        .value
        .set(upeCapturePhonePage, telephone)
        .success
        .value
        .set(subUsePrimaryContactPage, false)
        .success
        .value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(false), NormalMode, name, email, Some(telephone))(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }
    "must redirect to journey recovery if fm has gone through no id journey but contact name not answered/available" in {
      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, false)
        .success
        .value
        .set(fmContactEmailPage, email)
        .success
        .value
        .set(upeRegisteredInUKPage, false)
        .success
        .value
        .set(fmPhonePreferencePage, false)
        .success
        .value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }
    }
    "must redirect to journey recovery if fm has gone through no id journey but contact email not answered/available" in {
      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, false)
        .success
        .value
        .set(fmContactNamePage, name)
        .success
        .value
        .set(upeRegisteredInUKPage, false)
        .success
        .value
        .set(fmPhonePreferencePage, false)
        .success
        .value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }
    }

    "must redirect to journey recovery if fm has gone through no id journey but phone preference is not answered/available" in {
      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, false)
        .success
        .value
        .set(fmContactEmailPage, email)
        .success
        .value
        .set(upeRegisteredInUKPage, false)
        .success
        .value
        .set(fmContactNamePage, name)
        .success
        .value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }
    }

    "must redirect to journey recovery if upe has gone through no id journey but contact name not answered/available" in {
      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, true)
        .success
        .value
        .set(upeContactEmailPage, email)
        .success
        .value
        .set(upeRegisteredInUKPage, false)
        .success
        .value
        .set(upePhonePreferencePage, false)
        .success
        .value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }
    "must redirect to journey recovery if upe has gone through no id journey but contact email not answered/available" in {
      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, true)
        .success
        .value
        .set(upeContactNamePage, name)
        .success
        .value
        .set(upeRegisteredInUKPage, false)
        .success
        .value
        .set(upePhonePreferencePage, false)
        .success
        .value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }
    }

    "must redirect to journey recovery if upe has gone through no id journey but phone preference is not answered/available" in {
      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, false)
        .success
        .value
        .set(upeContactEmailPage, email)
        .success
        .value
        .set(upeRegisteredInUKPage, false)
        .success
        .value
        .set(upeContactNamePage, name)
        .success
        .value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }
    }
    "must return OK with the correct view if no filing member has been nominated with upe non-uk based and page not answered previously" in {
      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, false)
        .success
        .value
        .set(upeContactNamePage, name)
        .success
        .value
        .set(upeContactEmailPage, email)
        .success
        .value
        .set(upeRegisteredInUKPage, false)
        .success
        .value
        .set(upePhonePreferencePage, false)
        .success
        .value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode, name, email, None)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }
    "must return OK with the correct view if no filing member has been nominated with upe non-uk based and page answered previously" in {
      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, false)
        .success
        .value
        .set(upeContactNamePage, name)
        .success
        .value
        .set(upeContactEmailPage, email)
        .success
        .value
        .set(upeRegisteredInUKPage, false)
        .success
        .value
        .set(upePhonePreferencePage, true)
        .success
        .value
        .set(upeCapturePhonePage, telephone)
        .success
        .value
        .set(subUsePrimaryContactPage, false)
        .success
        .value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(false), NormalMode, name, email, Some(telephone))(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }
    "must redirect to journey recovery if no answer is provided/available for whether if they want to nominate a filing member" in {
      val ua          = emptyUserAnswers.set(upeRegisteredInUKPage, false).success.value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "must redirect to journey recovery if no answer is provided/available for whether if the ultimate parent is uk based" in {
      val ua          = emptyUserAnswers.set(NominateFilingMemberPage, false).success.value
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted for nfm uk based" in {

      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, true)
        .success
        .value
        .set(upeContactNamePage, name)
        .success
        .value
        .set(upeContactEmailPage, email)
        .success
        .value
        .set(upeRegisteredInUKPage, false)
        .success
        .value
        .set(upePhonePreferencePage, false)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua))
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
        contentAsString(result) mustEqual view(boundForm, NormalMode, name, email, None)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted for fm non-uk based" in {

      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, false)
        .success
        .value
        .set(fmContactNamePage, name)
        .success
        .value
        .set(fmContactEmailPage, email)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua))
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
        contentAsString(result) mustEqual view(boundForm, NormalMode, name, email, None)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must redirect to journey recovery if fm is registered and they are not uk based but no fm contact name can be found" in {

      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, false)
        .success
        .value
        .set(fmContactEmailPage, email)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }
    }

    "must redirect to journey recovery if fm is registered and they are uk based but no upe contact name can be found" in {

      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, false)
        .success
        .value
        .set(upeContactEmailPage, email)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }
    }

    "must redirect to journey recovery if fm is registered and they are not uk based but no fm contact email can be found" in {

      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, false)
        .success
        .value
        .set(fmContactNamePage, name)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }
    }

    "must redirect to journey recovery if fm is registered and they are uk based but no upe contact email can be found" in {

      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, true)
        .success
        .value
        .set(fmRegisteredInUKPage, false)
        .success
        .value
        .set(upeContactNamePage, name)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }
    }
    "must redirect to journey recovery if no data can be fetched for nominate filing member page" in {

      val application = applicationBuilder()
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }
    }
    "must redirect to add secondary contact detail page when Yes is selected" in {
      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, false)
        .success
        .value
        .set(upeContactNamePage, name)
        .success
        .value
        .set(upeContactEmailPage, email)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.AddSecondaryContactController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to add primary contact name page when no is selected" in {
      val ua = emptyUserAnswers
        .set(NominateFilingMemberPage, false)
        .success
        .value
        .set(upeContactNamePage, name)
        .success
        .value
        .set(upeContactEmailPage, email)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        val request = FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode).url
      }
    }
//
//    "must redirect to Add secondary contact page when Yes is selected with UPE default contact details" in {
//
//      val userAnswersWithUpeMemberWithSub =
//        userAnswersWithNoId.set(SubscriptionPage, validSubscriptionData()).success.value
//
//      val application = applicationBuilder(userAnswers = Some(userAnswersWithUpeMemberWithSub))
//        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
//        .build()
//
//      running(application) {
//        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
//
//        val request = FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onSubmit(NormalMode).url)
//          .withFormUrlEncodedBody(("value", "true"))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual controllers.subscription.routes.AddSecondaryContactController.onPageLoad(NormalMode).url
//      }
//
//    }
//
//    "must redirect to UnderConstruction When No is submitted" in {
//
//      val userAnswersWithNominatedFilingMemberWithSub =
//        userAnswersNfmNoId.set(SubscriptionPage, validSubscriptionData()).success.value
//
//      val application = applicationBuilder(userAnswers = Some(userAnswersWithNominatedFilingMemberWithSub))
//        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
//        .build()
//
//      running(application) {
//        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
//
//        val request = FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onSubmit(NormalMode).url)
//          .withFormUrlEncodedBody(("value", "false"))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode).url
//      }
//
//    }
//
//    "must redirect to UnderConstruction When No is submitted with upe contact details" in {
//
//      val userAnswersWithUpeContactWithSub =
//        userAnswersWithNoId.set(SubscriptionPage, validSubscriptionData()).success.value
//
//      val application = applicationBuilder(userAnswers = Some(userAnswersWithUpeContactWithSub))
//        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
//        .build()
//
//      running(application) {
//        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
//
//        val request = FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onSubmit(NormalMode).url)
//          .withFormUrlEncodedBody(("value", "false"))
//
//        val result = route(application, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode).url
//      }
//
//    }

  }
}
