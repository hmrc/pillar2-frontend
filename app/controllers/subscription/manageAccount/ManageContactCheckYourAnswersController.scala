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

package controllers.subscription.manageAccount

import cats.data.OptionT
import cats.implicits.catsSyntaxApplicativeError
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{IdentifierAction, SubscriptionDataRequiredAction, SubscriptionDataRetrievalAction}
import controllers.routes
import models.UnexpectedResponse
import pages.AgentClientPillar2ReferencePage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{ReferenceNumberService, SubscriptionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers.manageAccount._
import viewmodels.govuk.summarylist._
import views.html.subscriptionview.manageAccount.ManageContactCheckYourAnswersView

import javax.inject.Named
import scala.concurrent.{ExecutionContext, Future}
class ManageContactCheckYourAnswersController @Inject() (
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getData:                                SubscriptionDataRetrievalAction,
  requireData:                            SubscriptionDataRequiredAction,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   ManageContactCheckYourAnswersView,
  countryOptions:                         CountryOptions,
  sessionRepository:                      SessionRepository,
  subscriptionService:                    SubscriptionService,
  referenceNumberService:                 ReferenceNumberService
)(implicit ec:                            ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val primaryContactList = SummaryListViewModel(
        rows = Seq(
          ContactNameComplianceSummary.row(),
          ContactEmailAddressSummary.row(),
          ContactByTelephoneSummary.row(),
          ContactCaptureTelephoneDetailsSummary.row()
        ).flatten
      ).withCssClass("govuk-!-margin-bottom-9")

      val secondaryContactList = SummaryListViewModel(
        rows = Seq(
          AddSecondaryContactSummary.row(),
          SecondaryContactNameSummary.row(),
          SecondaryContactEmailSummary.row(),
          SecondaryTelephonePreferenceSummary.row(),
          SecondaryTelephoneSummary.row()
        ).flatten
      ).withCssClass("govuk-!-margin-bottom-9")

      val address = SummaryListViewModel(
        rows = Seq(ContactCorrespondenceAddressSummary.row(countryOptions)).flatten
      ).withCssClass("govuk-!-margin-bottom-9")

      Ok(view(primaryContactList, secondaryContactList, address))
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      (for {
        userAnswers <- OptionT.liftF(sessionRepository.get(request.userId))
        referenceNumber <- OptionT
                             .fromOption[Future](userAnswers.flatMap(_.get(AgentClientPillar2ReferencePage)))
                             .orElse(OptionT.fromOption[Future](referenceNumberService.get(None, enrolments = Some(request.enrolments))))
        _ <- OptionT.liftF(subscriptionService.amendContactOrGroupDetails(request.userId, referenceNumber, request.subscriptionLocalData))
      } yield Redirect(controllers.routes.DashboardController.onPageLoad))
        .recover { case UnexpectedResponse =>
          Redirect(routes.ViewAmendSubscriptionFailedController.onPageLoad)
        }
        .getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
    }

}
