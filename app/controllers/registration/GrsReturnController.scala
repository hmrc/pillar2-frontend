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

import connectors.{IncorporatedEntityIdentificationFrontendConnector, PartnershipIdentificationFrontendConnector, UserAnswersConnectors}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.Mode
import models.fm.JourneyType
import models.grs.RegistrationStatus.{Registered, RegistrationFailed}
import models.grs.VerificationStatus.Fail
import models.grs.{BusinessVerificationResult, EntityType, GrsErrorCodes, GrsRegistrationResult}
import models.registration.{GrsResponse, RegistrationInfo}
import pages._
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GrsReturnController @Inject() (
  val userAnswersConnectors:                         UserAnswersConnectors,
  identify:                                          IdentifierAction,
  getData:                                           DataRetrievalAction,
  requireData:                                       DataRequiredAction,
  val controllerComponents:                          MessagesControllerComponents,
  incorporatedEntityIdentificationFrontendConnector: IncorporatedEntityIdentificationFrontendConnector,
  partnershipIdentificationFrontendConnector:        PartnershipIdentificationFrontendConnector
)(implicit ec:                                       ExecutionContext)
    extends FrontendBaseController {

  def continueUpe(journeyId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(upeEntityTypePage)
      .map {
        case EntityType.UkLimitedCompany =>
          for {
            entityRegData <- incorporatedEntityIdentificationFrontendConnector.getJourneyData(journeyId)
            safeId = entityRegData.registration.registeredBusinessPartnerId.getOrElse(throw new Exception("No safe id in UK Limited response"))
            registeredInfo       = RegistrationInfo(crn = entityRegData.companyProfile.companyNumber, utr = entityRegData.ctutr, safeId = safeId)
            isRegistrationStatus = if (entityRegData.registration.registrationStatus == Registered) RowStatus.Completed else RowStatus.InProgress
            userAnswers <- Future.fromTry(
                             request.userAnswers.set(upeGRSResponsePage, GrsResponse(incorporatedEntityRegistrationData = Some(entityRegData)))
                           )
            userAnswers2 <- Future.fromTry(userAnswers.set(GrsUpeStatusPage, isRegistrationStatus))
            userAnswers3 <- Future.fromTry(userAnswers2.set(UpeRegInformationPage, registeredInfo))
            -            <- userAnswersConnectors.save(userAnswers3.id, Json.toJson(userAnswers3.data))
          } yield handleGrsAndBvResult(
            entityRegData.identifiersMatch,
            entityRegData.businessVerification,
            entityRegData.registration,
            JourneyType.UltimateParent
          )

        case EntityType.LimitedLiabilityPartnership =>
          for {
            entityRegData <- partnershipIdentificationFrontendConnector.getJourneyData(journeyId)
            safeId = entityRegData.registration.registeredBusinessPartnerId.getOrElse(throw new Exception("No safe id in LLP response"))
            registeredInfo = RegistrationInfo(
                               crn = entityRegData.companyProfile match {
                                 case Some(v) => v.companyNumber
                                 case _       => throw new Exception("LLP response without company profile")
                               },
                               utr = entityRegData.sautr.getOrElse(throw new Exception("LLP response without Utr available")),
                               safeId = safeId
                             )
            isRegistrationStatus = if (entityRegData.registration.registrationStatus == Registered) RowStatus.Completed else RowStatus.InProgress
            userAnswers <- Future.fromTry(
                             request.userAnswers.set(upeGRSResponsePage, GrsResponse(partnershipEntityRegistrationData = Some(entityRegData)))
                           )
            userAnswers2 <- Future.fromTry(userAnswers.set(GrsUpeStatusPage, isRegistrationStatus))
            userAnswers3 <- Future.fromTry(userAnswers2.set(UpeRegInformationPage, registeredInfo))
            -            <- userAnswersConnectors.save(userAnswers3.id, Json.toJson(userAnswers3.data))
          } yield handleGrsAndBvResult(
            entityRegData.identifiersMatch,
            entityRegData.businessVerification,
            entityRegData.registration,
            JourneyType.UltimateParent
          )
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

  }
  def continueFm(journeyId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(fmEntityTypePage)
      .map {
        case EntityType.UkLimitedCompany =>
          for {
            entityRegData <- incorporatedEntityIdentificationFrontendConnector.getJourneyData(journeyId)
            isNfmStatus = if (entityRegData.registration.registrationStatus == Registered) RowStatus.Completed else RowStatus.InProgress
            safeID      = entityRegData.registration.registeredBusinessPartnerId.getOrElse(throw new Exception("No safe id in UK Limited response"))
            userAnswers <- Future.fromTry(
                             request.userAnswers.set(fmGRSResponsePage, GrsResponse(incorporatedEntityRegistrationData = Some(entityRegData)))
                           )
            userAnswers2 <- Future.fromTry(userAnswers.set(GrsFilingMemberStatusPage, isNfmStatus))
            userAnswers3 <- Future.fromTry(userAnswers2.set(FmSafeIDPage, safeID))
            -            <- userAnswersConnectors.save(userAnswers3.id, Json.toJson(userAnswers3.data))
          } yield handleGrsAndBvResult(
            entityRegData.identifiersMatch,
            entityRegData.businessVerification,
            entityRegData.registration,
            JourneyType.FilingMember
          )

        case EntityType.LimitedLiabilityPartnership =>
          for {
            entityRegData <- partnershipIdentificationFrontendConnector.getJourneyData(journeyId)
            isNfmStatus = if (entityRegData.registration.registrationStatus == Registered) RowStatus.Completed else RowStatus.InProgress
            safeID      = entityRegData.registration.registeredBusinessPartnerId.getOrElse(throw new Exception("No safe id in LLP response"))
            userAnswers <- Future.fromTry(
                             request.userAnswers.set(fmGRSResponsePage, GrsResponse(partnershipEntityRegistrationData = Some(entityRegData)))
                           )
            userAnswers2 <- Future.fromTry(userAnswers.set(GrsFilingMemberStatusPage, isNfmStatus))
            userAnswers3 <- Future.fromTry(userAnswers2.set(FmSafeIDPage, safeID))
            -            <- userAnswersConnectors.save(userAnswers3.id, Json.toJson(userAnswers3.data))
          } yield handleGrsAndBvResult(
            entityRegData.identifiersMatch,
            entityRegData.businessVerification,
            entityRegData.registration,
            JourneyType.FilingMember
          )
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

  }

  private def handleGrsAndBvResult(
    identifiersMatch: Boolean,
    bvResult:         Option[BusinessVerificationResult],
    grsResult:        GrsRegistrationResult,
    journeyType:      JourneyType
  )(implicit hc:      HeaderCarrier): Result =
    (identifiersMatch, bvResult, grsResult.registrationStatus, grsResult.registeredBusinessPartnerId) match {
      case (false, _, _, _) | (_, Some(BusinessVerificationResult(Fail)), _, _) if journeyType == JourneyType.FilingMember =>
        Redirect(controllers.routes.GrsRegistrationNotCalledController.onPageLoadNfm)
      case (false, _, _, _) | (_, Some(BusinessVerificationResult(Fail)), _, _) if journeyType == JourneyType.UltimateParent =>
        Redirect(controllers.routes.GrsRegistrationNotCalledController.onPageLoadUpe)
      case (true, _, _, Some(_)) =>
        Redirect(controllers.routes.TaskListController.onPageLoad)
      case (_, _, RegistrationFailed, _) =>
        (grsResult.failures, journeyType) match {
          case (_, JourneyType.FilingMember) =>
            Redirect(controllers.routes.GrsRegistrationFailedController.onPageLoadNfm)
          case (_, JourneyType.UltimateParent) =>
            Redirect(controllers.routes.GrsRegistrationFailedController.onPageLoadUpe)
        }
      case _ =>
        throw new IllegalStateException(
          s"Invalid result received from GRS: identifiersMatch: $identifiersMatch, registration: $grsResult, businessVerification: $bvResult"
        )
    }
}
