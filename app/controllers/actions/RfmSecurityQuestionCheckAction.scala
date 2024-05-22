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
import com.google.inject.Inject
import config.FrontendAppConfig
import models.requests.OptionalDataRequest
import models.rfm.RegistrationDate
import models.subscription.SubscriptionData
import pages.{RfmPillar2ReferencePage, RfmRegistrationDatePage}
import play.api.mvc.Results._
import play.api.mvc._
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

trait RfmSecurityQuestionCheckAction extends ActionFilter[OptionalDataRequest]

class RfmSecurityQuestionCheckActionImpl @Inject() (
  sessionRepository:             SessionRepository,
  subscriptionService:           SubscriptionService,
  appConfig:                     FrontendAppConfig
)(implicit val executionContext: ExecutionContext)
    extends RfmSecurityQuestionCheckAction {

  override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val rfmAccessEnabled = appConfig.rfmAccessEnabled

    def checkUserAnswers(userId: String): Future[Option[Result]] =
      sessionRepository.get(userId).flatMap {
        case Some(userAnswers) if userAnswers.rfmAnsweredSecurityQuestions =>
          (for {
            sessionPillar2Ref     <- userAnswers.get(RfmPillar2ReferencePage)
            sessionPillar2RegDate <- userAnswers.get(RfmRegistrationDatePage)
          } yield subscriptionService.readSubscription(sessionPillar2Ref).flatMap { subscriptionData =>
            if (subscriptionData.upeDetails.registrationDate.isEqual(sessionPillar2RegDate.rfmRegistrationDate)) {
              Future.successful(None)
            } else {
              Future.successful(Some(Redirect(controllers.rfm.routes.MismatchedRegistrationDetailsController.onPageLoad)))
            }
          }).getOrElse(Future.successful(Some(Redirect(controllers.rfm.routes.MismatchedRegistrationDetailsController.onPageLoad))))
        case _ =>
          Future.successful(Some(Redirect(controllers.rfm.routes.MismatchedRegistrationDetailsController.onPageLoad)))
      }

    if (rfmAccessEnabled) {
      checkUserAnswers(request.userId)
    } else {
      Future.successful(None)
    }
  }
}
