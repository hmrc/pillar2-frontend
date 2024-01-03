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

package services.audit

import models.audit.{GrsAuditEvent, GrsAuditEventForLLP, GrsReturnAuditEvent, GrsReturnAuditEventForLLP}
import models.grs.GrsCreateRegistrationResponse
import models.registration.{IncorporatedEntityCreateRegistrationRequest, IncorporatedEntityRegistrationData, PartnershipEntityRegistrationData}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuditService @Inject() (
  auditConnector: AuditConnector
)(implicit ec:    ExecutionContext)
    extends Logging {

  def auditGrsForLimitedCompany(
    registrationRequest: IncorporatedEntityCreateRegistrationRequest,
    responseReceived:    GrsCreateRegistrationResponse
  )(implicit hc:         HeaderCarrier): Future[AuditResult] =
    auditConnector.sendExtendedEvent(
      GrsAuditEvent(
        requestData = registrationRequest,
        responseData = responseReceived
      ).extendedDataEvent
    )

  def auditGrsReturnForLimitedCompany(
    responseReceived: IncorporatedEntityRegistrationData
  )(implicit hc:      HeaderCarrier): Future[AuditResult] =
    auditConnector.sendExtendedEvent(
      GrsReturnAuditEvent(
        responseData = responseReceived
      ).extendedDataEvent
    )

  def auditGrsForLLP(
    registrationRequest: IncorporatedEntityCreateRegistrationRequest,
    responseReceived:    GrsCreateRegistrationResponse
  )(implicit hc:         HeaderCarrier): Future[AuditResult] =
    auditConnector.sendExtendedEvent(
      GrsAuditEventForLLP(
        requestData = registrationRequest,
        responseData = responseReceived
      ).extendedDataEvent
    )

  def auditGrsReturnForLLP(
    responseReceived: PartnershipEntityRegistrationData
  )(implicit hc:      HeaderCarrier): Future[AuditResult] =
    auditConnector.sendExtendedEvent(
      GrsReturnAuditEventForLLP(
        responseData = responseReceived
      ).extendedDataEvent
    )

}
