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
import models.subscription.SubscriptionStatus
import models.subscription.SubscriptionStatus._
import models.{DuplicateSafeIdError, DuplicateSubmissionError, InternalIssueError, UserAnswers, WithName}
import pages._
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
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

    setCheckYourAnswersLogic(userAnswers).flatMap { _ =>
      sessionRepository.get(request.userId).map { optionalUserAnswer =>
        (for {
          userAnswer <- optionalUserAnswer
          _          <- userAnswer.get(PlrReferencePage)
        } yield Redirect(controllers.routes.CannotReturnAfterSubscriptionController.onPageLoad))
          .getOrElse(
            Ok(
              view(upeSummaryList, nfmSummaryList, groupDetailSummaryList, primaryContactSummaryList, secondaryContactSummaryList, addressSummaryList)
            )
          )
      }
    }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    if (request.userAnswers.finalStatusCheck) {
      val subscriptionStatus: Future[WithName with SubscriptionStatus] =
        request.userAnswers
          .get(SubMneOrDomesticPage)
          .map { mneOrDom =>
            val companyName: String = request.userAnswers.get(FmNameRegistrationPage) match {
              case Some(x) => x
              case None =>
                request.userAnswers.get(UpeNameRegistrationPage) match {
                  case Some(y) => y
                  case None =>
                    request.userAnswers.get(UpeGRSResponsePage) match {
                      case Some(z) =>
                        z.partnershipEntityRegistrationData match {
                          case Some(a) => a.companyProfile.get.companyName
                          case None    => z.incorporatedEntityRegistrationData.get.companyProfile.companyName
                        }
                      case None => "THROW exception"
                    }
                }
            }
            (for {
              plr <- subscriptionService.createSubscription(request.userAnswers)
              dataToSave = UserAnswers(request.userId)
                             .setOrException(UpeNameRegistrationPage, companyName)
                             .setOrException(SubMneOrDomesticPage, mneOrDom)
                             .setOrException(PlrReferencePage, plr)
              _ <- sessionRepository.set(dataToSave)
              _ <- userAnswersConnectors.remove(request.userId)
            } yield SuccessfullyCompletedSubscription)
              .recover {
                case InternalIssueError =>
                  logger.error("Subscription failed due to failed call to the backend")
                  FailedWithInternalIssueError
                case DuplicateSubmissionError =>
                  logger.error("Subscription failed due to a Duplicate Submission")
                  FailedWithDuplicatedSubmission
                case DuplicateSafeIdError =>
                  logger.error("Subscription failed due to a Duplicate SafeId for UPE and NFM")
                  FailedWithDuplicatedSafeIdError
              }
          }
          .getOrElse(Future.successful(FailedWithNoMneOrDomesticValueFoundError))
      for {
        updatedSubscriptionStatus <- subscriptionStatus
        updatedAnswers            <- Future.fromTry(request.userAnswers.set(SubscriptionStatusPage, updatedSubscriptionStatus))
        _                         <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
      } yield (): Unit
      Redirect(controllers.routes.RegistrationWaitingRoomController.onPageLoad())
    } else {
      Redirect(controllers.subscription.routes.InprogressTaskListController.onPageLoad)
    }
  }
  private def setCheckYourAnswersLogic(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[UserAnswers] =
    for {
      updatedAnswers  <- Future.fromTry(userAnswers.set(CheckYourAnswersLogicPage, true))
      updatedAnswers1 <- Future.fromTry(updatedAnswers.remove(SubscriptionStatusPage))
      _               <- userAnswersConnectors.save(updatedAnswers1.id, Json.toJson(updatedAnswers1.data))
    } yield updatedAnswers1

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
