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
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl._
import uk.gov.hmrc.play.bootstrap.binders._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.Pillar2SessionKeys
import views.html.JourneyRecoveryView

import javax.inject.Inject
class JourneyRecoveryController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  journeyRecoveryView:      JourneyRecoveryView
)(implicit appConfig:       FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(continueUrl: Option[RedirectUrl] = None): Action[AnyContent] = Action { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val safeUrl: Option[String] = continueUrl.flatMap { unsafeUrl =>
      unsafeUrl.getEither(OnlyRelative) match {
        case Right(safeUrl) =>
          Some(safeUrl.url)
        case Left(message) =>
          logger.info(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - $message")
          None
      }
    }

    val (scenario, url) = safeUrl match {
      case Some(url)                                 => ("continue", Some(url))
      case None if shouldPreventBookmarking(request) => ("bookmarkPrevention", None)
      case None                                      => ("startAgain", None)
    }

    Ok(journeyRecoveryView(scenario, url))
  }

  private def shouldPreventBookmarking(request: Request[_]): Boolean =
    false
}
