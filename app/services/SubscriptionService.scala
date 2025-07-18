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

import connectors._
import models.EnrolmentRequest.{AllocateEnrolmentParameters, KnownFacts, KnownFactsParameters}
import models._
import models.registration._
import models.rfm.CorporatePosition
import models.subscription._
import org.apache.pekko.Done
import pages._
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureConverter.FutureOps

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionService @Inject() (
  registrationConnector:        RegistrationConnector,
  subscriptionConnector:        SubscriptionConnector,
  userAnswersConnectors:        UserAnswersConnectors,
  enrolmentConnector:           TaxEnrolmentConnector,
  enrolmentStoreProxyConnector: EnrolmentStoreProxyConnector
)(implicit ec:                  ExecutionContext)
    extends Logging {

  def createSubscription(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[String] = {
    logger.info(s"createSubscription - userAnswers: - ${Json.toJson(userAnswers)}")
    for {
      upeSafeId     <- registerUpe(userAnswers)
      latestAnswers <- userAnswersConnectors.getUserAnswer(userAnswers.id)
      updatedAnswers = latestAnswers.getOrElse(userAnswers)
      nfmSafeId       <- registerNfm(updatedAnswers)
      _               <- if (upeSafeId != nfmSafeId.getOrElse("")) Future.unit else Future.failed(DuplicateSafeIdError)
      plrRef          <- subscriptionConnector.subscribe(SubscriptionRequestParameters(userAnswers.id, upeSafeId, nfmSafeId))
      enrolmentExists <- enrolmentExists(plrRef)
      _               <- if (!enrolmentExists) Future.unit else Future.failed(DuplicateSubmissionError)
      enrolmentInfo = updatedAnswers.createEnrolmentInfo(plrRef)
      _ <- enrolmentConnector.enrolAndActivate(enrolmentInfo)
    } yield plrRef
  }

  private def enrolmentExists(plrReference: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    enrolmentStoreProxyConnector.getGroupIds(plrReference).map {
      case Some(_) => true
      case _       => false
    }

  def readAndCacheSubscription(parameters: ReadSubscriptionRequestParameters)(implicit hc: HeaderCarrier): Future[SubscriptionData] =
    subscriptionConnector.readSubscriptionAndCache(parameters).flatMap {
      case Some(readSubscriptionResponse) =>
        Future.successful(readSubscriptionResponse)
      case _ =>
        Future.failed(InternalIssueError)
    }
  def maybeReadAndCacheSubscription(parameters: ReadSubscriptionRequestParameters)(implicit hc: HeaderCarrier): Future[Option[SubscriptionData]] =
    subscriptionConnector.readSubscriptionAndCache(parameters).flatMap {
      case Some(readSubscriptionResponse) =>
        Future.successful(Some(readSubscriptionResponse))
      case _ =>
        Future.successful(None)
    }

  def readSubscription(plrReference: String)(implicit hc: HeaderCarrier): Future[SubscriptionData] =
    subscriptionConnector.readSubscription(plrReference).flatMap {
      case Some(subData) => Future.successful(subData)
      case None          => Future.failed(NoResultFound)
    }

  def matchingPillar2Records(id: String, sessionPillar2Id: String, sessionRegistrationDate: LocalDate)(implicit
    hc:                          HeaderCarrier
  ): Future[Boolean] =
    userAnswersConnectors.getUserAnswer(id).map { maybeUserAnswers =>
      (for {
        backendPillar2Id        <- maybeUserAnswers.flatMap(_.get(RfmPillar2ReferencePage))
        backendRegistrationDate <- maybeUserAnswers.flatMap(_.get(RfmRegistrationDatePage))
      } yield backendPillar2Id.equals(sessionPillar2Id) & backendRegistrationDate
        .isEqual(sessionRegistrationDate)).getOrElse(false)
    }
  def amendContactOrGroupDetails(userId: String, plrReference: String, subscriptionLocalData: SubscriptionLocalData)(implicit
    hc:                                  HeaderCarrier
  ): Future[Done] =
    for {
      currentSubscriptionData <- readSubscription(plrReference)
      amendData = amendGroupOrContactDetails(plrReference, currentSubscriptionData, subscriptionLocalData)
      result <- subscriptionConnector.amendSubscription(userId, amendData)
    } yield result
  private def registerUpe(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[String] =
    userAnswers.getUpeSafeID
      .map(Future.successful)
      .getOrElse {
        for {
          safeId         <- registrationConnector.registerUltimateParent(userAnswers.id)
          updatedAnswers <- Future.fromTry(userAnswers.set(UpeNonUKSafeIDPage, safeId))
          _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
        } yield safeId
      }

  private def registerNfm(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Option[String]] =
    userAnswers.getFmSafeID
      .map(safeId => Future.successful(Some(safeId)))
      .getOrElse {
        if (userAnswers.get(NominateFilingMemberPage).contains(true)) {
          for {
            safeId         <- registrationConnector.registerFilingMember(userAnswers.id)
            updatedAnswers <- Future.fromTry(userAnswers.set(FmNonUKSafeIDPage, safeId))
            _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
          } yield Some(safeId)
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
      replaceFilingMember = false,
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

  private def setUltimateParentAsNewFilingMember(
    requiredInfo:     NewFilingMemberDetail,
    subscriptionData: SubscriptionData
  ): AmendSubscription =
    AmendSubscription(
      replaceFilingMember = true,
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
        requiredInfo.contactAddress.addressLine1,
        requiredInfo.contactAddress.addressLine2,
        Some(requiredInfo.contactAddress.addressLine3),
        requiredInfo.contactAddress.addressLine4,
        requiredInfo.contactAddress.postalCode,
        requiredInfo.contactAddress.countryCode
      ),
      primaryContactDetails = ContactDetailsType(
        name = requiredInfo.primaryContactName,
        telephone = requiredInfo.primaryContactPhoneNumber,
        emailAddress = requiredInfo.primaryContactEmail
      ),
      secondaryContactDetails = requiredInfo.secondaryContactInformation,
      filingMemberDetails = None
    )

  private def replaceOldFilingMember(
    requiredInfo:     NewFilingMemberDetail,
    subscriptionData: SubscriptionData,
    filingMember:     FilingMemberAmendDetails
  ): AmendSubscription =
    AmendSubscription(
      replaceFilingMember = true,
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
        requiredInfo.contactAddress.addressLine1,
        requiredInfo.contactAddress.addressLine2,
        Some(requiredInfo.contactAddress.addressLine3),
        requiredInfo.contactAddress.addressLine4,
        requiredInfo.contactAddress.postalCode,
        requiredInfo.contactAddress.countryCode
      ),
      primaryContactDetails = ContactDetailsType(
        name = requiredInfo.primaryContactName,
        telephone = requiredInfo.primaryContactPhoneNumber,
        emailAddress = requiredInfo.primaryContactEmail
      ),
      secondaryContactDetails = requiredInfo.secondaryContactInformation,
      filingMemberDetails = Some(filingMember)
    )
  def amendFilingMemberDetails(userId: String, amendData: AmendSubscription)(implicit hc: HeaderCarrier): Future[Done] =
    for {
      result <- subscriptionConnector.amendSubscription(userId, amendData)
    } yield result

  private def registerOrGetNewFilingMemberSafeId(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[String] =
    for {
      safeID         <- userAnswers.get(RfmSafeIdPage).map(Future.successful).getOrElse(registrationConnector.registerNewFilingMember(userAnswers.id))
      updatedAnswers <- Future.fromTry(userAnswers.set(RfmSafeIdPage, safeID))
      _              <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
    } yield safeID

  def deallocateEnrolment(plrReference: String)(implicit hc: HeaderCarrier): Future[Done] =
    enrolmentStoreProxyConnector.getGroupIds(plrReference).flatMap {
      case Some(groupIds) =>
        logger.info(s"deallocateEnrolment groupIds: - $groupIds")
        enrolmentConnector.revokeEnrolment(groupId = groupIds.principalGroupIds.head, plrReference = plrReference)
      case error =>
        logger.warn(s"deallocateEnrolment error: - $error")
        Future.failed(InternalIssueError)
    }

  def allocateEnrolment(groupId: String, plrReference: String, enrolmentInfo: AllocateEnrolmentParameters)(implicit hc: HeaderCarrier): Future[Done] =
    enrolmentConnector.allocateEnrolment(groupId, plrReference, enrolmentInfo)

  def getUltimateParentEnrolmentInformation(subscriptionData: SubscriptionData, pillar2Reference: String, userId: String)(implicit
    hc:                                                       HeaderCarrier
  ): Future[AllocateEnrolmentParameters] =
    subscriptionData.upeDetails.customerIdentification1
      .flatMap { crn =>
        logger.info(s"getUltimateParentEnrolmentInformation crn: - $crn")
        subscriptionData.upeDetails.customerIdentification2
          .map { utr =>
            logger.info(s"getUltimateParentEnrolmentInformation utr: - $utr")
            val enrolementParameter =
              AllocateEnrolmentParameters(userId = userId, verifiers = Seq(Verifier(UTR.toString, utr), Verifier(CRN.toString, crn)))
            logger.info(s"getUltimateParentEnrolmentInformation WithId enrolment parameter - ${Json.toJson(enrolementParameter)}")
            enrolementParameter
          }
          .map(Future.successful)
      }
      .getOrElse(
        enrolmentStoreProxyConnector
          .getKnownFacts(KnownFactsParameters(knownFacts = Seq(KnownFacts(Pillar2Identifier.toString, pillar2Reference))))
          .map { knownFacts =>
            logger.info(s"getUltimateParentEnrolmentInformation knownFacts: - $knownFacts")
            val enrolementParameter = AllocateEnrolmentParameters(userId = userId, verifiers = knownFacts.enrolments.flatMap(_.verifiers))
            logger.info(s"getUltimateParentEnrolmentInformation WithoutId enrolment parameter - ${Json.toJson(enrolementParameter)}")
            enrolementParameter

          }
      )

  private def getNewFilingMemberDetails(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[FilingMemberAmendDetails] =
    userAnswers
      .get(RfmUkBasedPage)
      .flatMap(ukBased =>
        if (ukBased) {
          userAnswers
            .get(RfmGrsDataPage)
            .map(grsData =>
              FilingMemberAmendDetails(
                addNewFilingMember = true,
                safeId = grsData.companyId,
                customerIdentification1 = Some(grsData.crn),
                customerIdentification2 = Some(grsData.utr),
                organisationName = grsData.companyName
              ).toFuture
            )
        } else {
          userAnswers
            .get(RfmNameRegistrationPage)
            .map(companyName =>
              registerOrGetNewFilingMemberSafeId(userAnswers).map(companyId =>
                FilingMemberAmendDetails(
                  addNewFilingMember = true,
                  safeId = companyId,
                  customerIdentification1 = None,
                  customerIdentification2 = None,
                  organisationName = companyName
                )
              )
            )
        }
      )
      .getOrElse(throw new Exception("New Filing member details expected but could not find a value for RfmUkBased page"))

  def createAmendObjectForReplacingFilingMember(
    subscriptionData:   SubscriptionData,
    filingMemberDetail: NewFilingMemberDetail,
    userAnswers:        UserAnswers
  )(implicit hc:        HeaderCarrier): Future[AmendSubscription] =
    if (filingMemberDetail.corporatePosition == CorporatePosition.Upe) {
      logger.info("createAmendObjectForReplacingFilingMember - call setUltimateParentAsNewFilingMember")
      setUltimateParentAsNewFilingMember(requiredInfo = filingMemberDetail, subscriptionData = subscriptionData).toFuture
    } else {
      logger.info("createAmendObjectForReplacingFilingMember - call replaceOldFilingMember")
      getNewFilingMemberDetails(userAnswers).map(newFilingMember =>
        replaceOldFilingMember(
          requiredInfo = filingMemberDetail,
          subscriptionData = subscriptionData,
          filingMember = newFilingMember
        )
      )
    }

  def getCompanyNameFromGRS(grsResponse: GrsResponse): Option[String] =
    grsResponse.partnershipEntityRegistrationData
      .flatMap(_.companyProfile.map(_.companyName))
      .orElse(grsResponse.incorporatedEntityRegistrationData.map(_.companyProfile.companyName))

  def getCompanyName(userAnswers: UserAnswers): Either[Result, String] =
    userAnswers
      .get(FmNameRegistrationPage)
      .orElse(userAnswers.get(FmGRSResponsePage).flatMap(getCompanyNameFromGRS))
      .orElse(userAnswers.get(UpeNameRegistrationPage))
      .orElse(userAnswers.get(UpeGRSResponsePage).flatMap(getCompanyNameFromGRS)) match {
      case Some(companyName) => Right(companyName)
      case None              => Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }

}
