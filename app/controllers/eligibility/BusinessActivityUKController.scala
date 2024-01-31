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

package controllers.eligibility

import config.FrontendAppConfig
import controllers.actions.UnauthenticatedControllerComponents
import forms.BusinessActivityUKFormProvider
import pages.BusinessActivityUKPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.UnauthenticatedDataRepository
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.Pillar2SessionKeys
import views.html.BusinessActivityUKView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessActivityUKController @Inject() (
  formProvider:             BusinessActivityUKFormProvider,
  sessionRepository:        UnauthenticatedDataRepository,
  val controllerComponents: MessagesControllerComponents,
  view:                     BusinessActivityUKView
)(implicit appConfig:       FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad: Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    (for {
      sessionId <- hc.sessionId
      id = sessionId.value
    } yield sessionRepository.get(id).map { userAnswers =>
      userAnswers
        .map { x =>
          val preparedForm = x.get(BusinessActivityUKPage) match {
            case None       => form
            case Some(data) => form.fill(data)
          }
          Ok(view(preparedForm))
        }
        .getOrElse(Ok(view(form)))
    }).getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }

  def onSubmit: Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val sessionID = Pillar2SessionKeys.sessionId(hc)

    sessionRepository.get(sessionID).flatMap { optionalUserAnswer =>
      optionalUserAnswer
        .map { userAnswer =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
              {
                case value @ true =>
                  for {
                    updatedAnswers <- Future.fromTry(userAnswer.set(BusinessActivityUKPage, value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(controllers.eligibility.routes.TurnOverEligibilityController.onPageLoad)
                case value @ false =>
                  for {
                    updatedAnswers <- Future.fromTry(userAnswer.set(BusinessActivityUKPage, value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(controllers.eligibility.routes.KbUKIneligibleController.onPageLoad)
              }
            )
        }
        .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
    }

  }

}
