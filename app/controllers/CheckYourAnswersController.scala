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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.{DuplicateSubmissionError, InternalIssueError, UserAnswers}
import pages.{PlrReferencePage, SubMneOrDomesticPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Pillar2SessionKeys
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify:                 IdentifierAction,
  getData:                  DataRetrievalAction,
  requireData:              DataRequiredAction,
  subscriptionService:      SubscriptionService,
  val controllerComponents: MessagesControllerComponents,
  userAnswersConnectors:    UserAnswersConnectors,
  sessionRepository:        SessionRepository,
  view:                     CheckYourAnswersView,
  countryOptions:           CountryOptions
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    implicit val userAnswers: UserAnswers = request.userAnswers

    sessionRepository.get(request.userId).map { optionalUserAnswer =>
      (for {
        userAnswer <- optionalUserAnswer
        _          <- userAnswer.get(PlrReferencePage)
      } yield Redirect(controllers.routes.CannotReturnAfterSubscriptionController.onPageLoad))
        .getOrElse(
          Ok(view(upeSummaryList, nfmSummaryList, groupDetailSummaryList, primaryContactSummaryList, secondaryContactSummaryList, addressSummaryList))
        )
    }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    if (request.userAnswers.finalStatusCheck) {
      request.userAnswers
        .get(SubMneOrDomesticPage)
        .map { mneOrDom =>
          (for {
            plr <- subscriptionService.createSubscription(request.userAnswers)
            dataToSave = UserAnswers(request.userAnswers.id).setOrException(SubMneOrDomesticPage, mneOrDom).setOrException(PlrReferencePage, plr)
            _ <- sessionRepository.set(dataToSave)
            _ <- userAnswersConnectors.remove(request.userId)
          } yield Redirect(routes.RegistrationConfirmationController.onPageLoad))
            .recover {
              case InternalIssueError =>
                logger.error(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Subscription failed due to failed call to the backend")
                Redirect(controllers.subscription.routes.SubscriptionFailedController.onPageLoad)

              case DuplicateSubmissionError =>
                logger.error(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Subscription failed due to a Duplicate Submission")
                Redirect(controllers.routes.AlreadyRegisteredController.onPageLoad)
            }
        }
        .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
    } else {
      Future.successful(Redirect(controllers.subscription.routes.InprogressTaskListController.onPageLoad))
    }
  }

  private def addressSummaryList(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(ContactCorrespondenceAddressSummary.row(userAnswers, countryOptions)).flatten
    ).withCssClass("govuk-!-margin-bottom-6")

  private def secondaryContactSummaryList(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        AddSecondaryContactSummary.row(userAnswers),
        SecondaryContactNameSummary.row(userAnswers),
        SecondaryContactEmailSummary.row(userAnswers),
        SecondaryTelephonePreferenceSummary.row(userAnswers),
        SecondaryTelephoneSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def primaryContactSummaryList(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        ContactNameComplianceSummary.row(userAnswers),
        ContactEmailAddressSummary.row(userAnswers),
        ContactByTelephoneSummary.row(userAnswers),
        ContactCaptureTelephoneDetailsSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def nfmSummaryList(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        NominateFilingMemberYesNoSummary.row(userAnswers),
        NfmNameRegistrationSummary.row(userAnswers),
        NfmRegisteredAddressSummary.row(userAnswers, countryOptions),
        NfmContactNameSummary.row(userAnswers),
        NfmEmailAddressSummary.row(userAnswers),
        NfmTelephonePreferenceSummary.row(userAnswers),
        NfmContactTelephoneSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyNameNfmSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyRegNfmSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyUtrNfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyNameNfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyRegNfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyUtrNfmSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def upeSummaryList(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        UpeNameRegistrationSummary.row(userAnswers),
        UpeRegisteredAddressSummary.row(userAnswers, countryOptions),
        UpeContactNameSummary.row(userAnswers),
        UpeContactEmailSummary.row(userAnswers),
        UpeTelephonePreferenceSummary.row(userAnswers),
        UPEContactTelephoneSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyNameUpeSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyRegUpeSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyUtrUpeSummary.row(userAnswers),
        EntityTypePartnershipCompanyNameUpeSummary.row(userAnswers),
        EntityTypePartnershipCompanyRegUpeSummary.row(userAnswers),
        EntityTypePartnershipCompanyUtrUpeSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def groupDetailSummaryList(implicit messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        MneOrDomesticSummary.row(userAnswers),
        GroupAccountingPeriodSummary.row(userAnswers),
        GroupAccountingPeriodStartDateSummary.row(userAnswers),
        GroupAccountingPeriodEndDateSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

}
