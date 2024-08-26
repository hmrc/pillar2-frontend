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

package controllers.pdf

import cats.data.OptionT
import config.FrontendAppConfig
import controllers.actions._
import models.UserAnswers
import models.rfm.RfmJourneyModel
import pages.PlrReferencePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import repositories.SessionRepository
import services.FopService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Pillar2Reference, ViewHelpers}
import views.xml.pdf._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PrintPdfController @Inject() (
  override val messagesApi: MessagesApi,
  identify:                 IdentifierAction,
  getData:                  DataRetrievalAction,
  requireData:              DataRequiredAction,
  rfmAnswersPdfView:        RfmAnswersPdf,
  rfmConfirmationPdfView:   RfmConfirmationPdf,
  fopService:               FopService,
  sessionRepository:        SessionRepository,
  val controllerComponents: MessagesControllerComponents
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val dateHelper = new ViewHelpers()

  def onDownloadRfmAnswers: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    RfmJourneyModel
      .from(request.userAnswers)
      .map { model =>
        fopService.render(rfmAnswersPdfView.render(model, implicitly, implicitly).body).map { pdf =>
          Ok(pdf)
            .as("application/octet-stream")
            .withHeaders(CONTENT_DISPOSITION -> "attachment; filename=replace-filing-member-answers.pdf")
        }
      }
      .getOrElse(Future.successful(Redirect(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad)))
  }

  def onDownloadRfmConfirmation: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val currentDate = HtmlFormat.escape(dateHelper.formatDateGDS(java.time.LocalDate.now))
    (for {
      mayBeUserAnswer <- OptionT.liftF(sessionRepository.get(request.userAnswers.id))
      userAnswers = mayBeUserAnswer.getOrElse(UserAnswers(request.userId))
      pillar2Id <- OptionT.fromOption[Future](
                     Pillar2Reference
                       .getPillar2ID(request.enrolments, appConfig.enrolmentKey, appConfig.enrolmentIdentifier)
                       .orElse(userAnswers.get(PlrReferencePage))
                   )
      pdf <- OptionT.liftF(fopService.render(rfmConfirmationPdfView.render(pillar2Id, currentDate.toString(), implicitly, implicitly).body))
    } yield Ok(pdf)
      .as("application/octet-stream")
      .withHeaders(CONTENT_DISPOSITION -> "attachment; filename=replace-filing-member-confirmation.pdf"))
      .getOrElse {
        Redirect(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad)
      }
  }

}
