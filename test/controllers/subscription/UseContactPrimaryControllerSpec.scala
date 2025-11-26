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

package controllers.subscription

import base.SpecBase
import connectors.UserAnswersConnectors
import controllers.subscription.UseContactPrimaryController.contactSummaryList
import forms.UseContactPrimaryFormProvider
import helpers.ViewInstances
import models.subscription.AccountingPeriod
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.*
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.subscriptionview.UseContactPrimaryView

import java.time.LocalDate
import scala.concurrent.Future

class UseContactPrimaryControllerSpec extends SpecBase with ViewInstances {

  val formProvider = new UseContactPrimaryFormProvider()
  val name         = "name"
  val email        = "email@gmail.com"
  val phone        = "1221312"

  "UseContact Primary Controller" when {

    "must return OK and the correct view if filing member is nominated and they are not uk-based with no phone whilst page previously not answered" in {
      val ua = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(FmRegisteredInUKPage, false)
        .setOrException(FmContactNamePage, name)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(FmContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(FmPhonePreferencePage, false)
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode, contactSummaryList(name, email, None))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
    "must return OK and the correct view if filing member is nominated and they are not uk-based whilst page previously not answered" in {
      val ua = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(FmRegisteredInUKPage, false)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(FmContactNamePage, name)
        .setOrException(FmContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(FmPhonePreferencePage, true)
        .setOrException(FmCapturePhonePage, phone)
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          formProvider(),
          NormalMode,
          contactSummaryList(name, email, Some(phone))
        )(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
    "must return OK and the correct view if filing member is nominated and they are not uk-based with no phone whilst page previously answered" in {
      val ua = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(FmRegisteredInUKPage, false)
        .setOrException(FmContactNamePage, name)
        .setOrException(FmContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(FmPhonePreferencePage, false)
        .setOrException(SubUsePrimaryContactPage, false)
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          formProvider().fill(false),
          NormalMode,
          contactSummaryList(name, email, None)
        )(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
    "must return OK and the correct view if filing member is nominated and they are not uk-based whilst page previously answered" in {
      val ua = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(FmRegisteredInUKPage, false)
        .setOrException(FmContactNamePage, name)
        .setOrException(FmContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(FmPhonePreferencePage, true)
        .setOrException(FmCapturePhonePage, phone)
        .setOrException(SubUsePrimaryContactPage, false)
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          formProvider().fill(false),
          NormalMode,
          contactSummaryList(name, email, Some(phone))
        )(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view if filing member is nominated and they are uk-based with no phone whilst page previously not answered" in {
      val ua = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(FmRegisteredInUKPage, true)
        .setOrException(UpeContactNamePage, name)
        .setOrException(UpeContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(UpePhonePreferencePage, false)
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode, contactSummaryList(name, email, None))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
    "must return OK and the correct view if filing member is nominated and they are uk-based with phone whilst page previously not answered" in {
      val ua = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(FmRegisteredInUKPage, true)
        .setOrException(UpeContactNamePage, name)
        .setOrException(UpeContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(UpePhonePreferencePage, true)
        .setOrException(UpeCapturePhonePage, phone)
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          formProvider(),
          NormalMode,
          contactSummaryList(name, email, Some(phone))
        )(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
    "must return OK and the correct view if filing member is nominated and they are uk-based with no phone whilst page previously answered" in {
      val ua = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(FmRegisteredInUKPage, true)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(UpeContactNamePage, name)
        .setOrException(UpeContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(UpePhonePreferencePage, false)
        .setOrException(SubUsePrimaryContactPage, false)
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          formProvider().fill(false),
          NormalMode,
          contactSummaryList(name, email, None)
        )(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
    "must return OK and the correct view if filing member is nominated and they are uk-based with phone whilst page previously answered" in {
      val ua = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(FmRegisteredInUKPage, true)
        .setOrException(UpeContactNamePage, name)
        .setOrException(UpeContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(UpePhonePreferencePage, true)
        .setOrException(UpeCapturePhonePage, phone)
        .setOrException(SubUsePrimaryContactPage, false)
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          formProvider().fill(false),
          NormalMode,
          contactSummaryList(name, email, Some(phone))
        )(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
    "must redirect to journey recovery if fm has gone through no id journey but contact name not answered/available" in {
      val ua = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(FmRegisteredInUKPage, false)
        .setOrException(FmContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(FmPhonePreferencePage, false)
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
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(FmRegisteredInUKPage, false)
        .setOrException(FmContactNamePage, name)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(FmPhonePreferencePage, false)
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
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(FmRegisteredInUKPage, false)
        .setOrException(FmContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(FmContactNamePage, name)
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
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(FmRegisteredInUKPage, true)
        .setOrException(UpeContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(UpePhonePreferencePage, false)
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
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(FmRegisteredInUKPage, true)
        .setOrException(UpeContactNamePage, name)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(UpePhonePreferencePage, false)
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
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(FmRegisteredInUKPage, false)
        .setOrException(UpeContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(UpeContactNamePage, name)
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
        .setOrException(NominateFilingMemberPage, false)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(UpeContactNamePage, name)
        .setOrException(UpeContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(UpePhonePreferencePage, false)
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode, contactSummaryList(name, email, None))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
    "must return OK with the correct view if no filing member has been nominated with upe non-uk based and page answered previously" in {
      val ua = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, false)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(UpeContactNamePage, name)
        .setOrException(UpeContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(UpePhonePreferencePage, true)
        .setOrException(UpeCapturePhonePage, phone)
        .setOrException(SubUsePrimaryContactPage, false)
      val application = applicationBuilder(Some(ua)).build()
      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          formProvider().fill(false),
          NormalMode,
          contactSummaryList(name, email, Some(phone))
        )(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }
    "must redirect to journey recovery if no answer is provided/available for whether if they want to nominate a filing member" in {
      val ua          = emptyUserAnswers.set(UpeRegisteredInUKPage, false).success.value
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
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(FmRegisteredInUKPage, true)
        .setOrException(UpeContactNamePage, name)
        .setOrException(UpeContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(UpePhonePreferencePage, false)

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UseContactPrimaryView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, contactSummaryList(name, email, None))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted for fm non-uk based" in {

      val ua = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(FmRegisteredInUKPage, false)
        .setOrException(FmContactNamePage, name)
        .setOrException(FmContactEmailPage, email)
        .setOrException(FmPhonePreferencePage, false)

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UseContactPrimaryView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, contactSummaryList(name, email, None))(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must redirect to journey recovery if fm is registered and they are not uk based but no fm contact name can be found" in {

      val ua = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(FmRegisteredInUKPage, false)
        .setOrException(FmContactEmailPage, email)

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }
    }

    "must redirect to journey recovery if fm is registered and they are uk based but no upe contact name can be found" in {

      val ua = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(FmRegisteredInUKPage, false)
        .setOrException(UpeContactEmailPage, email)

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }
    }

    "must redirect to journey recovery if fm is registered and they are not uk based but no fm contact email can be found" in {

      val ua = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(FmRegisteredInUKPage, false)
        .setOrException(FmContactNamePage, name)

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }
    }

    "must redirect to journey recovery if fm is registered and they are uk based but no upe contact email can be found" in {

      val ua = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(FmRegisteredInUKPage, false)
        .setOrException(UpeContactNamePage, name)

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }
    }

    "must redirect to next page when valid data is submitted and use primary contact is yes" in {
      val ua = emptyUserAnswers
        .set(SubSecondaryContactNamePage, "TestName")
        .success
        .value
        .set(SubSecondaryPhonePreferencePage, true)
        .success
        .value
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(FmRegisteredInUKPage, true)
        .setOrException(UpeContactNamePage, name)
        .setOrException(UpeContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(UpePhonePreferencePage, false)

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("value", "true")
            )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.AddSecondaryContactController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to next page when valid data is submitted and use primary contact is no" in {
      val ua = emptyUserAnswers
        .set(SubSecondaryContactNamePage, "TestName")
        .success
        .value
        .set(SubSecondaryPhonePreferencePage, true)
        .success
        .value
        .setOrException(NominateFilingMemberPage, true)
        .setOrException(FmRegisteredInUKPage, true)
        .setOrException(UpeContactNamePage, name)
        .setOrException(UpeContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(UpePhonePreferencePage, false)

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(
              ("value", "false")
            )
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to journey recovery if no data can be fetched for nominate filing member page" in {

      val application = applicationBuilder()
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)
        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)

      }
    }

    "must show ultimate parent entity contact details when nominated filing member is false" in {
      val testDataWithSpecificValues: UserAnswers = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, false)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(UpeContactNamePage, name)
        .setOrException(UpeContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(UpePhonePreferencePage, true)
        .setOrException(UpeCapturePhonePage, phone)

      val application = applicationBuilder(userAnswers = Some(testDataWithSpecificValues))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(testDataWithSpecificValues)))

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustBe OK
        contentAsString(result) mustEqual view(
          formProvider(),
          NormalMode,
          contactSummaryList(name, email, Some(phone))
        )(request, applicationConfig, messages(application)).toString
      }
    }

    "must show saved contact information heading when contact details exist" in {
      val testDataWithContactDetails: UserAnswers = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, false)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(UpeContactNamePage, name)
        .setOrException(UpeContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(UpePhonePreferencePage, true)
        .setOrException(UpeCapturePhonePage, phone)

      val application = applicationBuilder(userAnswers = Some(testDataWithContactDetails))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(testDataWithContactDetails)))

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustBe OK
        contentAsString(result) mustEqual view(
          formProvider(),
          NormalMode,
          contactSummaryList(name, email, Some(phone))
        )(request, applicationConfig, messages(application)).toString
      }
    }

    "must retain No selection when user previously declined to use primary contact" in {
      val testDataWithPreviousAnswer: UserAnswers = emptyUserAnswers
        .setOrException(NominateFilingMemberPage, false)
        .setOrException(SubAccountingPeriodPage, AccountingPeriod(LocalDate.now(), LocalDate.now()))
        .setOrException(UpeContactNamePage, name)
        .setOrException(UpeContactEmailPage, email)
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(UpePhonePreferencePage, true)
        .setOrException(UpeCapturePhonePage, phone)
        .setOrException(SubUsePrimaryContactPage, false)

      val application = applicationBuilder(userAnswers = Some(testDataWithPreviousAnswer))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      when(mockUserAnswersConnectors.getUserAnswer(any())(any())).thenReturn(Future.successful(Some(testDataWithPreviousAnswer)))

      running(application) {
        val request = FakeRequest(GET, controllers.subscription.routes.UseContactPrimaryController.onPageLoad(NormalMode).url)
        val result  = route(application, request).value

        val view = application.injector.instanceOf[UseContactPrimaryView]

        status(result) mustBe OK
        contentAsString(result) mustEqual view(
          formProvider().fill(false),
          NormalMode,
          contactSummaryList(name, email, Some(phone))
        )(request, applicationConfig, messages(application)).toString
      }
    }

  }
}
