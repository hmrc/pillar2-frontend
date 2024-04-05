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

import cats.data.EitherT
import connectors.{EnrolmentConnector, EnrolmentStoreProxyConnector, RegistrationConnector, SubscriptionConnector}
import models.fm.JourneyType
import models.subscription.{AmendSubscription, ContactDetailsType, FilingMemberAmendDetails, ReadSubscriptionRequestParameters, ReadSubscriptionResponse, SubscriptionData, SubscriptionLocalData, SubscriptionRequestParameters, UpeDetailsAmend}
import models.{ApiError, DuplicateSubmissionError, InternalIssueError, MneOrDomestic, NotFoundError, UserAnswers}
import pages.NominateFilingMemberPage
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionService @Inject() (
  registrationConnector:        RegistrationConnector,
  subscriptionConnector:        SubscriptionConnector,
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

  def readSubscription(parameters: ReadSubscriptionRequestParameters)(implicit hc: HeaderCarrier): Future[ReadSubscriptionResponse] =
    subscriptionConnector.readSubscriptionAndCache(parameters).flatMap {
      case Some(readSubscriptionResponse) =>
        Future.successful(readSubscriptionResponse)
      case _ =>
        Future.failed(InternalIssueError)
    }

  def amendSubscription(userId: String, plrReference: String, subscriptionLocalData: SubscriptionLocalData)(implicit
    hc:                         HeaderCarrier
  ): Future[Either[ApiError, HttpResponse]] = { // TODO - tests
    for {
      // get etmp subscription data
      currentSubscriptionData <- EitherT.fromOptionF(subscriptionConnector.readSubscription(plrReference), NotFoundError)
      amendData = createAmendSubscription(plrReference, currentSubscriptionData, subscriptionLocalData)
      result <- EitherT(subscriptionConnector.amendSubscription(userId, amendData))
    } yield result
  }.value

  private def registerUpe(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[String] =
    userAnswers.getUpeSafeID
      .map(Future.successful)
      .getOrElse(registrationConnector.register(userAnswers.id, JourneyType.UltimateParent))

  private def registerNfm(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Option[String]] =
    userAnswers.getFmSafeID
      .map(safeId => Future.successful(Some(safeId)))
      .getOrElse {
        if (userAnswers.get(NominateFilingMemberPage).contains(true)) {
          registrationConnector.register(userAnswers.id, JourneyType.FilingMember).map(Some(_))
        } else {
          Future.successful(None)
        }
      }

  private def createAmendSubscription(
    plrReference: String,
    currentData:  SubscriptionData,
    userData:     SubscriptionLocalData
  ): AmendSubscription =
    AmendSubscription(
      upeDetails = UpeDetailsAmend(
        plrReference,
        customerIdentification1 = None,
        customerIdentification2 = None,
        organisationName = currentData.upeDetails.organisationName,
        registrationDate = currentData.upeDetails.registrationDate,
        domesticOnly = userData.subMneOrDomestic == MneOrDomestic.Uk,
        filingMember = false
      ),
      accountingPeriod = userData.subAccountingPeriod,
      upeCorrespAddressDetails = userData.subRegisteredAddress,
      primaryContactDetails = ContactDetailsType(
        name = userData.subPrimaryContactName,
        telephone = userData.subPrimaryCapturePhone,
        emailAddress = userData.subPrimaryEmail
      ),
      secondaryContactDetails = Option.when(userData.subAddSecondaryContact)( //TODO - maybe use Applicative here
        ContactDetailsType(
          userData.subSecondaryContactName.getOrElse(""),
          userData.subSecondaryCapturePhone,
          userData.subSecondaryEmail.getOrElse("")
        )
      ),
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
