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

package controllers.repayments

import config.FrontendAppConfig
import controllers.actions.{FeatureFlagActionFactory, IdentifierAction, SessionDataRequiredAction, SessionDataRetrievalAction}
import models.UserAnswers
import pages._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.repayments.RepaymentsConfirmationView

import javax.inject.{Inject, Named}
import scala.util.{Failure, Success, Try}

class RepaymentConfirmationController @Inject() (
  val controllerComponents:               MessagesControllerComponents,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  view:                                   RepaymentsConfirmationView,
  featureAction:                          FeatureFlagActionFactory,
  getSessionData:                         SessionDataRetrievalAction,
  requireSessionData:                     SessionDataRequiredAction
)(implicit appConfig:                     FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(clientPillar2Id: Option[String] = None): Action[AnyContent] =
    (featureAction.repaymentsAccessAction andThen identify andThen getSessionData andThen requireSessionData) { implicit request =>
      implicit val userAnswers: UserAnswers = request.userAnswers
      clearRepaymentsData(userAnswers)
      Ok(view(clientPillar2Id))
      clearRepaymentsData(userAnswers) match {
        case Success(_) =>
          Ok(view(clientPillar2Id))
        case Failure(_) =>
          //TODO - Change under construction to the journey recovery page in PIL-1007
          Redirect(controllers.routes.UnderConstructionController.onPageLoad)
      }
    }

  def clearRepaymentsData(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers
      .remove(RepaymentAccountNameConfirmationPage)
      .flatMap(_.remove(RepaymentsContactByTelephonePage))
      .flatMap(_.remove(RepaymentsContactEmailPage))
      .flatMap(_.remove(RepaymentsContactNamePage))
      .flatMap(_.remove(RepaymentsRefundAmountPage))
      .flatMap(_.remove(RepaymentsTelephoneDetailsPage))
}
