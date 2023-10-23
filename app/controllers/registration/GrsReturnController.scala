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
import models.grs.RegistrationStatus.{Registered, RegistrationFailed}
import models.grs.VerificationStatus.Fail
import models.grs.{BusinessVerificationResult, EntityType, GrsErrorCodes, GrsRegistrationResult}
import models.registration.GrsResponse
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

  def continueUpe(mode: Mode, journeyId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(upeEntityTypePage)
      .map {
        case EntityType.UkLimitedCompany =>
          for {
            entityRegData <- incorporatedEntityIdentificationFrontendConnector.getJourneyData(journeyId)
            isRegistrationStatus = if (entityRegData.registration.registrationStatus == Registered) RowStatus.Completed else RowStatus.InProgress
            userAnswers <- Future.fromTry(
                             request.userAnswers.set(upeGRSResponsePage, GrsResponse(incorporatedEntityRegistrationData = Some(entityRegData)))
                           )
            userAnswers2 <- Future.fromTry(userAnswers.set(GrsUpStatusPage, isRegistrationStatus))
            -            <- userAnswersConnectors.save(userAnswers2.id, Json.toJson(userAnswers2.data))
          } yield handleGrsAndBvResult(entityRegData.identifiersMatch, entityRegData.businessVerification, entityRegData.registration, mode)

        case EntityType.LimitedLiabilityPartnership =>
          for {
            entityRegData <- partnershipIdentificationFrontendConnector.getJourneyData(journeyId)
            isRegistrationStatus = if (entityRegData.registration.registrationStatus == Registered) RowStatus.Completed else RowStatus.InProgress
            userAnswers <- Future.fromTry(
                             request.userAnswers.set(upeGRSResponsePage, GrsResponse(partnershipEntityRegistrationData = Some(entityRegData)))
                           )
            userAnswers2 <- Future.fromTry(userAnswers.set(GrsUpStatusPage, isRegistrationStatus))
            -            <- userAnswersConnectors.save(userAnswers2.id, Json.toJson(userAnswers2.data))
          } yield handleGrsAndBvResult(entityRegData.identifiersMatch, entityRegData.businessVerification, entityRegData.registration, mode)
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

  }

  def continueFm(mode: Mode, journeyId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(fmEntityTypePage)
      .map {
        case EntityType.UkLimitedCompany =>
          for {
            entityRegData <- incorporatedEntityIdentificationFrontendConnector.getJourneyData(journeyId)
            isNfmStatus = if (entityRegData.registration.registrationStatus == Registered) RowStatus.Completed else RowStatus.InProgress
            userAnswers <- Future.fromTry(
                             request.userAnswers.set(fmGRSResponsePage, GrsResponse(incorporatedEntityRegistrationData = Some(entityRegData)))
                           )
            userAnswers2 <- Future.fromTry(userAnswers.set(GrsFilingMemberStatusPage, isNfmStatus))
            -            <- userAnswersConnectors.save(userAnswers2.id, Json.toJson(userAnswers2.data))
          } yield handleGrsAndBvResult(entityRegData.identifiersMatch, entityRegData.businessVerification, entityRegData.registration, mode)

        case EntityType.LimitedLiabilityPartnership =>
          for {
            entityRegData <- partnershipIdentificationFrontendConnector.getJourneyData(journeyId)
            isNfmStatus = if (entityRegData.registration.registrationStatus == Registered) RowStatus.Completed else RowStatus.InProgress
            userAnswers <- Future.fromTry(
                             request.userAnswers.set(fmGRSResponsePage, GrsResponse(partnershipEntityRegistrationData = Some(entityRegData)))
                           )
            userAnswers2 <- Future.fromTry(userAnswers.set(GrsFilingMemberStatusPage, isNfmStatus))
            -            <- userAnswersConnectors.save(userAnswers2.id, Json.toJson(userAnswers2.data))
          } yield handleGrsAndBvResult(entityRegData.identifiersMatch, entityRegData.businessVerification, entityRegData.registration, mode)
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

  }

  private def handleGrsAndBvResult(
    identifiersMatch: Boolean,
    bvResult:         Option[BusinessVerificationResult],
    grsResult:        GrsRegistrationResult,
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
