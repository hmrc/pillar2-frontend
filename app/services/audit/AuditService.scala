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

import models.audit._
import models.grs.EntityType
import models.registration.{IncorporatedEntityAddress, IncorporatedEntityRegistrationData, PartnershipEntityRegistrationData}
import models.subscription.NewFilingMemberDetail
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuditService @Inject() (
  auditConnector: AuditConnector
)(implicit ec:    ExecutionContext)
    extends Logging {

  def auditGrsReturnForLimitedCompany(
    responseReceived: IncorporatedEntityRegistrationData
  )(implicit hc:      HeaderCarrier): Future[AuditResult] =
    auditConnector.sendExtendedEvent(
      GrsReturnAuditEvent(
        entityType = EntityType.UkLimitedCompany.toString,
        companyName = responseReceived.companyProfile.companyName,
        companyNumber = responseReceived.companyProfile.companyNumber,
        dateOfIncorporation = responseReceived.companyProfile.dateOfIncorporation.toString,
        address_line_1 = responseReceived.companyProfile.unsanitisedCHROAddress.address_line_1.getOrElse(""),
        address_line_2 = responseReceived.companyProfile.unsanitisedCHROAddress.address_line_2.getOrElse(""),
        country = responseReceived.companyProfile.unsanitisedCHROAddress.country.getOrElse(""),
        locality = responseReceived.companyProfile.unsanitisedCHROAddress.locality.getOrElse(""),
        postal_code = responseReceived.companyProfile.unsanitisedCHROAddress.postal_code.getOrElse(""),
        region = responseReceived.companyProfile.unsanitisedCHROAddress.region.getOrElse(""),
        ctutr = responseReceived.ctutr,
        identifiersMatch = responseReceived.identifiersMatch,
        businessVerification = responseReceived.businessVerification,
        registrationStatus = responseReceived.registration
      ).extendedDataEvent
    )

  def auditGrsReturnForLLP(
    responseReceived: PartnershipEntityRegistrationData
  )(implicit hc:      HeaderCarrier): Future[AuditResult] = {
    val emptyString = ""
    val companyProfile = responseReceived.companyProfile
      .map(profile => (profile.companyName, profile.companyNumber, profile.dateOfIncorporation.toString))
      .getOrElse(("", "", ""))
    val companyAddress = responseReceived.companyProfile
      .map(data => data.unsanitisedCHROAddress)
      .fold(
        IncorporatedEntityAddress(
          Some(emptyString),
          Some(emptyString),
          Some(emptyString),
          Some(emptyString),
          Some(emptyString),
          Some(emptyString),
          Some(emptyString),
          Some(emptyString)
        )
      )(address => address)

    auditConnector.sendExtendedEvent(
      GrsReturnAuditEventForLLP(
        entityType = EntityType.LimitedLiabilityPartnership.toString,
        companyName = companyProfile._1,
        companyNumber = companyProfile._2,
        dateOfIncorporation = companyProfile._3,
        address_line_1 = companyAddress.address_line_1.getOrElse(""),
        address_line_2 = companyAddress.address_line_2.getOrElse(""),
        country = companyAddress.country.getOrElse(""),
        locality = companyAddress.locality.getOrElse(""),
        postal_code = companyAddress.postal_code.getOrElse(""),
        region = companyAddress.region.getOrElse(""),
        sautr = responseReceived.sautr.getOrElse(""),
        identifiersMatch = responseReceived.identifiersMatch,
        businessVerification = responseReceived.businessVerification,
        registrationStatus = responseReceived.registration
      ).extendedDataEvent
    )
  }

  def auditGrsReturnNfmForLimitedCompany(
    responseReceived: IncorporatedEntityRegistrationData
  )(implicit hc:      HeaderCarrier): Future[AuditResult] =
    auditConnector.sendExtendedEvent(
      GrsReturnNfmAuditEvent(nfmRegistration =
        NfmRegistration(
          entityType = EntityType.UkLimitedCompany.toString,
          companyName = responseReceived.companyProfile.companyName,
          companyNumber = responseReceived.companyProfile.companyNumber,
          dateOfIncorporation = responseReceived.companyProfile.dateOfIncorporation.toString,
          address_line_1 = responseReceived.companyProfile.unsanitisedCHROAddress.address_line_1.getOrElse(""),
          address_line_2 = responseReceived.companyProfile.unsanitisedCHROAddress.address_line_2.getOrElse(""),
          country = responseReceived.companyProfile.unsanitisedCHROAddress.country.getOrElse(""),
          locality = responseReceived.companyProfile.unsanitisedCHROAddress.locality.getOrElse(""),
          postal_code = responseReceived.companyProfile.unsanitisedCHROAddress.postal_code.getOrElse(""),
          region = responseReceived.companyProfile.unsanitisedCHROAddress.region.getOrElse(""),
          utr = responseReceived.ctutr,
          identifiersMatch = responseReceived.identifiersMatch,
          businessVerification = responseReceived.businessVerification,
          registrationStatus = responseReceived.registration
        )
      ).extendedDataEvent
    )

  def auditGrsReturnNfmForLLP(
    responseReceived: PartnershipEntityRegistrationData
  )(implicit hc:      HeaderCarrier): Future[AuditResult] = {
    val emptyString = ""
    val companyProfile = responseReceived.companyProfile
      .map(profile => (profile.companyName, profile.companyNumber, profile.dateOfIncorporation.toString))
      .getOrElse(("", "", ""))
    val companyAddress = responseReceived.companyProfile
      .map(data => data.unsanitisedCHROAddress)
      .fold(
        IncorporatedEntityAddress(
          Some(emptyString),
          Some(emptyString),
          Some(emptyString),
          Some(emptyString),
          Some(emptyString),
          Some(emptyString),
          Some(emptyString),
          Some(emptyString)
        )
      )(address => address)
    auditConnector.sendExtendedEvent(
      GrsReturnNfmAuditEventForLlp(nfmRegistration =
        NfmRegistration(
          entityType = EntityType.LimitedLiabilityPartnership.toString,
          companyName = companyProfile._1,
          companyNumber = companyProfile._2,
          dateOfIncorporation = companyProfile._3,
          address_line_1 = companyAddress.address_line_1.getOrElse(""),
          address_line_2 = companyAddress.address_line_2.getOrElse(""),
          country = companyAddress.country.getOrElse(""),
          locality = companyAddress.locality.getOrElse(""),
          postal_code = companyAddress.postal_code.getOrElse(""),
          region = companyAddress.region.getOrElse(""),
          utr = responseReceived.sautr.getOrElse(""),
          identifiersMatch = responseReceived.identifiersMatch,
          businessVerification = responseReceived.businessVerification,
          registrationStatus = responseReceived.registration
        )
      ).extendedDataEvent
    )
  }

  def auditRepayments(repaymentAuditDetail: RepaymentsAuditEvent)(implicit hc: HeaderCarrier): Future[AuditResult] =
    auditConnector.sendExtendedEvent(
      RepaymentsAuditEvent(
        repaymentAuditDetail.refundAmount,
        repaymentAuditDetail.reasonForRequestingRefund,
        repaymentAuditDetail.ukOrAbroadBankAccount,
        repaymentAuditDetail.uKBankAccountDetails,
        repaymentAuditDetail.nonUKBank,
        repaymentAuditDetail.repaymentsContactName,
        repaymentAuditDetail.repaymentsContactEmail,
        repaymentAuditDetail.repaymentsContactByPhone,
        repaymentAuditDetail.repaymentsPhoneDetails
      ).extendedDataEvent
    )

  def auditReplaceFilingMember(nfmDetail: NewFilingMemberDetail)(implicit hc: HeaderCarrier): Future[AuditResult] =
    auditConnector.sendExtendedEvent(
      RfmAuditEvent(
        nfmDetail.plrReference,
        nfmDetail.securityAnswerRegistrationDate,
        nfmDetail.corporatePosition,
        nfmDetail.ukBased,
        nfmDetail.nameRegistration,
        nfmDetail.registeredAddress,
        nfmDetail.primaryContactName,
        nfmDetail.primaryContactEmail,
        nfmDetail.primaryContactPhonePreference,
        nfmDetail.primaryContactPhoneNumber,
        nfmDetail.addSecondaryContact,
        nfmDetail.secondaryContactInformation.map(scInfo => scInfo.name),
        nfmDetail.secondaryContactInformation.map(scInfo => scInfo.emailAddress),
        nfmDetail.secondaryContactInformation.map(scInfo => scInfo.phone.isDefined),
        nfmDetail.secondaryContactInformation.flatMap(scInfo => scInfo.phone.map(tel => tel)),
        nfmDetail.contactAddress
      ).extendedDataEvent
    )

  def sendEventBTN(auditEvent: AuditEvent)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    logger.info(s"Sending audit event: ${auditEvent.auditType}")
    auditConnector.sendExtendedEvent(auditEvent.extendedDataEvent)
  }

  def auditBTN(
    pillarReference:            String,
    accountingPeriod:           String,
    entitiesInsideAndOutsideUK: Boolean,
    apiResponseData:            ApiResponseData
  )(implicit hc:                HeaderCarrier): Future[AuditResult] =
    sendEventBTN(
      CreateBtnAuditEvent(
        pillarReference = pillarReference,
        accountingPeriod = accountingPeriod,
        entitiesInsideAndOutsideUK = entitiesInsideAndOutsideUK,
        apiResponseData = apiResponseData
      )
    )

}
