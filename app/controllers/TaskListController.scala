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

package controllers

import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import models.tasklist.SectionStatus.Completed
import models.tasklist._
import pages.plrReferencePage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Pillar2SessionKeys
import views.html.TaskListView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaskListController @Inject() (
  val controllerComponents:  MessagesControllerComponents,
  identify:                  IdentifierAction,
  getData:                   DataRetrievalAction,
  requireData:               DataRequiredAction,
  view:                      TaskListView,
  val userAnswersConnectors: UserAnswersConnectors
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val groupDetailSection       = groupSections(request.userAnswers)
    val contactDetailSection     = contactSection(request.userAnswers)
    val reviewAndSubmitSection   = reviewSection(request.userAnswers)
    val countOfCompletedSections = (groupDetailSection :+ contactDetailSection :+ reviewAndSubmitSection).count(_.status == Completed)

    val plrReference = request.userAnswers.get(plrReferencePage).isDefined

    if (request.session.get(Pillar2SessionKeys.plrId).isDefined) {
      Future.successful(Redirect(routes.RegistrationConfirmationController.onPageLoad))
    } else {
      if (plrReference) {
        userAnswersConnectors.remove(request.userId).map { _ =>
          logger.info(s"Remove existing amend data from local database if exist")
          Redirect(routes.TaskListController.onPageLoad)
        }
      } else {
        Future.successful(
          Ok(
            view(
              groupSections(request.userAnswers),
              contactSection(request.userAnswers),
              reviewSection(request.userAnswers),
              countOfCompletedSections
            )
          )
        )
      }
    }
  }

  private[controllers] def groupSections(userAnswers: UserAnswers): Seq[SectionViewModel] =
    Seq(
      UltimateParentDetailSection.asViewModel(userAnswers),
      FilingMemberDetailSection.asViewModel(userAnswers),
      FurtherGroupDetailSection.asViewModel(userAnswers)
    )

  private[controllers] def contactSection(userAnswers: UserAnswers): SectionViewModel = ContactDetailSection.asViewModel(userAnswers)

  private[controllers] def reviewSection(userAnswers: UserAnswers): SectionViewModel = ReviewAndSubmitSection.asViewModel(userAnswers)
}
