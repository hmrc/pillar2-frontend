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

package controllers.rfm

import cats.data.OptionT
import cats.implicits.catsSyntaxApplicativeError
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.*
import models.{InternalIssueError, Mode, NoResultFound}
import pages.{RfmPillar2ReferencePage, RfmRegistrationDatePage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.*
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RowStatus
import viewmodels.checkAnswers.*
import viewmodels.govuk.summarylist.*
import views.html.rfm.SecurityQuestionsCheckYourAnswersView

import javax.inject.Named
import scala.concurrent.{ExecutionContext, Future}

class SecurityQuestionsCheckYourAnswersController @Inject() (
  @Named("RfmIdentifier") identify: IdentifierAction,
  val userAnswersConnectors:        UserAnswersConnectors,
  getSessionData:                   SessionDataRetrievalAction,
  requireSessionData:               SessionDataRequiredAction,
  journeyGuard:                     RfmSessionJourneyGuardAction,
  subscriptionService:              SubscriptionService,
  val controllerComponents:         MessagesControllerComponents,
  view:                             SecurityQuestionsCheckYourAnswersView
)(using appConfig: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getSessionData andThen requireSessionData andThen journeyGuard) { request =>
      given Request[AnyContent] = request
      val list                  = SummaryListViewModel(
        rows = Seq(
          RfmSecurityCheckSummary.row(request.userAnswers),
          RfmRegistrationDateSummary.row(request.userAnswers)
        ).flatten
      )
      if request.userAnswers.securityQuestionStatus == RowStatus.Completed then {
        Ok(view(mode, list))
      } else {
        Redirect(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad)
      }
    }

  def onSubmit: Action[AnyContent] = (identify andThen getSessionData andThen requireSessionData).async { request =>
    given Request[AnyContent] = request
    (for {
      inputPillar2Reference  <- OptionT.fromOption[Future](request.userAnswers.get(RfmPillar2ReferencePage))
      inputRegistrationDate  <- OptionT.fromOption[Future](request.userAnswers.get(RfmRegistrationDatePage))
      readData               <- OptionT.liftF(subscriptionService.readSubscription(inputPillar2Reference))
      matchingPillar2Records <-
        OptionT.liftF(subscriptionService.matchingPillar2Records(request.userId, inputPillar2Reference, inputRegistrationDate))
    } yield
      if matchingPillar2Records then {
        Redirect(controllers.rfm.routes.RfmSaveProgressInformController.onPageLoad())
      } else if !matchingPillar2Records & readData.upeDetails.registrationDate.isEqual(inputRegistrationDate) then {
        userAnswersConnectors.remove(request.userId)
        Redirect(controllers.rfm.routes.RfmSaveProgressInformController.onPageLoad())
      } else {
        Redirect(controllers.rfm.routes.MismatchedRegistrationDetailsController.onPageLoad)
      })
      .recover {
        case InternalIssueError => Redirect(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad)
        case NoResultFound      => Redirect(controllers.rfm.routes.MismatchedRegistrationDetailsController.onPageLoad)
      }
      .getOrElse(Redirect(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad))

  }

}
