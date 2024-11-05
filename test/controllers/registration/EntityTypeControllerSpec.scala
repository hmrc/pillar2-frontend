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

package controllers.registration

import base.SpecBase
import connectors.{IncorporatedEntityIdentificationFrontendConnector, PartnershipIdentificationFrontendConnector, UserAnswersConnectors}
import forms.EntityTypeFormProvider
import models.NormalMode
import models.grs.{EntityType, GrsCreateRegistrationResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{UpeEntityTypePage, UpeRegisteredInUKPage}
import play.api.inject.bind
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.EntityTypeView

import scala.concurrent.Future

class EntityTypeControllerSpec extends SpecBase {

  val formProvider = new EntityTypeFormProvider()

  "EntityType Controller" when {

    "must return OK and the correct view for a GET" in {
      val ua          = emptyUserAnswers.setOrException(UpeRegisteredInUKPage, true)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.EntityTypeController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EntityTypeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(
          request,
          appConfig(),
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val ua = emptyUserAnswers
        .setOrException(UpeEntityTypePage, EntityType.UkLimitedCompany)
        .setOrException(UpeRegisteredInUKPage, true)
      val application = applicationBuilder(userAnswers = Some(ua)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.EntityTypeController.onPageLoad(NormalMode).url)

        val view = application.injector.instanceOf[EntityTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(EntityType.UkLimitedCompany), NormalMode)(
          request,
          appConfig(),
          messages(application)
        ).toString
      }
    }

    "if chosen, populate view with entity type not listed and set fm as a uk based entity to delete all no ID data" in {
      val ua = emptyUserAnswers
        .setOrException(UpeEntityTypePage, EntityType.Other)
        .setOrException(UpeRegisteredInUKPage, true)
      val jsUserAnswers: JsValue = Json.toJson(ua)
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.EntityTypeController.onPageLoad(NormalMode).url)
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(jsUserAnswers))
        val view = application.injector.instanceOf[EntityTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(EntityType.Other), NormalMode)(
          request,
          appConfig(),
          messages(application)
        ).toString
      }
    }

    "if chosen, populate view with entity type not listed and set fm as not uk based entity to delete all no ID data" in {
      val ua = emptyUserAnswers
        .setOrException(UpeRegisteredInUKPage, false)
        .setOrException(UpeEntityTypePage, EntityType.Other)

      val jsUserAnswers: JsValue = Json.toJson(ua)
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(
          bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.EntityTypeController.onPageLoad(NormalMode).url)
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(jsUserAnswers))
        val view = application.injector.instanceOf[EntityTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(EntityType.Other), NormalMode)(
          request,
          appConfig(),
          messages(application)
        ).toString
      }
    }

    "redirect to bookmark page if previous page not answered" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.EntityTypeController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.registration.routes.EntityTypeController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = formProvider().bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[EntityTypeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(), messages(application)).toString
      }
    }

    "must redirect to GRS for UK Limited company" in {
      val ua = emptyUserAnswers.set(UpeEntityTypePage, EntityType.UkLimitedCompany).success.value
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

        val request = FakeRequest(POST, controllers.registration.routes.EntityTypeController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", EntityType.UkLimitedCompany.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual "/report-pillar2-top-up-taxes/test-only/stub-grs-journey-data?continueUrl=normalmode&entityType=UkLimitedCompany"
      }

    }

    "must redirect to GRS for Limited Liability Partnership" in {
      val ua = emptyUserAnswers.set(UpeEntityTypePage, EntityType.LimitedLiabilityPartnership).success.value
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

        val request = FakeRequest(POST, controllers.registration.routes.EntityTypeController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", EntityType.LimitedLiabilityPartnership.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual "/report-pillar2-top-up-taxes/test-only/stub-grs-journey-data?continueUrl=normalmode&entityType=LimitedLiabilityPartnership"
      }

    }

    "redirect to ultimate parent name registration if entity type not listed chosen with set UPE as non-uk based" in {
      val jsonTobeReturned = Json.toJson(
        emptyUserAnswers
          .setOrException(UpeRegisteredInUKPage, false)
          .setOrException(UpeEntityTypePage, EntityType.Other)
      )

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.registration.routes.EntityTypeController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", EntityType.Other.toString))
        when(mockUserAnswersConnectors.save(any(), any())(any()))
          .thenReturn(Future(jsonTobeReturned))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.registration.routes.UpeNameRegistrationController.onPageLoad(NormalMode).url
      }
    }

  }
}
