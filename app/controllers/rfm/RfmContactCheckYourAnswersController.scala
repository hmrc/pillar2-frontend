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
import cats.data.OptionT
import cats.implicits.catsSyntaxApplicativeError
import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, RfmIdentifierAction}
import models.{InternalIssueError, UserAnswers}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.rfm.RfmContactCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RfmContactCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  getData:                  DataRetrievalAction,
  rfmIdentify:              RfmIdentifierAction,
  requireData:              DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  subscriptionService:      SubscriptionService,
  view:                     RfmContactCheckYourAnswersView,
  countryOptions:           CountryOptions
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (rfmIdentify andThen getData andThen requireData) { implicit request =>
    implicit val userAnswers: UserAnswers = request.userAnswers

    val rfmEnabled = appConfig.rfmAccessEnabled
    if (rfmEnabled) {
      val rfmPrimaryContactList = SummaryListViewModel(
        rows = Seq(
          RfmPrimaryContactNameSummary.row(request.userAnswers),
          RfmPrimaryContactEmailSummary.row(request.userAnswers),
          RfmContactByTelephoneSummary.row(request.userAnswers),
          RfmCapturePrimaryTelephoneSummary.row(request.userAnswers)
        ).flatten
      ).withCssClass("govuk-!-margin-bottom-9")
      val rfmSecondaryContactList = SummaryListViewModel(
        rows = Seq(
          RfmAddSecondaryContactSummary.row(request.userAnswers),
          RfmSecondaryContactNameSummary.row(request.userAnswers),
          RfmSecondaryContactEmailSummary.row(request.userAnswers),
          RfmSecondaryTelephonePreferenceSummary.row(request.userAnswers),
          RfmSecondaryTelephoneSummary.row(request.userAnswers)
        ).flatten
      ).withCssClass("govuk-!-margin-bottom-9")
      val address = SummaryListViewModel(
        rows = Seq(RfmContactAddressSummary.row(request.userAnswers, countryOptions)).flatten
      )
      if (request.userAnswers.rfmContactDetailStatus) {
        Ok(view(rfmCorporatePositionSummaryList, rfmPrimaryContactList, rfmSecondaryContactList, address))
      } else {
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    } else {
      Redirect(controllers.routes.UnderConstructionController.onPageLoad)
    }
  }

  def onSubmit(): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData) async { implicit request =>
    (for {
      newFilingMemberInformation <- OptionT.fromOption[Future](request.userAnswers.getNewFilingMemberDetail)
      subscriptionData           <- OptionT.liftF(subscriptionService.readSubscription(newFilingMemberInformation.plrReference))
      _                          <- OptionT.liftF(subscriptionService.deallocateEnrolment(newFilingMemberInformation.plrReference))
      groupID                    <- OptionT.fromOption[Future](request.groupId)
      _                          <- OptionT.liftF(subscriptionService.allocateEnrolment(groupID, newFilingMemberInformation.plrReference))
      amendData <- OptionT.liftF(
                     subscriptionService
                       .createAmendObjectForReplacingFilingMember(subscriptionData, newFilingMemberInformation, request.userAnswers)
                   )
      _ <- OptionT.liftF(subscriptionService.amendFilingMemberDetails(request.userAnswers.id, amendData))
    } yield Redirect(controllers.routes.UnderConstructionController.onPageLoad))
      .recover {
        case InternalIssueError =>
          logger.warn("Replace filing member failed")
          Redirect(controllers.routes.UnderConstructionController.onPageLoad)
        case _: Exception =>
          logger.warn("Replace filing member failed as expected a value for RfmUkBased page but could not find one")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
      .getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

  private def rfmCorporatePositionSummaryList(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        RfmCorporatePositionSummary.row(userAnswers),
        RfmNameRegistrationSummary.row(userAnswers),
        RfmRegisteredAddressSummary.row(userAnswers, countryOptions),
        EntityTypeIncorporatedCompanyNameRfmSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyRegRfmSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyUtrRfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyNameRfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyRegRfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyUtrRfmSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

}
