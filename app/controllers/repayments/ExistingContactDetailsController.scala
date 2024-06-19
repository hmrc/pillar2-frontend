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

import cats.syntax.option._
import config.FrontendAppConfig
import connectors.SubscriptionConnector
import controllers.actions._
import forms.ExistingContactDetailsFormProvider
import pages.{ExistingContactDetailsPage, SubPrimaryCapturePhonePage, SubPrimaryContactNamePage, SubPrimaryEmailPage, SubPrimaryPhonePreferencePage}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.repayments.ExistingContactDetailsView
import controllers.repayments.ExistingContactDetailsController.contactSummaryList
import controllers.subscription.manageAccount.identifierAction
import models.NormalMode
import navigation.RepaymentNavigator
import play.api.data.Form
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class ExistingContactDetailsController @Inject() (
  val subscriptionConnector: SubscriptionConnector,
  identify:                  IdentifierAction,
  getSessionData:            SessionDataRetrievalAction,
  requireSessionData:        SessionDataRequiredAction,
  agentIdentifierAction:     AgentIdentifierAction,
  sessionRepository:         SessionRepository,
  featureAction:             FeatureFlagActionFactory,
  formProvider:              ExistingContactDetailsFormProvider,
  val controllerComponents:  MessagesControllerComponents,
  navigator:                 RepaymentNavigator,
  view:                      ExistingContactDetailsView
)(implicit ec:               ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(clientPillar2Id: Option[String] = None): Action[AnyContent] =
    (featureAction.repaymentsAccessAction andThen identifierAction(
      clientPillar2Id,
      agentIdentifierAction,
      identify
    ) andThen getSessionData andThen requireSessionData).async { implicit request =>
      val preparedForm = request.userAnswers.get(ExistingContactDetailsPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      subscriptionConnector.getSubscriptionCache(request.userAnswers.id).map { userAnswers =>
        (for {
          contactName   <- userAnswers.flatMap(_.get(SubPrimaryContactNamePage))
          contactEmail  <- userAnswers.flatMap(_.get(SubPrimaryEmailPage))
          telephonePerf <- userAnswers.flatMap(_.get(SubPrimaryPhonePreferencePage))
        } yield {
          val contactTelephone = if (telephonePerf) userAnswers.flatMap(_.get(SubPrimaryCapturePhonePage)) else None
          Ok(view(preparedForm, contactSummaryList(contactName, contactEmail, contactTelephone)))
        }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
    }

  def onSubmit(clientPillar2Id: Option[String] = None): Action[AnyContent] =
    (identifierAction(
      clientPillar2Id,
      agentIdentifierAction,
      identify
    ) andThen getSessionData andThen requireSessionData).async { implicit request =>
      subscriptionConnector.getSubscriptionCache(request.userAnswers.id).flatMap { userAnswers =>
        (for {
          contactName   <- userAnswers.flatMap(_.get(SubPrimaryContactNamePage))
          contactEmail  <- userAnswers.flatMap(_.get(SubPrimaryEmailPage))
          telephonePerf <- userAnswers.flatMap(_.get(SubPrimaryPhonePreferencePage))
        } yield {
          val contactTelephone = if (telephonePerf) userAnswers.flatMap(_.get(SubPrimaryCapturePhonePage)) else None
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(view(formWithErrors, contactSummaryList(contactName, contactEmail, contactTelephone)))
                ),
              {
                case value @ true =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(ExistingContactDetailsPage, value))
                    //TODO: Update to use RepaymentsContactNamePage when 964 is merged
                    updatedAnswers1 <- Future.fromTry(updatedAnswers.set(SubPrimaryContactNamePage, contactName))
                    //TODO: Update to use RepaymentsContactEmailPage when 964 is merged
                    updatedAnswers2 <- Future.fromTry(updatedAnswers1.set(SubPrimaryEmailPage, contactEmail))
                    //TODO: Update to use RepaymentsPhonePreferencePage when 965 is merged
                    updatedAnswers3 <- Future.fromTry(updatedAnswers2.set(SubPrimaryPhonePreferencePage, telephonePerf))
                    //TODO: Update to use RepaymentsCapturePhonePage when 965 is merged
                    updatedAnswers4 <-
                      Future.fromTry(contactTelephone.map(updatedAnswers3.set(SubPrimaryCapturePhonePage, _)).getOrElse(Success(updatedAnswers3)))
                    _ <- sessionRepository.set(updatedAnswers4)
                    //TODO: Update to redirect to Contact Name page when PIL-964 is merged
                  } yield Redirect(navigator.nextPage(ExistingContactDetailsPage, clientPillar2Id, NormalMode, updatedAnswers))
                case value @ false =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(ExistingContactDetailsPage, value))
                    _              <- sessionRepository.set(updatedAnswers)
                    //TODO: Update to redirect to CYA page when PIL-148 is merged
                  } yield Redirect(navigator.nextPage(ExistingContactDetailsPage, clientPillar2Id, NormalMode, updatedAnswers))
              }
            )
        }).getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
      }
    }
}

object ExistingContactDetailsController {
  def contactSummaryList(contactName: String, contactEmail: String, contactTel: Option[String])(implicit
    messages:                         Messages
  ): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        SummaryListRowViewModel(key = "repayments.existingContactDetails.name", value = ValueViewModel(HtmlFormat.escape(contactName).toString)).some,
        SummaryListRowViewModel(
          key = "repayments.existingContactDetails.email",
          value = ValueViewModel(HtmlFormat.escape(contactEmail).toString)
        ).some,
        contactTel.map(tel =>
          SummaryListRowViewModel(key = "repayments.existingContactDetails.telephone", value = ValueViewModel(HtmlFormat.escape(tel).toString))
        )
      ).flatten
    )
}
