/*
 * Copyright 2026 HM Revenue & Customs
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

import connectors.{IncorporatedEntityIdentificationFrontendConnector, PartnershipIdentificationFrontendConnector, UserAnswersConnectors}
import models.fm.JourneyType
import models.grs.*
import models.grs.RegistrationStatus.{Registered, RegistrationFailed}
import models.grs.VerificationStatus.Fail
import models.registration.{GrsResponse, RegistrationInfo}
import pages.*
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import services.audit.AuditService
import uk.gov.hmrc.http.HeaderCarrier
import utils.RowStatus

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GrsReturnService @Inject() (
  userAnswersConnectors:                             UserAnswersConnectors,
  incorporatedEntityIdentificationFrontendConnector: IncorporatedEntityIdentificationFrontendConnector,
  partnershipIdentificationFrontendConnector:        PartnershipIdentificationFrontendConnector,
  auditService:                                      AuditService
)(using ec: ExecutionContext)
    extends Logging {

  def continueUpe(journeyId: String, entityType: EntityType, userAnswers: models.UserAnswers)(using hc: HeaderCarrier): Future[Result] =
    entityType match {
      case EntityType.UkLimitedCompany            => upeLimited(userAnswers, journeyId)
      case EntityType.LimitedLiabilityPartnership => upePartnership(userAnswers, journeyId)
    }

  def continueFm(journeyId: String, entityType: EntityType, userAnswers: models.UserAnswers)(using hc: HeaderCarrier): Future[Result] =
    entityType match {
      case EntityType.UkLimitedCompany            => fmLimited(userAnswers, journeyId)
      case EntityType.LimitedLiabilityPartnership => fmPartnership(userAnswers, journeyId)
    }

  def continueRfm(journeyId: String, entityType: EntityType, userAnswers: models.UserAnswers)(using hc: HeaderCarrier): Future[Result] =
    entityType match {
      case EntityType.UkLimitedCompany            => rfmLimited(userAnswers, journeyId)
      case EntityType.LimitedLiabilityPartnership => rfmPartnership(userAnswers, journeyId)
    }

  private def upeLimited(userAnswers: models.UserAnswers, journeyId: String)(using hc: HeaderCarrier): Future[Result] =
    incorporatedEntityIdentificationFrontendConnector.getJourneyData(journeyId).flatMap { data =>
      auditService.auditGrsReturnForLimitedCompany(data)
      if data.registration.registrationStatus == Registered then {
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
              ua1 <- Future.fromTry(userAnswers.set(UpeGRSResponsePage, GrsResponse(incorporatedEntityRegistrationData = Some(data))))
              ua2 <- Future.fromTry(ua1.set(GrsUpeStatusPage, RowStatus.Completed))
              ua3 <- Future.fromTry(ua2.set(UpeRegInformationPage, registeredInfo))
              _   <- userAnswersConnectors.save(ua3.id, Json.toJson(ua3.data))
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

  private def upePartnership(userAnswers: models.UserAnswers, journeyId: String)(using hc: HeaderCarrier): Future[Result] =
    partnershipIdentificationFrontendConnector.getJourneyData(journeyId).flatMap { data =>
      auditService.auditGrsReturnForLLP(data)
      if data.registration.registrationStatus == Registered then {
        (for {
          safeId         <- data.registration.registeredBusinessPartnerId
          companyProfile <- data.companyProfile
          companyNumber = companyProfile.companyNumber
          utr <- data.sautr
        } yield {
          val registeredInfo = RegistrationInfo(crn = companyNumber, utr, safeId, registrationDate = None, filingMember = None)
          for {
            ua1 <- Future.fromTry(userAnswers.set(UpeGRSResponsePage, GrsResponse(partnershipEntityRegistrationData = Some(data))))
            ua2 <- Future.fromTry(ua1.set(GrsUpeStatusPage, RowStatus.Completed))
            ua3 <- Future.fromTry(ua2.set(UpeRegInformationPage, registeredInfo))
            _   <- userAnswersConnectors.save(ua3.id, Json.toJson(ua3.data))
          } yield handleGrsAndBvResult(
            data.identifiersMatch,
            data.businessVerification,
            data.registration,
            JourneyType.UltimateParent,
            journeyId,
            EntityType.LimitedLiabilityPartnership
          )
        }).getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
      } else {
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

  private def fmLimited(userAnswers: models.UserAnswers, journeyId: String)(using hc: HeaderCarrier): Future[Result] =
    incorporatedEntityIdentificationFrontendConnector.getJourneyData(journeyId).flatMap { data =>
      auditService.auditGrsReturnNfmForLimitedCompany(data)
      if data.registration.registrationStatus == Registered then {
        data.registration.registeredBusinessPartnerId
          .map { safeId =>
            for {
              ua1 <- Future.fromTry(userAnswers.set(FmGRSResponsePage, GrsResponse(incorporatedEntityRegistrationData = Some(data))))
              ua2 <- Future.fromTry(ua1.set(GrsFilingMemberStatusPage, RowStatus.Completed))
              ua3 <- Future.fromTry(ua2.set(FmSafeIDPage, safeId))
              _   <- userAnswersConnectors.save(ua3.id, Json.toJson(ua3.data))
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

  private def fmPartnership(userAnswers: models.UserAnswers, journeyId: String)(using hc: HeaderCarrier): Future[Result] =
    partnershipIdentificationFrontendConnector.getJourneyData(journeyId).flatMap { data =>
      auditService.auditGrsReturnNfmForLLP(data)
      if data.registration.registrationStatus == Registered then {
        data.registration.registeredBusinessPartnerId
          .map { safeId =>
            for {
              ua1 <- Future.fromTry(userAnswers.set(FmGRSResponsePage, GrsResponse(partnershipEntityRegistrationData = Some(data))))
              ua2 <- Future.fromTry(ua1.set(GrsFilingMemberStatusPage, RowStatus.Completed))
              ua3 <- Future.fromTry(ua2.set(FmSafeIDPage, safeId))
              _   <- userAnswersConnectors.save(ua3.id, Json.toJson(ua3.data))
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

  private def rfmLimited(userAnswers: models.UserAnswers, journeyId: String)(using hc: HeaderCarrier): Future[Result] =
    incorporatedEntityIdentificationFrontendConnector.getJourneyData(journeyId).flatMap { data =>
      if data.registration.registrationStatus == Registered then {
        data.registration.registeredBusinessPartnerId
          .map { safeId =>
            val grsData = GrsRegistrationData(
              companyId = safeId,
              companyName = data.companyProfile.companyName,
              utr = data.ctutr,
              crn = data.companyProfile.companyNumber
            )
            for {
              ua1 <- Future.fromTry(userAnswers.set(RfmGRSUkLimitedPage, data))
              ua2 <- Future.fromTry(ua1.set(RfmGrsDataPage, grsData))
              _   <- userAnswersConnectors.save(ua2.id, Json.toJson(ua2.data))
            } yield handleGrsAndBvResult(
              data.identifiersMatch,
              data.businessVerification,
              data.registration,
              JourneyType.ReplaceFilingMember,
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
            JourneyType.ReplaceFilingMember,
            journeyId,
            EntityType.UkLimitedCompany
          )
        )
      }
    }

  private def rfmPartnership(userAnswers: models.UserAnswers, journeyId: String)(using hc: HeaderCarrier): Future[Result] =
    partnershipIdentificationFrontendConnector
      .getJourneyData(journeyId)
      .flatMap { data =>
        auditService.auditGrsReturnNfmForLLP(data)
        if data.registration.registrationStatus == Registered then {
          val companyProfile = data.companyProfile.getOrElse(throw new Exception("no profile found for limited liability partnership company"))
          val sautr          = data.sautr.getOrElse(throw new Exception("no UTR found for limited liability partnership company"))
          data.registration.registeredBusinessPartnerId
            .map { safeId =>
              val grsData = GrsRegistrationData(
                companyId = safeId,
                companyName = companyProfile.companyName,
                utr = sautr,
                crn = companyProfile.companyNumber
              )
              for {
                ua1 <- Future.fromTry(userAnswers.set(RfmGRSUkPartnershipPage, data))
                ua2 <- Future.fromTry(ua1.set(RfmGrsDataPage, grsData))
                _   <- userAnswersConnectors.save(ua2.id, Json.toJson(ua2.data))
              } yield handleGrsAndBvResult(
                data.identifiersMatch,
                data.businessVerification,
                data.registration,
                JourneyType.ReplaceFilingMember,
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
              JourneyType.ReplaceFilingMember,
              journeyId,
              EntityType.LimitedLiabilityPartnership
            )
          )
        }
      }
      .recover { case e: Exception =>
        logger.error(s"exception thrown due to due absence of UTR or companyProfile with message ${e.getMessage}")
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }

  private def handleGrsAndBvResult(
    identifiersMatch: Boolean,
    bvResult:         Option[BusinessVerificationResult],
    grsResult:        GrsRegistrationResult,
    journeyType:      JourneyType,
    journeyId:        String,
    entityType:       EntityType
  ): Result =
    (identifiersMatch, bvResult, grsResult.registrationStatus, grsResult.registeredBusinessPartnerId) match {
      case (false, _, _, _) | (_, Some(BusinessVerificationResult(Fail)), _, _) if journeyType == JourneyType.FilingMember =>
        logger.info(
          s"Filing Member Business Verification failed for $entityType with journey ID $journeyId"
        )
        Redirect(controllers.routes.GrsRegistrationNotCalledController.onPageLoadNfm)
      case (false, _, _, _) | (_, Some(BusinessVerificationResult(Fail)), _, _) if journeyType == JourneyType.UltimateParent =>
        logger.info(
          s"Ultimate Parent Business Verification failed for $entityType with journey ID $journeyId"
        )
        Redirect(controllers.routes.GrsRegistrationNotCalledController.onPageLoadUpe)
      case (false, _, _, _) | (_, Some(BusinessVerificationResult(Fail)), _, _) if journeyType == JourneyType.ReplaceFilingMember =>
        logger.info(
          s"Replace Filing Member Business Verification failed for $entityType with journey ID $journeyId"
        )
        Redirect(controllers.routes.GrsRegistrationNotCalledController.onPageLoadRfm)
      case (true, _, _, Some(_)) if journeyType == JourneyType.ReplaceFilingMember =>
        logger.info(
          s"Registration successful for $entityType with journey ID $journeyId --redirecting to continue RFM"
        )
        Redirect(controllers.rfm.routes.RfmContactDetailsRegistrationController.onPageLoad())

      case (true, _, _, Some(_)) =>
        logger.info(
          s"Registration successful for $entityType with journey ID $journeyId --redirecting to task list"
        )
        Redirect(controllers.routes.TaskListController.onPageLoad)
      case (_, _, RegistrationFailed, _) =>
        logger.info(
          s"$journeyType registration failed for $entityType with journey ID $journeyId"
        )
        (grsResult.failures, journeyType) match {
          case (_, JourneyType.FilingMember) =>
            Redirect(controllers.routes.GrsRegistrationFailedController.onPageLoadNfm)
          case (_, JourneyType.UltimateParent) =>
            Redirect(controllers.routes.GrsRegistrationFailedController.onPageLoadUpe)
          case (_, JourneyType.ReplaceFilingMember) =>
            Redirect(controllers.routes.GrsRegistrationFailedController.onPageLoadRfm)
        }
      case _ =>
        throw new IllegalStateException(
          s"Invalid result received from GRS: identifiersMatch: $identifiersMatch, registration: $grsResult, businessVerification: $bvResult"
        )
    }
}
