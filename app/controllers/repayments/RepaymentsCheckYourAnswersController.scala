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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions._
import models.UserAnswers
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.repayments._
import viewmodels.govuk.summarylist._
import views.html.repayments.RepaymentsCheckYourAnswersView

import javax.inject.Named
import scala.concurrent.Future

class RepaymentsCheckYourAnswersController @Inject() (
  override val messagesApi:               MessagesApi,
  @Named("EnrolmentIdentifier") identify: IdentifierAction,
  getSessionData:                         SessionDataRetrievalAction,
  requireSessionData:                     SessionDataRequiredAction,
  featureAction:                          FeatureFlagActionFactory,
  val controllerComponents:               MessagesControllerComponents,
  view:                                   RepaymentsCheckYourAnswersView
)(implicit appConfig:                     FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (featureAction.repaymentsAccessAction andThen identify andThen getSessionData andThen requireSessionData) { implicit request =>
      implicit val userAnswers: UserAnswers = request.userAnswers
      Ok(
        view(listRefund(), listBankAccountDetails(), contactDetailsList())
      )

    }

  def onSubmit(): Action[AnyContent] =
    (featureAction.repaymentsAccessAction andThen identify andThen getSessionData andThen requireSessionData).async {
      Future.successful(Redirect(controllers.routes.UnderConstructionController.onPageLoadAgent))
    }

  private def contactDetailsList()(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        RepaymentsContactNameSummary.row(userAnswers),
        RepaymentsContactEmailSummary.row(userAnswers),
        RepaymentsContactByTelephoneSummary.row(userAnswers),
        RepaymentsTelephoneDetailsSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def listBankAccountDetails()(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        UkOrAbroadBankAccountSummary.row(userAnswers),
        UKBankNameSummary.row(userAnswers),
        UKBankNameOnAccountSummary.row(userAnswers),
        UKBankSortCodeSummary.row(userAnswers),
        UKBankAccNumberSummary.row(userAnswers),
        NonUKBankNameSummary.row(userAnswers),
        NonUKBankNameOnAccountSummary.row(userAnswers),
        NonUKBankBicOrSwiftCodeSummary.row(userAnswers),
        NonUKBankIbanSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def listRefund()(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        RequestRefundAmountSummary.row(userAnswers),
        ReasonForRequestingRefundSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

}
