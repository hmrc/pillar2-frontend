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
import com.google.inject.Inject
import com.google.inject.name.Named
import config.FrontendAppConfig
import controllers.actions._
import models.UserAnswers
import models.repayments.{PdfModel, RepaymentJourneyModel}
import models.rfm.RfmJourneyModel
import pages.pdf.{PdfRegistrationDatePage, PdfRegistrationTimeStampPage}
import pages.{PlrReferencePage, UpeNameRegistrationPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import repositories.SessionRepository
import services.FopService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{Pillar2Reference, ViewHelpers}
import views.xml.pdf._

import scala.concurrent.{ExecutionContext, Future}

class PrintPdfController @Inject() (
  override val messagesApi:                        MessagesApi,
  @Named("EnrolmentIdentifier") identifyRepayment: IdentifierAction,
  getSessionData:                                  SessionDataRetrievalAction,
  requireSessionData:                              SessionDataRequiredAction,
  identify:                                        IdentifierAction,
  getData:                                         DataRetrievalAction,
  requireData:                                     DataRequiredAction,
  rfmAnswersPdfView:                               RfmAnswersPdf,
  rfmConfirmationPdfView:                          RfmConfirmationPdf,
  repaymentAnswersPdfView:                         RepaymentAnswersPdf,
  repaymentConfirmationPdfView:                    RepaymentConfirmationPdf,
  registrationConfirmationPdfView:                 ConfirmationPdf,
  fopService:                                      FopService,
  sessionRepository:                               SessionRepository,
  val controllerComponents:                        MessagesControllerComponents
)(implicit ec:                                     ExecutionContext, appConfig: FrontendAppConfig)
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
    val currentDate = HtmlFormat.escape(dateHelper.getDateTimeGMT)
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

  def onDownloadRepaymentAnswers: Action[AnyContent] = (identifyRepayment andThen getSessionData andThen requireSessionData).async {
    implicit request =>
      RepaymentJourneyModel
        .from(request.userAnswers)
        .map { model =>
          fopService.render(repaymentAnswersPdfView.render(model, implicitly, implicitly).body).map { pdf =>
            Ok(pdf)
              .as("application/octet-stream")
              .withHeaders(CONTENT_DISPOSITION -> "attachment; filename=repayment-answers.pdf")
          }
        }
        .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }

  def onDownloadRepaymentConfirmation: Action[AnyContent] = (identifyRepayment andThen getSessionData andThen requireSessionData).async {
    implicit request =>
      val currentDate = HtmlFormat.escape(dateHelper.getDateTimeGMT)
      fopService.render(repaymentConfirmationPdfView.render(currentDate.toString(), implicitly, implicitly).body).map { pdf =>
        Ok(pdf)
          .as("application/octet-stream")
          .withHeaders(CONTENT_DISPOSITION -> "attachment; filename=repayment-confirmation.pdf")
      }
  }

  def onDownload: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    sessionRepository.get(request.userAnswers.id).flatMap { optionalUserAnswers =>
      val pdfData: Option[PdfModel] = for {
        userAnswer <- optionalUserAnswers
        pillar2Id <- Pillar2Reference
                       .getPillar2ID(request.enrolments, appConfig.enrolmentKey, appConfig.enrolmentIdentifier)
                       .orElse(userAnswer.get(PlrReferencePage))
        companyName <- userAnswer.get(UpeNameRegistrationPage)
        regDate     <- userAnswer.get(PdfRegistrationDatePage)
        timeStamp   <- userAnswer.get(PdfRegistrationTimeStampPage)
      } yield PdfModel(pillar2Id, regDate, timeStamp, companyName)
      pdfData match {
        case Some(data) =>
          fopService.render(registrationConfirmationPdfView.render(data, implicitly, implicitly).body).map { pdf =>
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
