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

package controllers.fm

import base.SpecBase
import connectors.{IncorporatedEntityIdentificationFrontendConnector, PartnershipIdentificationFrontendConnector, UserAnswersConnectors}
import controllers.routes
import forms.NfmEntityTypeFormProvider
import models.grs.{EntityType, GrsCreateRegistrationResponse}
import models.{NfmRegisteredInUkConfirmation, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{EntityTypePage, NominatedFilingMemberPage}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.NfmEntityTypeView
import play.api.inject.bind

import scala.concurrent.Future

class NfmEntityTypeControllerSpec extends SpecBase {

  val formProvider = new NfmEntityTypeFormProvider()

  "NfmEntityType Controller" when {

    "must return OK and the correct view for a GET" in {
      val userAnswersWithNominatedFilingMember =
        emptyUserAnswers
          .set(NominatedFilingMemberPage, validWithIdFmData(isNfmRegisteredInUK = Some(NfmRegisteredInUkConfirmation.Yes)))
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNominatedFilingMember)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmEntityTypeController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NfmEntityTypeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must return NOT_FOUND When previous page data is not avilable" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.fm.routes.NfmEntityTypeController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NfmEntityTypeView]

        status(result) mustEqual NOT_FOUND
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswersWithNominatedFilingMember =
        emptyUserAnswers
          .set(
            NominatedFilingMemberPage,
            validWithIdFmData(isNfmRegisteredInUK = Some(NfmRegisteredInUkConfirmation.Yes), orgType = Some(EntityType.UkLimitedCompany))
          )
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithNominatedFilingMember)).build()

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

    "must return a Bad Request and errors when invalid data is submitted" in {

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
    "must redirect to GRS for UK Limited company" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithIdForLimitedCompForFm))
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

        val boundForm = formProvider()

        val view = application.injector.instanceOf[NfmEntityTypeView]

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual "/report-pillar2-top-up-taxes/test-only/stub-grs-journey-data?continueUrl=normalmode&entityType=UkLimitedCompany"
      }

    }

    "must redirect to GRS for Limited Liability Partnership" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithIdForLLPForFm))
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

        val boundForm = formProvider()

        val view = application.injector.instanceOf[NfmEntityTypeView]

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual "/report-pillar2-top-up-taxes/test-only/stub-grs-journey-data?continueUrl=normalmode&entityType=LimitedLiabilityPartnership"
      }

    }
  }
}
