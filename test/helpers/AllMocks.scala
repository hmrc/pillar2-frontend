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

package helpers

import cache.SessionData
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.TradingBusinessConfirmationFormProvider
import navigation.Navigator
import org.mockito.Mockito
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

//TODO: Add all mocking instants in here.
trait AllMocks extends MockitoSugar { me: BeforeAndAfterEach =>

  val mockAuditConnector:                          AuditConnector                          = mock[AuditConnector]
  val mockAuthConnector:                           AuthConnector                           = mock[AuthConnector]
  val mockFrontendAppConfig:                       FrontendAppConfig                       = mock[FrontendAppConfig]
  val mockUserAnswersConnectors:                   UserAnswersConnectors                   = mock[UserAnswersConnectors]
  val mockSessionData:                             SessionData                             = mock[SessionData]
  val mockNavigator:                               Navigator                               = mock[Navigator]
  val mockIdentifierAction:                        IdentifierAction                        = mock[IdentifierAction]
  val mockDataRetrievalAction:                     DataRetrievalAction                     = mock[DataRetrievalAction]
  val mockDataRequiredAction:                      DataRequiredAction                      = mock[DataRequiredAction]
  val mockTradingBusinessConfirmationFormProvider: TradingBusinessConfirmationFormProvider = mock[TradingBusinessConfirmationFormProvider]
  val

  override protected def beforeEach(): Unit =
    Seq(
      mockAuditConnector,
      mockAuthConnector,
      mockFrontendAppConfig,
      mockUserAnswersConnectors,
      mockNavigator,
      mockIdentifierAction,
      mockDataRetrievalAction,
      mockDataRequiredAction,
      mockTradingBusinessConfirmationFormProvider
    ).foreach(Mockito.reset(_))
}
