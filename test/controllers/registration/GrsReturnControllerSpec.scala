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
import models.grs.RegistrationStatus.{Registered, RegistrationNotCalled}
import models.grs.{EntityType, GrsRegistrationResult}
import models.registration.{IncorporatedEntityRegistrationData, PartnershipEntityRegistrationData}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{FmEntityTypePage, RfmEntityTypePage, UpeEntityTypePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class GrsReturnControllerSpec extends SpecBase {

  private val validRegisterWithIdResponse = Json.parse(validRegistrationWithIdResponse).as[IncorporatedEntityRegistrationData]
  private val validRegisterWithIdResponseWithoutPartner =
    Json.parse(validRegistrationWithIdResponseWithoutPartnerId).as[IncorporatedEntityRegistrationData]
  private val failedIdentifierLimited   = Json.parse(registrationNotCalledLimited).as[IncorporatedEntityRegistrationData]
  private val registrationFailedLimited = Json.parse(registrationFailedLimitedJs).as[IncorporatedEntityRegistrationData]

  private val validRegisterWithIdResponseForLLP = Json.parse(validRegistrationWithIdResponseForLLP).as[PartnershipEntityRegistrationData]
  private val validRegisterWithIdResponseForLLPWithoutPartner =
    Json.parse(validRegistrationWithIdResponseForLLPWithoutPartnerId).as[PartnershipEntityRegistrationData]
  private val failedIdentifierLLP   = Json.parse(registrationNotCalledLLP).as[PartnershipEntityRegistrationData]
  private val registrationFailedLLP = Json.parse(registrationFailedLLPJs).as[PartnershipEntityRegistrationData]

  "GrsReturn Controller" must {

    "return 303 redirect to the next page with UK Limited company for UPE" in {
      val ua = emptyUserAnswers.set(UpeEntityTypePage, EntityType.UkLimitedCompany).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[IncorporatedEntityIdentificationFrontendConnector].toInstance(mockIncorporatedEntityIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        when(mockIncorporatedEntityIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponse))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueUpe("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url
      }

    }

    "return 303 redirect to the next page with Limited Liability Partnership for UPE" in {
      val ua = emptyUserAnswers.set(UpeEntityTypePage, EntityType.LimitedLiabilityPartnership).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponseForLLP))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueUpe("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url

      }

    }

    "redirect to Journey Recovery page if Upe Entity Type not answered" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponseForLLP))
        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueUpe("journeyId").url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery page if UK Limited company for UPE and no registered Business Partner Id exists" in {
      val ua = emptyUserAnswers.set(UpeEntityTypePage, EntityType.UkLimitedCompany).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[IncorporatedEntityIdentificationFrontendConnector].toInstance(mockIncorporatedEntityIdentificationFrontendConnector))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        when(mockIncorporatedEntityIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponse.copy(registration = GrsRegistrationResult(Registered, None, None))))
        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueUpe("journeyId").url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery page if Limited Liability Partnership for UPE and no registered Business Partner Id exists" in {
      val ua = emptyUserAnswers.set(UpeEntityTypePage, EntityType.LimitedLiabilityPartnership).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponseForLLP.copy(registration = GrsRegistrationResult(Registered, None, None))))
        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueUpe("journeyId").url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "return 303 redirect to the next page with UK Limited company for Filing Member" in {
      val ua = emptyUserAnswers.set(FmEntityTypePage, EntityType.UkLimitedCompany).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[IncorporatedEntityIdentificationFrontendConnector].toInstance(mockIncorporatedEntityIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        when(mockIncorporatedEntityIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponse))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueFm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url
      }

    }

    "redirect to Journey Recovery page if UK Limited company for FM and no registered Business Partner Id exists" in {
      val ua = emptyUserAnswers.set(FmEntityTypePage, EntityType.UkLimitedCompany).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[IncorporatedEntityIdentificationFrontendConnector].toInstance(mockIncorporatedEntityIdentificationFrontendConnector))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        when(mockIncorporatedEntityIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponse.copy(registration = GrsRegistrationResult(Registered, None, None))))
        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueFm("journeyId").url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "return 303 redirect to the next page with Limited Liability Partnership for Filing Member" in {
      val ua = emptyUserAnswers.set(FmEntityTypePage, EntityType.LimitedLiabilityPartnership).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponseForLLP))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueFm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url

      }

    }

    "redirect to Journey Recovery page if Limited Liability Partnership for Filing Member and no registered Business Partner Id exists" in {
      val ua = emptyUserAnswers.set(FmEntityTypePage, EntityType.LimitedLiabilityPartnership).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponseForLLP.copy(registration = GrsRegistrationResult(Registered, None, None))))
        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueFm("journeyId").url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "return 303 redirect to the next page with UK Limited company for RFM" in {
      val ua = emptyUserAnswers.set(RfmEntityTypePage, EntityType.UkLimitedCompany).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[IncorporatedEntityIdentificationFrontendConnector].toInstance(mockIncorporatedEntityIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        when(mockIncorporatedEntityIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponse))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueRfm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmContactDetailsRegistrationController.onPageLoad.url
      }

    }

    "return 303 redirect to the next page with Limited Liability Partnership for RFM" in {
      val ua = emptyUserAnswers.set(RfmEntityTypePage, EntityType.LimitedLiabilityPartnership).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponseForLLP))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueRfm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.rfm.routes.RfmContactDetailsRegistrationController.onPageLoad.url

      }

    }

    "redirect to Journey Recovery page if Limited Liability Partnership for RFM and no Company Profile exists" in {
      val ua = emptyUserAnswers.set(RfmEntityTypePage, EntityType.LimitedLiabilityPartnership).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponseForLLP.copy(companyProfile = None)))
        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueRfm("journeyId").url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery page if Limited Liability Partnership for RFM and no SAUTR exists" in {
      val ua = emptyUserAnswers.set(RfmEntityTypePage, EntityType.LimitedLiabilityPartnership).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponseForLLP.copy(sautr = None)))
        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueRfm("journeyId").url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery page if Limited Liability Partnership for RFM and no registered Business Partner Id exists" in {
      val ua = emptyUserAnswers.set(RfmEntityTypePage, EntityType.LimitedLiabilityPartnership).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponseForLLP.copy(registration = GrsRegistrationResult(Registered, None, None))))
        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueRfm("journeyId").url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "return 303 redirect to Journey Recovery page for RFM When RFMEntityType not set" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueRfm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

      }

    }

    "return 303 redirect to Journey Recovery page for RFM When businesspartnerId not set for UKLimited " in {

      val ua = emptyUserAnswers.set(RfmEntityTypePage, EntityType.UkLimitedCompany).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[IncorporatedEntityIdentificationFrontendConnector].toInstance(mockIncorporatedEntityIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        when(mockIncorporatedEntityIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponseWithoutPartner))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueRfm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

      }

    }

    "return 303 redirect to Journey Recovery page for RFM When businesspartnerId not set for LLP " in {
      val ua = emptyUserAnswers.set(FmEntityTypePage, EntityType.LimitedLiabilityPartnership).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponseForLLPWithoutPartner))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueRfm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

      }

    }

    "redirect to registration not called controller for UPE if GRS fails to identify the entity" in {
      val ua = emptyUserAnswers.set(UpeEntityTypePage, EntityType.UkLimitedCompany).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[IncorporatedEntityIdentificationFrontendConnector].toInstance(mockIncorporatedEntityIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        when(mockIncorporatedEntityIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(failedIdentifierLimited))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueUpe("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.GrsRegistrationNotCalledController.onPageLoadUpe.url
      }

    }

    "redirect to registration not called controller for nfm if GRS fails to identify the entity" in {
      val ua = emptyUserAnswers.set(FmEntityTypePage, EntityType.LimitedLiabilityPartnership).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(failedIdentifierLLP))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueFm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.GrsRegistrationNotCalledController.onPageLoadNfm.url

      }

    }

    "redirect to registration failed controller if grs registration fails in the fm journey " in {
      val ua = emptyUserAnswers.set(FmEntityTypePage, EntityType.UkLimitedCompany).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[IncorporatedEntityIdentificationFrontendConnector].toInstance(mockIncorporatedEntityIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        when(mockIncorporatedEntityIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(registrationFailedLimited))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueFm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.GrsRegistrationFailedController.onPageLoadNfm.url
      }

    }

    "redirect to registration failed controller if grs registration fails in the upe journey" in {
      val ua = emptyUserAnswers.set(UpeEntityTypePage, EntityType.LimitedLiabilityPartnership).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(registrationFailedLLP))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueUpe("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.GrsRegistrationFailedController.onPageLoadUpe.url

      }

    }

    "redirect to registration failed controller if grs registration fails in the RFM journey " in {
      val ua = emptyUserAnswers.set(RfmEntityTypePage, EntityType.UkLimitedCompany).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[IncorporatedEntityIdentificationFrontendConnector].toInstance(mockIncorporatedEntityIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        when(mockIncorporatedEntityIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(registrationFailedLimited))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueRfm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.GrsRegistrationFailedController.onPageLoadRfm.url
      }

    }

    "redirect to registration not called controller for nfm if GRS fails to identify the entity in RFM journey" in {
      val ua = emptyUserAnswers.set(RfmEntityTypePage, EntityType.LimitedLiabilityPartnership).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))

        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(failedIdentifierLLP))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueRfm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.GrsRegistrationNotCalledController.onPageLoadRfm.url

      }

    }

    "redirect to JourneyRecoveryController for RFM if an invalid result received from GRS" in {
      val ua = emptyUserAnswers.set(RfmEntityTypePage, EntityType.LimitedLiabilityPartnership).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future.successful(Json.toJson(Json.obj())))
        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(
            Future.successful(
              validRegisterWithIdResponseForLLP
                .copy(
                  identifiersMatch = true,
                  businessVerification = None,
                  registration = GrsRegistrationResult(RegistrationNotCalled, None, None)
                )
            )
          )
        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueRfm("journeyId").url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to bookmark page if previous page not answered" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueFm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }
  }
}
