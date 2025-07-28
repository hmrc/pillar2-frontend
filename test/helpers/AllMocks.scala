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
import controllers.actions.{AgentAccessFilterAction, DataRequiredAction, DataRetrievalAction, SubscriptionDataRetrievalAction}
import forms.TradingBusinessConfirmationFormProvider
import models.fm.FilingMember
import models.registration.Registration
import navigation.UltimateParentNavigator
import org.mockito.Mockito
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.mvc.MessagesControllerComponents
import repositories.SessionRepository
import services._
import services.audit.AuditService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.client.HttpClientV2
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
  val mockSessionRepository:                       SessionRepository                       = mock[SessionRepository]
  val mockNavigator:                               UltimateParentNavigator                 = mock[UltimateParentNavigator]
  val mockDataRetrievalAction:                     DataRetrievalAction                     = mock[DataRetrievalAction]
  val mockSubscriptionDataRetrievalAction:         SubscriptionDataRetrievalAction         = mock[SubscriptionDataRetrievalAction]
  val mockDataRequiredAction:                      DataRequiredAction                      = mock[DataRequiredAction]
  val mockSubscriptionService:                     SubscriptionService                     = mock[SubscriptionService]
  val mockObligationsAndSubmissionsService:        ObligationsAndSubmissionsService        = mock[ObligationsAndSubmissionsService]
  val mockControllerComponents:                    MessagesControllerComponents            = mock[MessagesControllerComponents]
  val mockCheckYourAnswersView:                    CheckYourAnswersView                    = mock[CheckYourAnswersView]
  val mockDashboardView:                           DashboardView                           = mock[DashboardView]
  val mockTradingBusinessConfirmationFormProvider: TradingBusinessConfirmationFormProvider = mock[TradingBusinessConfirmationFormProvider]
  val mockIncorporatedEntityIdentificationFrontendConnector: IncorporatedEntityIdentificationFrontendConnector =
    mock[IncorporatedEntityIdentificationFrontendConnector]
  val mockPartnershipIdentificationFrontendConnector: PartnershipIdentificationFrontendConnector = mock[PartnershipIdentificationFrontendConnector]
  val mockHttpClient:                                 HttpClientV2                               = mock[HttpClientV2]
  val mockRegistrationConnector:                      RegistrationConnector                      = mock[RegistrationConnector]
  val mockSubscriptionConnector:                      SubscriptionConnector                      = mock[SubscriptionConnector]
  val mockEnrolmentStoreProxyConnector:               EnrolmentStoreProxyConnector               = mock[EnrolmentStoreProxyConnector]
  val mockRegistration:                               Registration                               = mock[Registration]
  val mockFilingMember:                               FilingMember                               = mock[FilingMember]
  val mockAuditService:                               AuditService                               = mock[AuditService]
  val mockEnrolmentConnector:                         TaxEnrolmentConnector                      = mock[TaxEnrolmentConnector]
  val mockBarsConnector:                              BarsConnector                              = mock[BarsConnector]
  val mockBarsService:                                BarsService                                = mock[BarsService]
  val mockRepaymentConnector:                         RepaymentConnector                         = mock[RepaymentConnector]
  val mockRepaymentService:                           RepaymentService                           = mock[RepaymentService]
  val mockTransactionHistoryConnector:                TransactionHistoryConnector                = mock[TransactionHistoryConnector]
  val mockAgentAccessFilterAction:                    AgentAccessFilterAction                    = mock[AgentAccessFilterAction]
  val mockBTNService:                                 BTNService                                 = mock[BTNService]
  val mockBTNConnector:                               BTNConnector                               = mock[BTNConnector]

  override protected def beforeEach(): Unit =
    Seq(
      mockAuditConnector,
      mockRepaymentConnector,
      mockAuthConnector,
      mockFrontendAppConfig,
      mockRepaymentService,
      mockUserAnswersConnectors,
      mockCountryOptions,
      mockNavigator,
      mockSubscriptionService,
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
      mockEnrolmentConnector,
      mockSessionRepository
    ).foreach(Mockito.reset(_))
}
