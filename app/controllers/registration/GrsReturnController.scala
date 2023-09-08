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
import models.grs.EntityType.{LimitedLiabilityPartnership, UkLimitedCompany}
import models.{Mode, NfmRegistrationConfirmation, UserType}
import models.grs.{BusinessVerificationResult, EntityType, GrsErrorCodes, GrsRegistrationResult}
import models.grs.RegistrationStatus.{Registered, RegistrationFailed}
import models.grs.VerificationStatus.Fail
import models.registration.GrsResponse
import pages.{NominatedFilingMemberPage, RegistrationPage}
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

  def continueUpe(mode: Mode, journeyId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers.get(RegistrationPage) match {
      case Some(registration) =>
        registration.orgType match {
          case Some(e @ UkLimitedCompany) =>
            for {
              entityRegData <- incorporatedEntityIdentificationFrontendConnector.getJourneyData(journeyId)
              isRegistrationStatus = if (entityRegData.registration.registrationStatus == Registered) RowStatus.Completed else RowStatus.InProgress
              userAnswers <- Future.fromTry(
                               request.userAnswers.set(
                                 RegistrationPage,
                                 registration.copy(
                                   isRegistrationStatus = isRegistrationStatus,
                                   withIdRegData = Some(GrsResponse(incorporatedEntityRegistrationData = Some(entityRegData))),
                                   safeId = entityRegData.registration.registeredBusinessPartnerId
                                 )
                               )
                             )
              - <- userAnswersConnectors.save(userAnswers.id, Json.toJson(userAnswers.data))
            } yield handleGrsAndBvResult(entityRegData.identifiersMatch, entityRegData.businessVerification, entityRegData.registration, e, mode)

          case Some(e @ LimitedLiabilityPartnership) =>
            for {
              entityRegData <- partnershipIdentificationFrontendConnector.getJourneyData(journeyId)
              isRegistrationStatus = if (entityRegData.registration.registrationStatus == Registered) RowStatus.Completed else RowStatus.InProgress
              userAnswers <- Future.fromTry(
                               request.userAnswers.set(
                                 RegistrationPage,
                                 registration.copy(
                                   isRegistrationStatus = isRegistrationStatus,
                                   withIdRegData = Some(GrsResponse(partnershipEntityRegistrationData = Some(entityRegData))),
                                   safeId = entityRegData.registration.registeredBusinessPartnerId
                                 )
                               )
                             )
              - <- userAnswersConnectors.save(userAnswers.id, Json.toJson(userAnswers.data))
            } yield handleGrsAndBvResult(entityRegData.identifiersMatch, entityRegData.businessVerification, entityRegData.registration, e, mode)

          case _ => throw new IllegalStateException("No valid org type found in registration data")
        }

      case _ => throw new IllegalStateException("No valid org type found in registration data")
    }

  }

  def continueFm(mode: Mode, journeyId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers.get(NominatedFilingMemberPage) match {
      case Some(nfmregistration) =>
        nfmregistration.orgType match {
          case Some(e @ UkLimitedCompany) =>
            for {
              entityRegData <- incorporatedEntityIdentificationFrontendConnector.getJourneyData(journeyId)
              isNfmStatus = if (entityRegData.registration.registrationStatus == Registered) RowStatus.Completed else RowStatus.InProgress
              userAnswers <- Future.fromTry(
                               request.userAnswers.set(
                                 NominatedFilingMemberPage,
                                 nfmregistration.copy(
                                   isNFMnStatus = isNfmStatus,
                                   withIdRegData = Some(GrsResponse(incorporatedEntityRegistrationData = Some(entityRegData))),
                                   safeId = entityRegData.registration.registeredBusinessPartnerId
                                 )
                               )
                             )
              - <- userAnswersConnectors.save(userAnswers.id, Json.toJson(userAnswers.data))
            } yield handleGrsAndBvResult(entityRegData.identifiersMatch, entityRegData.businessVerification, entityRegData.registration, e, mode)

          case Some(e @ LimitedLiabilityPartnership) =>
            for {
              entityRegData <- partnershipIdentificationFrontendConnector.getJourneyData(journeyId)
              isNfmStatus = if (entityRegData.registration.registrationStatus == Registered) RowStatus.Completed else RowStatus.InProgress
              userAnswers <- Future.fromTry(
                               request.userAnswers.set(
                                 NominatedFilingMemberPage,
                                 nfmregistration.copy(
                                   isNFMnStatus = isNfmStatus,
                                   withIdRegData = Some(GrsResponse(partnershipEntityRegistrationData = Some(entityRegData))),
                                   safeId = entityRegData.registration.registeredBusinessPartnerId
                                 )
                               )
                             )
              - <- userAnswersConnectors.save(userAnswers.id, Json.toJson(userAnswers.data))
            } yield handleGrsAndBvResult(entityRegData.identifiersMatch, entityRegData.businessVerification, entityRegData.registration, e, mode)

          case _ => throw new IllegalStateException("No valid org type found in Filing Member Registration data")
        }

      case _ => throw new IllegalStateException("No valid org type found in Filing Member Registration data")
    }

  }

  private def handleGrsAndBvResult(
    identifiersMatch: Boolean,
    bvResult:         Option[BusinessVerificationResult],
    grsResult:        GrsRegistrationResult,
    orgType:          EntityType,
    mode:             Mode
  )(implicit hc:      HeaderCarrier): Result =
    (identifiersMatch, bvResult, grsResult.registrationStatus, grsResult.registeredBusinessPartnerId) match {
      case (false, _, _, _) =>
        Redirect(controllers.routes.UnderConstructionController.onPageLoad)
      case (_, Some(BusinessVerificationResult(Fail)), _, _) =>
        Redirect(controllers.routes.UnderConstructionController.onPageLoad)
      case (_, _, _, Some(businessPartnerId)) =>
        Redirect(controllers.routes.TaskListController.onPageLoad)
      case (true, _, Registered, Some(businessPartnerId)) =>
        Redirect(controllers.routes.TaskListController.onPageLoad)
      case (_, _, RegistrationFailed, _) =>
        grsResult.failures match {
          case Some(failures) if failures.exists(_.code == GrsErrorCodes.PartyTypeMismatch) =>
            Redirect(controllers.routes.UnderConstructionController.onPageLoad)
          case _ =>
            Redirect(controllers.routes.TaskListController.onPageLoad)
        }
      case _ =>
        throw new IllegalStateException(
          s"Invalid result received from GRS: identifiersMatch: $identifiersMatch, registration: $grsResult, businessVerification: $bvResult"
        )
    }
}
