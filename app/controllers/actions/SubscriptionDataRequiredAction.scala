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

import cats.data.OptionT
import connectors.SubscriptionConnector
import models.requests.{OptionalSubscriptionDataRequest, SubscriptionDataRequest}
import models.subscription.ReadSubscriptionRequestParameters
import pages.AgentClientPillar2ReferencePage
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import repositories.SessionRepository
import services.{ReferenceNumberService, SubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.JourneyCheck

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionDataRequiredActionImpl @Inject() (
  subscriptionService:           SubscriptionService,
  referenceNumberService:        ReferenceNumberService,
  subscriptionConnector:         SubscriptionConnector,
  sessionRepository:             SessionRepository
)(implicit val executionContext: ExecutionContext)
    extends SubscriptionDataRequiredAction
    with Logging {

  override protected def refine[A](request: OptionalSubscriptionDataRequest[A]): Future[Either[Result, SubscriptionDataRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request.request, request.request.session)

    request.maybeSubscriptionLocalData match {
      case Some(subscriptionData) =>
        Future.successful(
          Right(
            SubscriptionDataRequest(
              request.request,
              request.userId,
              subscriptionData,
              request.enrolments,
              request.isAgent
            )
          )
        )
      case None =>
        logger.warn(s"subscription data not found for path: ${request.path}")
        if (JourneyCheck.isBTNJourney(request.path)) {
          Future.successful(Left(Redirect(controllers.btn.routes.BTNProblemWithServiceController.onPageLoad)))
        } else if (JourneyCheck.isManageAccountJourney(request.path)) {
          attemptCacheRepopulation(request)
        } else {
          Future.successful(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
        }
    }
  }

  private def attemptCacheRepopulation[A](request: OptionalSubscriptionDataRequest[A])(implicit
    hc:                                            HeaderCarrier
  ): Future[Either[Result, SubscriptionDataRequest[A]]] = {
    val result = for {
      userAnswers <- OptionT.liftF(sessionRepository.get(request.userId))
      referenceNumber <- OptionT
                           .fromOption[Future](
                             if (request.isAgent) {
                               userAnswers.flatMap(_.get(AgentClientPillar2ReferencePage))
                             } else {
                               None
                             }
                           )
                           .orElse(OptionT.fromOption[Future](referenceNumberService.get(userAnswers, Some(request.enrolments))))
      _ <- OptionT.liftF {
             logger.info(
               s"Attempting to re-populate subscription cache for user ${request.userId} (isAgent: ${request.isAgent}) with PLR: $referenceNumber"
             )
             subscriptionService
               .cacheSubscription(ReadSubscriptionRequestParameters(request.userId, referenceNumber))
               .map(_ => ())
           }
      repopulatedCache <- OptionT(
                            subscriptionConnector.getSubscriptionCache(request.userId)
                          )
    } yield repopulatedCache

    result.value
      .flatMap {
        case Some(subscriptionData) =>
          logger.info(s"Successfully re-populated subscription cache for user ${request.userId} on manage-account journey")
          Future.successful(
            Right(
              SubscriptionDataRequest(
                request.request,
                request.userId,
                subscriptionData,
                request.enrolments,
                request.isAgent
              )
            )
          )
        case None =>
          logger.warn(
            s"Failed to re-populate subscription cache for user ${request.userId} on manage-account journey. Redirecting to error page."
          )
          Future.successful(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
      }
      .recover { case ex: Throwable =>
        logger.error(
          s"Error while attempting to re-populate subscription cache for user ${request.userId} on manage-account journey: ${ex.getMessage}",
          ex
        )
        Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
  }

}

trait SubscriptionDataRequiredAction extends ActionRefiner[OptionalSubscriptionDataRequest, SubscriptionDataRequest]
