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

package connectors

import config.FrontendAppConfig
import models.grs.{EntityType, GrsCreateRegistrationResponse, ServiceName}
import models.registration.{IncorporatedEntityCreateRegistrationRequest, PartnershipEntityRegistrationData}
import models.{Mode, UserType}
import play.api.i18n.MessagesApi
import services.audit.AuditService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait PartnershipIdentificationFrontendConnector {
  def createPartnershipJourney(
    userType:        UserType,
    partnershipType: EntityType,
    mode:            Mode
  )(implicit hc:     HeaderCarrier): Future[GrsCreateRegistrationResponse]

  def getJourneyData(journeyId: String)(implicit hc: HeaderCarrier): Future[PartnershipEntityRegistrationData]
}

class PartnershipIdentificationFrontendConnectorImpl @Inject() (
  appConfig:    FrontendAppConfig,
  httpClient:   HttpClient,
  auditService: AuditService
)(implicit
  val messagesApi: MessagesApi,
  ec:              ExecutionContext
) extends PartnershipIdentificationFrontendConnector {
  private val apiUrl = s"${appConfig.partnershipEntityIdentificationFrontendBaseUrl}/partnership-identification/api"

  def createPartnershipJourney(
    userType:        UserType,
    partnershipType: EntityType,
    mode:            Mode
  )(implicit hc:     HeaderCarrier): Future[GrsCreateRegistrationResponse] = {

    val serviceName = ServiceName()
    val registrationRequest = IncorporatedEntityCreateRegistrationRequest(
      continueUrl = s"${appConfig.grsContinueUrl}/${mode.toString.toLowerCase}/${userType.toString.toLowerCase()}",
      businessVerificationCheck = appConfig.partnershipBvEnabled,
      optServiceName = Some(serviceName.en.optServiceName),
      deskProServiceId = appConfig.appName,
      signOutUrl = appConfig.signOutUrl,
      accessibilityUrl = appConfig.accessibilityStatementPath,
      labels = serviceName
    )

    httpClient.POST[IncorporatedEntityCreateRegistrationRequest, GrsCreateRegistrationResponse](
      s"$apiUrl/limited-liability-partnership-journey",
      registrationRequest
    )
  }

  def getJourneyData(journeyId: String)(implicit hc: HeaderCarrier): Future[PartnershipEntityRegistrationData] =
    httpClient.GET[PartnershipEntityRegistrationData](s"$apiUrl/journey/$journeyId")
}
