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

package controllers.rfm

import base.SpecBase
import connectors.{IncorporatedEntityIdentificationFrontendConnector, PartnershipIdentificationFrontendConnector, UserAnswersConnectors}
import forms.RfmEntityTypeFormProvider
import models.NormalMode
import models.grs.{EntityType, GrsCreateRegistrationResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{RfmEntityTypePage, RfmUkBasedPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.rfm.RfmEntityTypeView

import scala.concurrent.Future

class RfmEntityTypeControllerSpec extends SpecBase {

  val formProvider = new RfmEntityTypeFormProvider()

  "RfmEntityTypeController Controller" when {

    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers.setOrException(RfmUkBasedPage, true)
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmEntityTypeController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RfmEntityTypeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "must return Journey Recovery if previous page not answered" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmEntityTypeController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad.url)
      }
    }
    "must populate the view correctly on a GET when the question has previously been answered" in {
      val ua          = emptyUserAnswers.setOrException(RfmUkBasedPage, true)
      val userAnswers = ua.set(RfmEntityTypePage, EntityType.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmEntityTypeController.onPageLoad(NormalMode).url)

        val view = application.injector.instanceOf[RfmEntityTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(EntityType.values.head), NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered And Entity type is other" in {
      val ua          = emptyUserAnswers.setOrException(RfmUkBasedPage, true)
      val userAnswers = ua.set(RfmEntityTypePage, EntityType.Other).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmEntityTypeController.onPageLoad(NormalMode).url)

        val view = application.injector.instanceOf[RfmEntityTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(EntityType.Other), NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.rfm.routes.RfmEntityTypeController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = formProvider().bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[RfmEntityTypeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "must redirect to GRS for UK Limited company" in {

      val ua =
        emptyUserAnswers
          .set(RfmEntityTypePage, EntityType.UkLimitedCompany)
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

        val request = FakeRequest(POST, controllers.rfm.routes.RfmEntityTypeController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", EntityType.UkLimitedCompany.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual "/report-pillar2-top-up-taxes/test-only/stub-grs-journey-data?continueUrl=normalmode&entityType=UkLimitedCompany"
      }

    }

    "must redirect to GRS for Limited Liability Partnership" in {

      val ua =
        emptyUserAnswers
          .set(RfmEntityTypePage, EntityType.LimitedLiabilityPartnership)
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

        val request = FakeRequest(POST, controllers.rfm.routes.RfmEntityTypeController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", EntityType.LimitedLiabilityPartnership.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual "/report-pillar2-top-up-taxes/test-only/stub-grs-journey-data?continueUrl=normalmode&entityType=LimitedLiabilityPartnership"
      }

    }

    "redirect to name registration page if entity type not listed is chosen and set rfm as non uk based" in {
      val jsonTobeReturned = Json.toJson(
        emptyUserAnswers
          .setOrException(RfmUkBasedPage, false)
          .setOrException(RfmEntityTypePage, EntityType.Other)
      )

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.rfm.routes.RfmEntityTypeController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", EntityType.Other.toString))
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(jsonTobeReturned))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmNameRegistrationController.onPageLoad(NormalMode).url
      }
    }

    "redirect to Entity page page if entity type not listed is chosen and set rfm as Non UK based" in {
      val jsonTobeReturned = Json.toJson(
        emptyUserAnswers
          .setOrException(RfmUkBasedPage, true)
          .setOrException(RfmEntityTypePage, EntityType.Other)
      )
      val userAnswers = emptyUserAnswers
        .setOrException(RfmUkBasedPage, false)
        .setOrException(RfmEntityTypePage, EntityType.Other)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.RfmEntityTypeController.onPageLoad(NormalMode).url)
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(jsonTobeReturned))
        val view = application.injector.instanceOf[RfmEntityTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(EntityType.Other), NormalMode)(
          request,
          applicationConfig,
          messages(application)
        ).toString

      }
    }

  }
}
