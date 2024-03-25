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
import connectors.{IncorporatedEntityIdentificationFrontendConnector, PartnershipIdentificationFrontendConnector, UserAnswersConnectors}
import forms.NfmEntityTypeFormProvider
import models.NormalMode
import models.grs.{EntityType, GrsCreateRegistrationResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{FmEntityTypePage, FmRegisteredInUKPage}
import play.api.inject.bind
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.NfmEntityTypeView

import scala.concurrent.Future

class NfmEntityTypeControllerSpec extends SpecBase {

  val formProvider = new NfmEntityTypeFormProvider()

  "NfmEntityType Controller" must {

    " return OK and populate view with an empty form if page previously not answered" in {
      val userAnswers = emptyUserAnswers.setOrException(FmRegisteredInUKPage, true)
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmEntityTypeController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NfmEntityTypeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    " populate the view correctly on a GET when page has been previously answered" in {
      val ua =
        emptyUserAnswers
          .setOrException(FmEntityTypePage, EntityType.UkLimitedCompany)
          .setOrException(FmRegisteredInUKPage, true)

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmEntityTypeController.onPageLoad(NormalMode).url)

        val view = application.injector.instanceOf[NfmEntityTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(EntityType.UkLimitedCompany), NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }
    "if chosen, populate view with entity type not listed and set fm as a uk based entity to delete all no ID data" in {
      val ua =
        emptyUserAnswers
          .setOrException(FmEntityTypePage, EntityType.Other)
          .setOrException(FmRegisteredInUKPage, true)
      val jsUserAnswers: JsValue = Json.toJson(ua)
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmEntityTypeController.onPageLoad(NormalMode).url)
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(jsUserAnswers))

        val view = application.injector.instanceOf[NfmEntityTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(EntityType.Other), NormalMode)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "redirect to bookmark page if previous page not answered" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmEntityTypeController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    " return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.fm.routes.NfmEntityTypeController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = formProvider().bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[NfmEntityTypeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }
    " redirect to GRS for UK Limited company" in {

      val ua =
        emptyUserAnswers
          .set(FmEntityTypePage, EntityType.UkLimitedCompany)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[IncorporatedEntityIdentificationFrontendConnector].toInstance(mockIncorporatedEntityIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        when(mockIncorporatedEntityIdentificationFrontendConnector.createLimitedCompanyJourney(any(), any())(any()))
          .thenReturn(
            Future(
              GrsCreateRegistrationResponse(
                "/report-pillar2-top-up-taxes/test-only/stub-grs-journey-data?continueUrl=normalmode&entityType=UkLimitedCompany"
              )
            )
          )

        val request = FakeRequest(POST, controllers.fm.routes.NfmEntityTypeController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", EntityType.UkLimitedCompany.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual "/report-pillar2-top-up-taxes/test-only/stub-grs-journey-data?continueUrl=normalmode&entityType=UkLimitedCompany"
      }

    }

    " redirect to GRS for Limited Liability Partnership" in {

      val ua =
        emptyUserAnswers
          .set(FmEntityTypePage, EntityType.LimitedLiabilityPartnership)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        when(mockPartnershipIdentificationFrontendConnector.createPartnershipJourney(any(), any(), any())(any()))
          .thenReturn(
            Future(
              GrsCreateRegistrationResponse(
                "/report-pillar2-top-up-taxes/test-only/stub-grs-journey-data?continueUrl=normalmode&entityType=LimitedLiabilityPartnership"
              )
            )
          )

        val request = FakeRequest(POST, controllers.fm.routes.NfmEntityTypeController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", EntityType.LimitedLiabilityPartnership.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual "/report-pillar2-top-up-taxes/test-only/stub-grs-journey-data?continueUrl=normalmode&entityType=LimitedLiabilityPartnership"
      }

    }
    "redirect to name registration page if entity type not listed is chosen and set fm as non uk based" in {
      val jsonTobeReturned = Json.toJson(
        emptyUserAnswers
          .setOrException(FmRegisteredInUKPage, false)
          .setOrException(FmEntityTypePage, EntityType.Other)
      )

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.fm.routes.NfmEntityTypeController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", EntityType.Other.toString))
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(jsonTobeReturned))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.fm.routes.NfmNameRegistrationController.onPageLoad(NormalMode).url
      }
    }
  }
}
