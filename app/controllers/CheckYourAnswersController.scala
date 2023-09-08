/*
 * Copyright 2023 HM Revenue & Customs
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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.fm.FilingMember
import models.{MandatoryInformationMissingError, NfmRegistrationConfirmation, UPERegisteredInUKConfirmation, UserAnswers}
import pages.{NominatedFilingMemberPage, RegistrationPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.RegisterWithoutIdService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.govuk.summarylist._

import scala.concurrent.ExecutionContext.Implicits.global
import views.html.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify:                 IdentifierAction,
  getData:                  DataRetrievalAction,
  requireData:              DataRequiredAction,
  registerWithoutIdService: RegisterWithoutIdService,
  userAnswersConnectors:    UserAnswersConnectors,
  val controllerComponents: MessagesControllerComponents,
  view:                     CheckYourAnswersView
)(implicit appConfig:       FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val list = SummaryListViewModel(
      rows = Seq.empty
    )

    Ok(view(list))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    val regdata = request.userAnswers.get(RegistrationPage).getOrElse(throw new Exception("Registration is not available"))
    val fmData  = request.userAnswers.get(NominatedFilingMemberPage).getOrElse(throw new Exception("Filing is not available"))

    (regdata.safeId, fmData.safeId) match {
      case (Some(safeId), Some(fmSafeId)) =>
        println(" Yes I have both safe id -----------------------------")
        Future.successful(Redirect(routes.IndexController.onPageLoad))
      //createSubscription(safeId, fmSafeId)

      case (Some(safeId), None) =>
        println(" Yes I have(Some(safeId), None) id -----------------------------")
        Future.successful(Redirect(routes.IndexController.onPageLoad))

        registerWithoutIdService.sendFmRegistrationWithoutId(request.userId, Some(request.userAnswers)).flatMap {
          case Right(fmsafeId) => Future.successful(Redirect(routes.IndexController.onPageLoad)) //createSubscription(safeId, fmSafeid)
          case Left(value) =>
            logger.warn(s"Error $value")
            value match {
              case MandatoryInformationMissingError(_) => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
              case _                                   => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
            }
        }

      case (None, Some(fmSafeId)) =>
        println(" Yes I have(None, Some(fmSafeId))id -----------------------------")
        registerWithoutIdService.sendUpeRegistrationWithoutId(request.userId, request.userAnswers).flatMap {
          case Right(safeId) => Future.successful(Redirect(routes.IndexController.onPageLoad)) //createSubscription(safeId, fmSafeid)
          case Left(value) =>
            logger.warn(s"Error $value")
            value match {
              case MandatoryInformationMissingError(_) => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
              case _                                   => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
            }
        }

      case (None, None) =>
        println(" Yes I have(None, None)id -----------------------------")

        for {
          safeId            <- registerWithoutIdService.sendUpeRegistrationWithoutId(request.userId, request.userAnswers)
          userAnswersLatest <- userAnswersConnectors.getUserAnswer(request.userId)
          fmSafeId          <- registerWithoutIdService.sendFmRegistrationWithoutId(request.userId, userAnswersLatest)
        } yield (safeId, fmSafeId) match {

          case (Right(safeId), Right(fmSafeId)) =>
            println(s"what is safeid & fmSafeId--------------------------${(safeId, fmSafeId)}")
            Redirect(routes.IndexController.onPageLoad)
          case (_, Left(value)) =>
            logger.warn(s"Error $value")
            println(s"I am coming to error ---------(_, Left(value))--------------$value")
            value match {
              case MandatoryInformationMissingError(_) => Redirect(routes.UnderConstructionController.onPageLoad)
              case _                                   => Redirect(routes.UnderConstructionController.onPageLoad)

            }
          case (Left(value), _) =>
            logger.warn(s"Error $value")
            println(s"I am coming to error ---------(Left(value), _)--------------$value")
            value match {
              case MandatoryInformationMissingError(_) => Redirect(routes.UnderConstructionController.onPageLoad)
              case _                                   => Redirect(routes.UnderConstructionController.onPageLoad)

            }

        }

      /*        registerWithoutIdService.sendUpeRegistrationWithoutId(request.userId, request.userAnswers).flatMap {
          case Right(safeId) =>
            registerWithoutIdService.sendFmRegistrationWithoutId(request.userId, request.userAnswers).flatMap {
              case Right(fmsafeId) => Future.successful(Redirect(routes.IndexController.onPageLoad)) //createSubscription(safeId,fmsafeId)
              case Left(value) =>
                logger.warn(s"Error $value")
                value match {
                  case MandatoryInformationMissingError(_) => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
                  case _                                   => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
                }
            }
          case Left(value) =>
            println(" I am in error.----------------------")
            logger.warn(s"Error $value")
            value match {
              case MandatoryInformationMissingError(_) => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
              case _                                   => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
            }
        }*/
      /*        registerWithoutIdService.sendUpeRegistrationWithoutId(request.userId, request.userAnswers).flatMap {
          case Right(safeId) =>
            registerWithoutIdService.sendFmRegistrationWithoutId(request.userId, request.userAnswers).flatMap {
              case Right(fmsafeId) => Future.successful(Redirect(routes.IndexController.onPageLoad)) //createSubscription(safeId,fmsafeId)
              case Left(value) =>
                logger.warn(s"Error $value")
                value match {
                  case MandatoryInformationMissingError(_) => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
                  case _                                   => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
                }
            }
          case Left(value) =>
            println(" I am in error.----------------------")
            logger.warn(s"Error $value")
            value match {
              case MandatoryInformationMissingError(_) => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
              case _                                   => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
            }
        }*/
      case _ => Future.successful(Redirect(routes.UnderConstructionController.onPageLoad))
    }
  }

}
