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

import config.FrontendAppConfig
import controllers.actions._
import models.repayments.PdfModel
import pages.{PlrReferencePage, UpeNameRegistrationPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.FopService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Pillar2Reference
import viewmodels.checkAnswers.GroupAccountingPeriodStartDateSummary.dateHelper
import views.ViewUtils.currentTimeGMT
import views.xml.pdf.ConfirmationPdf

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PrintPdfController @Inject() (
  override val messagesApi: MessagesApi,
  identify:                 IdentifierAction,
  getData:                  DataRetrievalAction,
  requireData:              DataRequiredAction,
  pdfView:                  ConfirmationPdf,
  sessionRepository:        SessionRepository,
  fopService:               FopService,
  val controllerComponents: MessagesControllerComponents
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onDownload: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val regDate = dateHelper.formatDateGDS(java.time.LocalDate.now)

    sessionRepository.get(request.userAnswers.id).flatMap { optionalUserAnswers =>
      val pdfData: Option[PdfModel] = for {
        userAnswer <- optionalUserAnswers
        pillar2Id <- Pillar2Reference
                       .getPillar2ID(request.enrolments, appConfig.enrolmentKey, appConfig.enrolmentIdentifier)
                       .orElse(userAnswer.get(PlrReferencePage))
        companyName <- userAnswer.get(UpeNameRegistrationPage)
      } yield PdfModel(pillar2Id, regDate, currentTimeGMT, companyName)

      pdfData match {
        case Some(data) =>
          fopService.render(pdfView.render(data, implicitly, implicitly).body).map { pdf =>
            Ok(pdf)
              .as("application/octet-stream")
              .withHeaders(CONTENT_DISPOSITION -> "attachment; filename=Pillar 2 Registration Confirmation.pdf")
          }
        case None =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }
  }
}
