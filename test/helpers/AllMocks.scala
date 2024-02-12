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

package helpers

import config.FrontendAppConfig
import connectors._
import controllers.actions.{DataRequiredAction, DataRetrievalAction}
import forms.TradingBusinessConfirmationFormProvider
import models.fm.FilingMember
import models.registration.Registration
import navigation.Navigator
import org.mockito.Mockito
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.mvc.MessagesControllerComponents
import repositories.SessionRepository
import services.audit.AuditService
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import utils.countryOptions.CountryOptions
import views.html.{CheckYourAnswersView, DashboardView}

//TODO: Add all mocking instants in here.
trait AllMocks extends MockitoSugar { me: BeforeAndAfterEach =>

  val mockAuditConnector:                          AuditConnector                          = mock[AuditConnector]
  val mockAuthConnector:                           AuthConnector                           = mock[AuthConnector]
  val mockFrontendAppConfig:                       FrontendAppConfig                       = mock[FrontendAppConfig]
  val mockUserAnswersConnectors:                   UserAnswersConnectors                   = mock[UserAnswersConnectors]
  val mockCountryOptions:                          CountryOptions                          = mock[CountryOptions]
  val mockMessagesApi:                             MessagesApi                             = mock[MessagesApi]
  val mockSessionData:                             SessionData                             = mock[SessionData]
  val mockSessionRepository:                       SessionRepository                       = mock[SessionRepository]
  val mockNavigator:                               Navigator                               = mock[Navigator]
  val mockDataRetrievalAction:                     DataRetrievalAction                     = mock[DataRetrievalAction]
  val mockDataRequiredAction:                      DataRequiredAction                      = mock[DataRequiredAction]
  val mockSubscriptionService:                     SubscriptionService                     = mock[SubscriptionService]
  val mockControllerComponents:                    MessagesControllerComponents            = mock[MessagesControllerComponents]
  val mockCheckYourAnswersView:                    CheckYourAnswersView                    = mock[CheckYourAnswersView]
  val mockDashboardView:                           DashboardView                           = mock[DashboardView]
  val mockTradingBusinessConfirmationFormProvider: TradingBusinessConfirmationFormProvider = mock[TradingBusinessConfirmationFormProvider]
  val mockIncorporatedEntityIdentificationFrontendConnector: IncorporatedEntityIdentificationFrontendConnector =
    mock[IncorporatedEntityIdentificationFrontendConnector]
  val mockPartnershipIdentificationFrontendConnector: PartnershipIdentificationFrontendConnector = mock[PartnershipIdentificationFrontendConnector]
  val mockHttpClient:                                 HttpClient                                 = mock[HttpClient]
  val mockRegistrationConnector:                      RegistrationConnector                      = mock[RegistrationConnector]
  val mockSubscriptionConnector:                      SubscriptionConnector                      = mock[SubscriptionConnector]
  val mockAmendSubscriptionService:                   AmendSubscriptionService                   = mock[AmendSubscriptionService]
  val mockEnrolmentStoreProxyConnector:               EnrolmentStoreProxyConnector               = mock[EnrolmentStoreProxyConnector]
  val mockRegistration:                               Registration                               = mock[Registration]
  val mockFilingMember:                               FilingMember                               = mock[FilingMember]
  val mockReadSubscriptionService:                    ReadSubscriptionService                    = mock[ReadSubscriptionService]
  val mockReadSubscriptionConnector:                  ReadSubscriptionConnector                  = mock[ReadSubscriptionConnector]
  val mockAmendSubscriptionConnector:                 AmendSubscriptionConnector                 = mock[AmendSubscriptionConnector]
  val mockAuditService:                               AuditService                               = mock[AuditService]
  val mockEnrolmentConnector:                         EnrolmentConnector                         = mock[EnrolmentConnector]

  override protected def beforeEach(): Unit =
    Seq(
      mockAuditConnector,
      mockAuthConnector,
      mockFrontendAppConfig,
      mockUserAnswersConnectors,
      mockCountryOptions,
      mockNavigator,
      mockReadSubscriptionService,
      mockDataRetrievalAction,
      mockDataRequiredAction,
      mockTradingBusinessConfirmationFormProvider,
      mockIncorporatedEntityIdentificationFrontendConnector,
      mockPartnershipIdentificationFrontendConnector,
      mockHttpClient,
      mockRegistrationConnector,
      mockSubscriptionConnector,
      mockEnrolmentStoreProxyConnector,
      mockAuditService,
      mockEnrolmentConnector
    ).foreach(Mockito.reset(_))
}
