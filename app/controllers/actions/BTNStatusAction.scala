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

package controllers.actions

import controllers.btn.routes.*
import models.btn.BTNStatus
import models.longrunningsubmissions.LongRunningSubmission.BTN
import models.requests.SubscriptionDataRequest
import models.subscription.SubscriptionLocalData
import pages.EntitiesInsideOutsideUKPage
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, RequestHeader, Result}
import repositories.SessionRepository
import services.audit.AuditService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BTNStatusAction @Inject() (
  val sessionRepository: SessionRepository,
  auditService:          AuditService
)(using val ec: ExecutionContext)
    extends Logging
    with FrontendHeaderCarrierProvider {

  def subscriptionRequest: ActionRefiner[SubscriptionDataRequest, SubscriptionDataRequest] =
    new ActionRefiner[SubscriptionDataRequest, SubscriptionDataRequest] {
      override protected def refine[A](request: SubscriptionDataRequest[A]): Future[Either[Result, SubscriptionDataRequest[A]]] =
        btnAlreadySubmitted(request.userId, request.subscriptionLocalData)(using request)

      override protected def executionContext: ExecutionContext = ec
    }

  private def btnAlreadySubmitted[T <: RequestHeader](userId: String, subscriptionData: SubscriptionLocalData)(using
    request: T
  ): Future[Either[Result, T]] =
    sessionRepository
      .get(userId)
      .map { maybeAnswers =>
        (
          maybeAnswers.flatMap(_.get(BTNStatus)),
          maybeAnswers.flatMap(_.get(EntitiesInsideOutsideUKPage))
        )
      }
      .flatMap { case (btnStatus, entitiesInsideOutsideUk) =>
        btnStatus match {
          case Some(BTNStatus.submitted) =>
            auditService
              .auditBtnAlreadySubmitted(
                subscriptionData.plrReference,
                subscriptionData.subAccountingPeriod,
                entitiesInsideOutsideUk.getOrElse(false)
              )
              .map(_ => Left(Redirect(CheckYourAnswersController.cannotReturnKnockback)))
          case Some(BTNStatus.processing) => Future.successful(Left(Redirect(controllers.routes.WaitingRoomController.onPageLoad(BTN))))
          case _                          => Future.successful(Right(request))
        }
      }
}
