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

package controllers.registration

import connectors.{IncorporatedEntityIdentificationFrontendConnector, PartnershipIdentificationFrontendConnector, UserAnswersConnectors}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.fm.JourneyType
import models.grs.RegistrationStatus.{Registered, RegistrationFailed}
import models.grs.VerificationStatus.Fail
import models.grs.{BusinessVerificationResult, EntityType, GrsRegistrationResult}
import models.registration.{GrsResponse, RegistrationInfo}
import models.requests.DataRequest
import pages._
import play.api.Logging
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
    extends FrontendBaseController
    with Logging {

  def continueUpe(journeyId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(upeEntityTypePage)
      .map {
        case EntityType.UkLimitedCompany            => upeLimited(request, journeyId)
        case EntityType.LimitedLiabilityPartnership => upePartnership(request, journeyId)
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.BookmarkPreventionController.onPageLoad)))

  }

  private def upeLimited(request: DataRequest[AnyContent], journeyId: String)(implicit hc: HeaderCarrier): Future[Result] =
    incorporatedEntityIdentificationFrontendConnector.getJourneyData(journeyId).flatMap { data =>
      if (data.registration.registrationStatus == Registered) {
        data.registration.registeredBusinessPartnerId
          .map { safeId =>
            val registeredInfo = RegistrationInfo(
              crn = data.companyProfile.companyNumber,
              utr = data.ctutr,
              safeId = safeId,
              registrationDate = None,
              filingMember = None
            )
            for {
              userAnswers <- Future.fromTry(request.userAnswers.set(upeGRSResponsePage, GrsResponse(incorporatedEntityRegistrationData = Some(data))))
              userAnswers2 <- Future.fromTry(userAnswers.set(GrsUpeStatusPage, RowStatus.Completed))
              userAnswers3 <- Future.fromTry(userAnswers2.set(UpeRegInformationPage, registeredInfo))
              -            <- userAnswersConnectors.save(userAnswers3.id, Json.toJson(userAnswers3.data))

            } yield handleGrsAndBvResult(
              data.identifiersMatch,
              data.businessVerification,
              data.registration,
              JourneyType.UltimateParent,
              journeyId,
              EntityType.UkLimitedCompany
            )
          }
          .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
      } else {
        Future.successful(
          handleGrsAndBvResult(
            data.identifiersMatch,
            data.businessVerification,
            data.registration,
            JourneyType.UltimateParent,
            journeyId,
            EntityType.UkLimitedCompany
          )
        )
      }
    }

  private def upePartnership(request: DataRequest[AnyContent], journeyId: String)(implicit hc: HeaderCarrier): Future[Result] =
    partnershipIdentificationFrontendConnector.getJourneyData(journeyId).flatMap { data =>
      if (data.registration.registrationStatus == Registered) {
        for {
          safeId         <- data.registration.registeredBusinessPartnerId
          companyProfile <- data.companyProfile
          companyNumber = companyProfile.companyNumber
          utr <- data.sautr
        } yield {
          val registeredInfo = RegistrationInfo(crn = companyNumber, utr, safeId, registrationDate = None, filingMember = None)
          for {
            userAnswers <- Future.fromTry(
                             request.userAnswers.set(upeGRSResponsePage, GrsResponse(partnershipEntityRegistrationData = Some(data)))
                           )
            userAnswers2 <- Future.fromTry(userAnswers.set(GrsUpeStatusPage, RowStatus.Completed))
            userAnswers3 <- Future.fromTry(userAnswers2.set(UpeRegInformationPage, registeredInfo))
            -            <- userAnswersConnectors.save(userAnswers3.id, Json.toJson(userAnswers3.data))
          } yield handleGrsAndBvResult(
            data.identifiersMatch,
            data.businessVerification,
            data.registration,
            JourneyType.UltimateParent,
            journeyId,
            EntityType.LimitedLiabilityPartnership
          )
        }
      }.getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
      else {
        Future.successful(
          handleGrsAndBvResult(
            data.identifiersMatch,
            data.businessVerification,
            data.registration,
            JourneyType.UltimateParent,
            journeyId,
            EntityType.LimitedLiabilityPartnership
          )
        )
      }
    }

  def continueFm(journeyId: String): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(fmEntityTypePage)
      .map {
        case EntityType.UkLimitedCompany =>
          incorporatedEntityIdentificationFrontendConnector.getJourneyData(journeyId).flatMap { data =>
            if (data.registration.registrationStatus == Registered) {
              data.registration.registeredBusinessPartnerId
                .map { safeId =>
                  for {
                    userAnswers <-
                      Future.fromTry(request.userAnswers.set(fmGRSResponsePage, GrsResponse(incorporatedEntityRegistrationData = Some(data))))
                    userAnswers2 <- Future.fromTry(userAnswers.set(GrsFilingMemberStatusPage, RowStatus.Completed))
                    userAnswers3 <- Future.fromTry(userAnswers2.set(FmSafeIDPage, safeId))
                    -            <- userAnswersConnectors.save(userAnswers3.id, Json.toJson(userAnswers3.data))

                  } yield handleGrsAndBvResult(
                    data.identifiersMatch,
                    data.businessVerification,
                    data.registration,
                    JourneyType.FilingMember,
                    journeyId,
                    EntityType.UkLimitedCompany
                  )
                }
                .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
            } else {
              Future.successful(
                handleGrsAndBvResult(
                  data.identifiersMatch,
                  data.businessVerification,
                  data.registration,
                  JourneyType.FilingMember,
                  journeyId,
                  EntityType.UkLimitedCompany
                )
              )
            }
          }
        case EntityType.LimitedLiabilityPartnership =>
          partnershipIdentificationFrontendConnector.getJourneyData(journeyId).flatMap { data =>
            if (data.registration.registrationStatus == Registered) {
              data.registration.registeredBusinessPartnerId
                .map { safeId =>
                  for {
                    userAnswers <-
                      Future.fromTry(request.userAnswers.set(fmGRSResponsePage, GrsResponse(partnershipEntityRegistrationData = Some(data))))
                    userAnswers2 <- Future.fromTry(userAnswers.set(GrsFilingMemberStatusPage, RowStatus.Completed))
                    userAnswers3 <- Future.fromTry(userAnswers2.set(FmSafeIDPage, safeId))
                    -            <- userAnswersConnectors.save(userAnswers3.id, Json.toJson(userAnswers3.data))

                  } yield handleGrsAndBvResult(
                    data.identifiersMatch,
                    data.businessVerification,
                    data.registration,
                    JourneyType.FilingMember,
                    journeyId,
                    EntityType.LimitedLiabilityPartnership
                  )
                }
                .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
            } else {
              Future.successful(
                handleGrsAndBvResult(
                  data.identifiersMatch,
                  data.businessVerification,
                  data.registration,
                  JourneyType.FilingMember,
                  journeyId,
                  EntityType.LimitedLiabilityPartnership
                )
              )
            }
          }
      }
      .getOrElse(Future.successful(Redirect(controllers.routes.BookmarkPreventionController.onPageLoad)))

  }

  private def handleGrsAndBvResult(
    identifiersMatch: Boolean,
    bvResult:         Option[BusinessVerificationResult],
    grsResult:        GrsRegistrationResult,
    journeyType:      JourneyType,
    journeyId:        String,
    entityType:       EntityType
  )(implicit hc: HeaderCarrier): Result =
    (identifiersMatch, bvResult, grsResult.registrationStatus, grsResult.registeredBusinessPartnerId) match {
      case (false, _, _, _) | (_, Some(BusinessVerificationResult(Fail)), _, _) if journeyType == JourneyType.FilingMember =>
        logger.info(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - " +
          s"Filing Member Business Verification failed for $entityType with journey ID $journeyId")
        Redirect(controllers.routes.GrsRegistrationNotCalledController.onPageLoadNfm)
      case (false, _, _, _) | (_, Some(BusinessVerificationResult(Fail)), _, _) if journeyType == JourneyType.UltimateParent =>
        logger.info(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - " +
          s"Ultimate Parent Business Verification failed for $entityType with journey ID $journeyId")
        Redirect(controllers.routes.GrsRegistrationNotCalledController.onPageLoadUpe)
      case (true, _, _, Some(_)) =>
        logger.info(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - " +
          s"Registration successful for $entityType with journey ID $journeyId --redirecting to task list")
        Redirect(controllers.routes.TaskListController.onPageLoad)
      case (_, _, RegistrationFailed, _) =>
        logger.info(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - " +
          s"$journeyType registration failed for $entityType with journey ID $journeyId")
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
