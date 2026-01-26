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

import models.UserAnswers
import models.audit.{ApiResponseData, ApiResponseFailure}
import models.btn.{BTNRequest, BTNStatus}
import models.subscription.AccountingPeriod
import pages.{BtnConfirmationPage, EntitiesInsideOutsideUKPage}
import play.api.Logging
import repositories.SessionRepository
import services.audit.AuditService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, ZonedDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BtnSubmissionService @Inject() (
  btnService:        BTNService,
  sessionRepository: SessionRepository,
  auditService:      AuditService
)(using ec: ExecutionContext)
    extends Logging {

  def startSubmission(
    userId:            String,
    userAnswers:       UserAnswers,
    pillar2Id:         String,
    accountingPeriod:  AccountingPeriod,
    btnPayload:        BTNRequest
  )(using hc: HeaderCarrier, clock: Clock): Future[Unit] = {
    val setProcessingF: Future[Unit] = for {
      updatedAnswers <- Future.fromTry(userAnswers.set(BTNStatus, BTNStatus.processing))
      _              <- sessionRepository.set(updatedAnswers)
    } yield ()

    setProcessingF.foreach { _ =>
      submitInBackground(
        userId = userId,
        originalAnswers = userAnswers,
        pillar2Id = pillar2Id,
        accountingPeriod = accountingPeriod,
        btnPayload = btnPayload
      )
    }

    setProcessingF
  }

  private def submitInBackground(
    userId:           String,
    originalAnswers:  UserAnswers,
    pillar2Id:        String,
    accountingPeriod: AccountingPeriod,
    btnPayload:       BTNRequest
  )(using hc: HeaderCarrier, clock: Clock): Unit = {
    given String = pillar2Id

    btnService
      .submitBTN(btnPayload)
      .flatMap { resp =>
        sessionRepository.get(userId).flatMap {
          case Some(latest) =>
            resp.result match {
              case Right(_) =>
                for {
                  submittedAnswers <- Future.fromTry {
                                        latest
                                          .set(BTNStatus, BTNStatus.submitted)
                                          .flatMap(_.set(BtnConfirmationPage, ZonedDateTime.now()))
                                      }
                  _ <- sessionRepository.set(submittedAnswers)
                  _ <- auditService.auditBTNSubmission(
                         pillarReference = pillar2Id,
                         accountingPeriod = accountingPeriod,
                         entitiesInsideAndOutsideUK = originalAnswers.get(EntitiesInsideOutsideUKPage).getOrElse(false),
                         response = ApiResponseData.fromBtnResponse(resp)(using clock)
                       )
                } yield ()
              case Left(_) =>
                for {
                  errorAnswers <- Future.fromTry(latest.set(BTNStatus, BTNStatus.error))
                  _            <- sessionRepository.set(errorAnswers)
                  _ <- auditService.auditBTNSubmission(
                         pillarReference = pillar2Id,
                         accountingPeriod = accountingPeriod,
                         entitiesInsideAndOutsideUK = originalAnswers.get(EntitiesInsideOutsideUKPage).getOrElse(false),
                         response = ApiResponseData.fromBtnResponse(resp)(using clock)
                       )
                } yield ()
            }
          case None =>
            Future.successful(())
        }
      }
      .recover { (err: Throwable) =>
        sessionRepository.get(userId).flatMap {
          case Some(latest) =>
            for {
              errorAnswers <- Future.fromTry(latest.set(BTNStatus, BTNStatus.error))
              _            <- sessionRepository.set(errorAnswers)
              _ <- auditService.auditBTNSubmission(
                     pillarReference = pillar2Id,
                     accountingPeriod = accountingPeriod,
                     entitiesInsideAndOutsideUK = originalAnswers.get(EntitiesInsideOutsideUKPage).getOrElse(false),
                     response = ApiResponseFailure(
                       statusCode = 500,
                       processedAt = ZonedDateTime.now(),
                       errorCode = "InternalIssueError",
                       responseMessage = err.getMessage
                     )
                   )
            } yield ()
          case None =>
            Future.successful(())
        }
      }

    ()
  }
}

