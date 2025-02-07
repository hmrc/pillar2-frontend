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
import config.FrontendAppConfig
import connectors.EnrolmentStoreProxyConnector
import controllers.actions._
import forms.RfmSecurityCheckFormProvider
import models.{Mode, NormalMode, UserAnswers}
import navigation.ReplaceFilingMemberNavigator
import pages.RfmPillar2ReferencePage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.rfm.{SecurityCheckErrorView, SecurityCheckView}

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class SecurityCheckController @Inject() (
  sessionRepository:                SessionRepository,
  @Named("RfmIdentifier") identify: IdentifierAction,
  getSessionData:                   SessionDataRetrievalAction,
  requireSessionData:               SessionDataRequiredAction,
  formProvider:                     RfmSecurityCheckFormProvider,
  navigator:                        ReplaceFilingMemberNavigator,
  enrolmentStoreProxyConnector:     EnrolmentStoreProxyConnector,
  val controllerComponents:         MessagesControllerComponents,
  view:                             SecurityCheckView,
  errorView:                        SecurityCheckErrorView
)(implicit ec:                      ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode = NormalMode): Action[AnyContent] =
    (identify andThen getSessionData andThen requireSessionData) { implicit request =>
      val preparedForm = request.userAnswers.get(RfmPillar2ReferencePage).map(form.fill).getOrElse(form)
      Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getSessionData andThen requireSessionData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        pillar2Id =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(RfmPillar2ReferencePage, pillar2Id))
            _              <- sessionRepository.set(updatedAnswers)
            result         <- redirectSecurityCheck(pillar2Id, mode, updatedAnswers)(request.request.groupId)
          } yield result
      )
  }

  def onPageLoadNotAllowed: Action[AnyContent] =
    (identify andThen getSessionData andThen requireSessionData) { implicit request =>
      Ok(errorView())
    }

  /** If the groupId of the logged in user matches the groupId of the pillar2 enrolment, this check will redirect to a KB page
    *
    * The groupId can be the same if the user belongs to an organisation that has other enrolments on other HMRC systems but not a pillar2 enrolment.
    *
    * https://confluence.tools.tax.service.gov.uk/display/GGWRLS/EACD+Concepts%3A+Government+Gateway+Accounts+and+Groups
    */
  private def redirectSecurityCheck(pillar2Id: String, mode: Mode, updatedAnswers: UserAnswers)(userGroupIdOpt: Option[String])(implicit
    hc:                                        HeaderCarrier
  ): Future[Result] =
    enrolmentStoreProxyConnector
      .getGroupIds(pillar2Id)
      .map { groupIds =>
        groupIds -> userGroupIdOpt match {
          case (Some(value), Some(userGroupId)) if value.principalGroupIds.contains(userGroupId) =>
            Redirect(controllers.rfm.routes.SecurityCheckController.onPageLoadNotAllowed())
          case _ => Redirect(navigator.nextPage(RfmPillar2ReferencePage, mode, updatedAnswers))
        }
      }
}
