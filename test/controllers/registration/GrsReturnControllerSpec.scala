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

package controllers.registration

import base.SpecBase
import connectors.{IncorporatedEntityIdentificationFrontendConnector, PartnershipIdentificationFrontendConnector, UserAnswersConnectors}
import models.grs.EntityType
import models.registration.{IncorporatedEntityRegistrationData, PartnershipEntityRegistrationData}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{fmEntityTypePage, upeEntityTypePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class GrsReturnControllerSpec extends SpecBase {

  private val validRegistrationWithIdResponseForLLP: String =
    s"""{
       |  "companyProfile" : {
       |                "companyName" : "Test Example Partnership Name",
       |                "companyNumber" : "76543210",
       |                "dateOfIncorporation" : "2010-12-12",
       |                "unsanitisedCHROAddress" : {
       |                    "address_line_1" : "Address Line 1",
       |                    "address_line_2" : "Address Line 2",
       |                    "country" : "United Kingdom",
       |                    "locality" : "Town",
       |                    "postal_code" : "AB12 3CD",
       |                    "region" : "Region"
       |                }
       |            },
       |            "sautr" : "1234567890",
       |            "postcode" : "AA11AA",
       |            "identifiersMatch" : true,
       |            "registration" : {
       |                "registrationStatus" : "REGISTERED",
       |                "registeredBusinessPartnerId" : "XB0000000000001"
       |            }
       |  }

       """.stripMargin

  private val validRegistrationWithIdResponse: String =
    s"""{
       |            "companyProfile" : {
       |                "companyName" : "Test Example Company Name",
       |                "companyNumber" : "76543210",
       |                "dateOfIncorporation" : "2010-12-12",
       |                "unsanitisedCHROAddress" : {
       |                    "address_line_1" : "Address Line 1",
       |                    "address_line_2" : "Address Line 2",
       |                    "locality" : "Town",
       |                    "postal_code" : "AB12 3CD",
       |                    "region" : "Region"
       |                }
       |            },
       |            "ctutr" : "1234567890",
       |            "identifiersMatch" : true,
       |            "registration" : {
       |                "registrationStatus" : "REGISTERED",
       |                "registeredBusinessPartnerId" : "XB0000000000001"
       |            }
       |  }

       """.stripMargin

  private val registrationNotCalledLimited: String =
    s"""{
       |            "companyProfile" : {
       |                "companyName" : "Test Example Company Name",
       |                "companyNumber" : "76543210",
       |                "dateOfIncorporation" : "2010-12-12",
       |                "unsanitisedCHROAddress" : {
       |                    "address_line_1" : "Address Line 1",
       |                    "address_line_2" : "Address Line 2",
       |                    "locality" : "Town",
       |                    "postal_code" : "AB12 3CD",
       |                    "region" : "Region"
       |                }
       |            },
       |            "ctutr" : "1234567890",
       |            "identifiersMatch" : false,
       |            "registration" : {
       |                "registrationStatus" : "REGISTRATION_NOT_CALLED"
       |            }
       |  }

       """.stripMargin

  private val registrationNotCalledLLP: String =
    s"""{
       |  "companyProfile" : {
       |                "companyName" : "Test Example Partnership Name",
       |                "companyNumber" : "76543210",
       |                "dateOfIncorporation" : "2010-12-12",
       |                "unsanitisedCHROAddress" : {
       |                    "address_line_1" : "Address Line 1",
       |                    "address_line_2" : "Address Line 2",
       |                    "country" : "United Kingdom",
       |                    "locality" : "Town",
       |                    "postal_code" : "AB12 3CD",
       |                    "region" : "Region"
       |                }
       |            },
       |            "sautr" : "1234567890",
       |            "postcode" : "AA11AA",
       |            "identifiersMatch" : false,
       |            "registration" : {
       |                "registrationStatus" : "REGISTRATION_NOT_CALLED"
       |            }
       |  }
       """.stripMargin

  private val registrationFailedLimitedJs: String =
    s"""{
       |            "companyProfile" : {
       |                "companyName" : "Test Example Company Name",
       |                "companyNumber" : "76543210",
       |                "dateOfIncorporation" : "2010-12-12",
       |                "unsanitisedCHROAddress" : {
       |                    "address_line_1" : "Address Line 1",
       |                    "address_line_2" : "Address Line 2",
       |                    "locality" : "Town",
       |                    "postal_code" : "AB12 3CD",
       |                    "region" : "Region"
       |                }
       |            },
       |            "ctutr" : "1234567890",
       |            "identifiersMatch" : true,
       |            "registration" : {
       |                "registrationStatus" : "REGISTRATION_FAILED"
       |            }
       |  }

       """.stripMargin

  private val registrationFailedLLPJs: String =
    s"""{
       |  "companyProfile" : {
       |                "companyName" : "Test Example Partnership Name",
       |                "companyNumber" : "76543210",
       |                "dateOfIncorporation" : "2010-12-12",
       |                "unsanitisedCHROAddress" : {
       |                    "address_line_1" : "Address Line 1",
       |                    "address_line_2" : "Address Line 2",
       |                    "country" : "United Kingdom",
       |                    "locality" : "Town",
       |                    "postal_code" : "AB12 3CD",
       |                    "region" : "Region"
       |                }
       |            },
       |            "sautr" : "1234567890",
       |            "postcode" : "AA11AA",
       |            "identifiersMatch" : true,
       |            "registration" : {
       |                "registrationStatus" : "REGISTRATION_FAILED"
       |            }
       |  }
       """.stripMargin

  private val validRegisterWithIdResponse = Json.parse(validRegistrationWithIdResponse).as[IncorporatedEntityRegistrationData]
  private val failedIdentifierLimited     = Json.parse(registrationNotCalledLimited).as[IncorporatedEntityRegistrationData]
  private val registrationFailedLimited   = Json.parse(registrationFailedLimitedJs).as[IncorporatedEntityRegistrationData]

  private val validRegisterWithIdResponseForLLP = Json.parse(validRegistrationWithIdResponseForLLP).as[PartnershipEntityRegistrationData]
  private val failedIdentifierLLP               = Json.parse(registrationNotCalledLLP).as[PartnershipEntityRegistrationData]
  private val registrationFailedLLP             = Json.parse(registrationFailedLLPJs).as[PartnershipEntityRegistrationData]

  "GrsReturn Controller" when {

    "must return 303 redirect to the next page with UK Limited company for UPE" in {
      val ua = emptyUserAnswers.set(upeEntityTypePage, EntityType.UkLimitedCompany).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[IncorporatedEntityIdentificationFrontendConnector].toInstance(mockIncorporatedEntityIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        when(mockIncorporatedEntityIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponse))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueUpe("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url
      }

    }

    "must return 303 redirect to the next page with Limited Liability Partnership for UPE" in {
      val ua = emptyUserAnswers.set(upeEntityTypePage, EntityType.LimitedLiabilityPartnership).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponseForLLP))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueUpe("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url

      }

    }

    "must return 303 redirect to the next page with UK Limited company for Filing Member" in {
      val ua = emptyUserAnswers.set(fmEntityTypePage, EntityType.UkLimitedCompany).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[IncorporatedEntityIdentificationFrontendConnector].toInstance(mockIncorporatedEntityIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        when(mockIncorporatedEntityIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponse))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueFm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url
      }

    }

    "must return 303 redirect to the next page with Limited Liability Partnership for Filing Member" in {
      val ua = emptyUserAnswers.set(fmEntityTypePage, EntityType.LimitedLiabilityPartnership).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(validRegisterWithIdResponseForLLP))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueFm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url

      }

    }

    "must redirect to registration not called controller for UPE if GRS fails to identify the entity" in {
      val ua = emptyUserAnswers.set(upeEntityTypePage, EntityType.UkLimitedCompany).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[IncorporatedEntityIdentificationFrontendConnector].toInstance(mockIncorporatedEntityIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        when(mockIncorporatedEntityIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(failedIdentifierLimited))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueUpe("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.GrsRegistrationNotCalledController.onPageLoadUpe.url
      }

    }

    "must redirect to registration not called controller for nfm if GRS fails to identify the entity" in {
      val ua = emptyUserAnswers.set(fmEntityTypePage, EntityType.LimitedLiabilityPartnership).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(failedIdentifierLLP))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueFm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.GrsRegistrationNotCalledController.onPageLoadNfm.url

      }

    }

    "redirect to registration failed controller if grs registration fails in the fm journey " in {
      val ua = emptyUserAnswers.set(fmEntityTypePage, EntityType.UkLimitedCompany).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[IncorporatedEntityIdentificationFrontendConnector].toInstance(mockIncorporatedEntityIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        when(mockIncorporatedEntityIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(registrationFailedLimited))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueFm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.GrsRegistrationFailedController.onPageLoadNfm.url
      }

    }

    "redirect to registration failed controller if grs registration fails in the upe journey" in {
      val ua = emptyUserAnswers.set(upeEntityTypePage, EntityType.LimitedLiabilityPartnership).success.value
      val application = applicationBuilder(userAnswers = Some(ua))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .overrides(bind[PartnershipIdentificationFrontendConnector].toInstance(mockPartnershipIdentificationFrontendConnector))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))

        when(mockPartnershipIdentificationFrontendConnector.getJourneyData(any())(any()))
          .thenReturn(Future.successful(registrationFailedLLP))

        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueUpe("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.GrsRegistrationFailedController.onPageLoadUpe.url

      }

    }

    "redirect to bookmark page if previous page not answered" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, controllers.registration.routes.GrsReturnController.continueFm("journeyId").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.BookmarkPreventionController.onPageLoad.url)
      }
    }
  }
}
