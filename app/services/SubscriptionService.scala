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

package services

import akka.Done
import connectors.{EnrolmentConnector, EnrolmentStoreProxyConnector, RegistrationConnector, SubscriptionConnector, UserAnswersConnectors}
import models.rfm.CorporatePosition
import models.subscription._
import models.{DuplicateSubmissionError, InternalIssueError, MneOrDomestic, UserAnswers}
import pages._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionService @Inject() (
  registrationConnector:        RegistrationConnector,
  subscriptionConnector:        SubscriptionConnector,
  userAnswersConnectors:          UserAnswersConnectors,
  enrolmentConnector:           EnrolmentConnector,
  enrolmentStoreProxyConnector: EnrolmentStoreProxyConnector
)(implicit ec:                  ExecutionContext) {

  def createSubscription(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[String] =
    for {
      upeSafeId       <- registerUpe(userAnswers)
      fmnSafeId       <- registerNfm(userAnswers)
      plrRef          <- subscriptionConnector.subscribe(SubscriptionRequestParameters(userAnswers.id, upeSafeId, fmnSafeId))
      enrolmentExists <- enrolmentStoreProxyConnector.enrolmentExists(plrRef)
      _               <- if (!enrolmentExists) Future.unit else Future.failed(DuplicateSubmissionError)
      enrolmentInfo = userAnswers.createEnrolmentInfo(plrRef)
      _ <- enrolmentConnector.createEnrolment(enrolmentInfo)
    } yield plrRef

  def readAndCacheSubscription(parameters: ReadSubscriptionRequestParameters)(implicit hc: HeaderCarrier): Future[SubscriptionData] =
    subscriptionConnector.readSubscriptionAndCache(parameters).flatMap {
      case Some(readSubscriptionResponse) =>
        Future.successful(readSubscriptionResponse)
      case _ =>
        Future.failed(InternalIssueError)
    }

  def readSubscription(plrReference: String)(implicit hc: HeaderCarrier): Future[SubscriptionData] =
    subscriptionConnector.readSubscription(plrReference).flatMap {
      case Some(readSubscriptionResponse) =>
        Future.successful(readSubscriptionResponse)
      case _ =>
        Future.failed(InternalIssueError)
    }

  def amendContactOrGroupDetails(userId: String, plrReference: String, subscriptionLocalData: SubscriptionLocalData)(implicit
                                                                                                                     hc:                         HeaderCarrier
  ): Future[Done] =
    for {
      currentSubscriptionData <- readSubscription(plrReference)
      amendData = amendGroupOrContactDetails(plrReference, currentSubscriptionData, subscriptionLocalData)
      result <- subscriptionConnector.amendSubscription(userId, amendData)
    } yield result

  def amendFilingMemberDetails(userId: String, amendData: AmendSubscription)(implicit hc:HeaderCarrier): Future[Done] ={
    for {
      result <- subscriptionConnector.amendSubscription(userId, amendData)
      _ <- userAnswersConnectors.remove(userId)
    } yield result
  }

  private def registerUpe(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[String] =
    userAnswers.getUpeSafeID
      .map(Future.successful)
      .getOrElse(registrationConnector.registerUltimateParent(userAnswers.id))

  private def registerNfm(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Option[String]] =
    userAnswers.getFmSafeID
      .map(safeId => Future.successful(Some(safeId)))
      .getOrElse {
        if (userAnswers.get(NominateFilingMemberPage).contains(true)) {
          registrationConnector.registerFilingMember(userAnswers.id).map(Some(_))
        } else {
          Future.successful(None)
        }
      }

  def amendGroupOrContactDetails(
    plrReference: String,
    currentData:  SubscriptionData,
    userData:     SubscriptionLocalData
  ): AmendSubscription = {
    val address = UpeCorrespAddressDetails(
      addressLine1 = userData.subRegisteredAddress.addressLine1,
      addressLine2 = userData.subRegisteredAddress.addressLine2,
      addressLine3 = Some(userData.subRegisteredAddress.addressLine3),
      addressLine4 = userData.subRegisteredAddress.addressLine4,
      postCode = userData.subRegisteredAddress.postalCode,
      countryCode = userData.subRegisteredAddress.countryCode
    )
    AmendSubscription(
      upeDetails = UpeDetailsAmend(
        plrReference,
        customerIdentification1 = currentData.upeDetails.customerIdentification1,
        customerIdentification2 = currentData.upeDetails.customerIdentification2,
        organisationName = currentData.upeDetails.organisationName,
        registrationDate = currentData.upeDetails.registrationDate,
        domesticOnly = if (userData.subMneOrDomestic == MneOrDomestic.Uk) true else false,
        filingMember = currentData.upeDetails.filingMember
      ),
      accountingPeriod = AccountingPeriodAmend(startDate = userData.subAccountingPeriod.startDate, endDate = userData.subAccountingPeriod.endDate),
      upeCorrespAddressDetails = address,
      primaryContactDetails = ContactDetailsType(
        name = userData.subPrimaryContactName,
        telephone = userData.subPrimaryCapturePhone,
        emailAddress = userData.subPrimaryEmail
      ),
      secondaryContactDetails = if (userData.subAddSecondaryContact) {
        for {
          name  <- userData.subSecondaryContactName
          email <- userData.subSecondaryEmail
        } yield ContactDetailsType(name = name, telephone = userData.subSecondaryCapturePhone, emailAddress = email)
      } else {
        None
      },
      filingMemberDetails = currentData.filingMemberDetails.map(details =>
        FilingMemberAmendDetails(
          safeId = details.safeId,
          customerIdentification1 = details.customerIdentification1,
          customerIdentification2 = details.customerIdentification2,
          organisationName = details.organisationName
        )
      )
    )
  }

   def setUltimateParentAsNewFilingMember(requiredInfo: NewFilingMemberDetails, subscriptionData: SubscriptionData, userAnswers: UserAnswers):
  AmendSubscription = {
     AmendSubscription(
      upeDetails = UpeDetailsAmend(
        plrReference = requiredInfo.plrReference,
        customerIdentification1 = subscriptionData.upeDetails.customerIdentification1,
        customerIdentification2 = subscriptionData.upeDetails.customerIdentification2,
        organisationName = subscriptionData.upeDetails.organisationName,
        registrationDate = subscriptionData.upeDetails.registrationDate,
        domesticOnly = subscriptionData.upeDetails.domesticOnly,
        filingMember = true
      ),
      accountingPeriod =
        AccountingPeriodAmend(startDate = subscriptionData.accountingPeriod.startDate, endDate = subscriptionData.accountingPeriod.endDate),
      upeCorrespAddressDetails = UpeCorrespAddressDetails(
        requiredInfo.address.addressLine1,
        requiredInfo.address.addressLine2,
        Some(requiredInfo.address.addressLine3),
        requiredInfo.address.addressLine4,
        requiredInfo.address.postalCode,
        requiredInfo.address.countryCode
      ),
      primaryContactDetails =
        ContactDetailsType(name = requiredInfo.companyName,
          telephone = userAnswers.get(RfmCapturePrimaryTelephonePage),
          emailAddress = requiredInfo.contactEmail),
      secondaryContactDetails = userAnswers.getSecondaryContact,
      filingMemberDetails = None
    )
  }

   def replaceOldFilingMember(safeId:String, requiredInfo: NewFilingMemberDetails, subscriptionData: SubscriptionData, userAnswers: UserAnswers)(implicit
    hc:                                            HeaderCarrier
  ): AmendSubscription = {
  AmendSubscription(
      upeDetails = UpeDetailsAmend(
        plrReference = requiredInfo.plrReference,
        customerIdentification1 = subscriptionData.upeDetails.customerIdentification1,
        customerIdentification2 = subscriptionData.upeDetails.customerIdentification2,
        organisationName = subscriptionData.upeDetails.organisationName,
        registrationDate = subscriptionData.upeDetails.registrationDate,
        domesticOnly = subscriptionData.upeDetails.domesticOnly,
        filingMember = false
      ),
      accountingPeriod =
        AccountingPeriodAmend(startDate = subscriptionData.accountingPeriod.startDate, endDate = subscriptionData.accountingPeriod.endDate),
      upeCorrespAddressDetails = UpeCorrespAddressDetails(
        requiredInfo.address.addressLine1,
        requiredInfo.address.addressLine2,
        Some(requiredInfo.address.addressLine3),
        requiredInfo.address.addressLine4,
        requiredInfo.address.postalCode,
        requiredInfo.address.countryCode
      ),
      primaryContactDetails =
        ContactDetailsType(name = requiredInfo.companyName,
          telephone = userAnswers.get(RfmSecondaryCapturePhonePage),
          emailAddress = requiredInfo.contactEmail),
      secondaryContactDetails = userAnswers.getSecondaryContact,
      filingMemberDetails =createNewFilingMemberDetails(safeId, userAnswers)

    )
  }

   def registerNewFilingMember(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[String] = {
    userAnswers.get(RfmSafeIDPage)
      .map(Future.successful)
      .getOrElse(registrationConnector.registerNewFilingMember(userAnswers.id))
  }
  private def createNewFilingMemberDetails(safeId: String , userAnswers: UserAnswers)(implicit hc: HeaderCarrier) = {
    userAnswers.get(RfmNameRegistrationPage).map{orgName=>
      FilingMemberAmendDetails(
        addNewFilingMember = true,
        safeId = safeId,
        customerIdentification1 = None,
        customerIdentification2 = None,
        organisationName = orgName
      )
    }
  }


}
