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
import models.*
import models.longrunningsubmissions.LongRunningSubmission.Registration
import pages.*
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.*
import repositories.SessionRepository
import services.SubscriptionOrchestrationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers.*
import viewmodels.govuk.summarylist.*
import views.html.CheckYourAnswersView

import java.time.Clock
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi:         MessagesApi,
  identify:                         IdentifierAction,
  getData:                          DataRetrievalAction,
  requireData:                      DataRequiredAction,
  subscriptionOrchestrationService: SubscriptionOrchestrationService,
  val controllerComponents:         MessagesControllerComponents,
  userAnswersConnectors:            UserAnswersConnectors,
  sessionRepository:                SessionRepository,
  view:                             CheckYourAnswersView,
  countryOptions:                   CountryOptions
)(using ec: ExecutionContext, appConfig: FrontendAppConfig, clock: Clock)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async { request =>
    given Request[AnyContent] = request
    given userAnswers: UserAnswers = request.userAnswers
    sessionRepository.get(request.userId).map { optionalUserAnswer =>
      (for {
        userAnswer <- optionalUserAnswer
        _          <- userAnswer.get(PlrReferencePage)
      } yield Redirect(controllers.routes.CannotReturnAfterSubscriptionController.onPageLoad))
        .getOrElse {
          setCheckYourAnswersLogic(userAnswers)
          Ok(
            view(upeSummaryList, nfmSummaryList, groupDetailSummaryList, primaryContactSummaryList, secondaryContactSummaryList, addressSummaryList)
          )
        }
    }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { request =>
    given Request[AnyContent] = request
    subscriptionOrchestrationService.onSubmit(userId = request.userId, userAnswers = request.userAnswers)
  }

  private def setCheckYourAnswersLogic(userAnswers: UserAnswers)(using hc: HeaderCarrier): Future[Unit] =
    for {
      updatedAnswers      <- Future.fromTry(userAnswers.set(CheckYourAnswersLogicPage, true))
      _                   <- userAnswersConnectors.save(updatedAnswers.id, Json.toJson(updatedAnswers.data))
      optionalSessionData <- sessionRepository.get(updatedAnswers.id)
      sessionData = optionalSessionData.getOrElse(UserAnswers(updatedAnswers.id))
      updatedSessionData <- Future.fromTry(sessionData.remove(SubscriptionStatusPage))
      _                  <- sessionRepository.set(updatedSessionData)
    } yield (): Unit

  private def addressSummaryList(using messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(ContactCorrespondenceAddressSummary.row(userAnswers, countryOptions)).flatten
    ).withCssClass("govuk-!-margin-bottom-6")

  private def secondaryContactSummaryList(using messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        AddSecondaryContactSummary.row(userAnswers),
        SecondaryContactNameSummary.row(userAnswers),
        SecondaryContactEmailSummary.row(userAnswers),
        SecondaryPhonePreferenceSummary.row(userAnswers),
        SecondaryPhoneSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def primaryContactSummaryList(using messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        ContactNameComplianceSummary.row(userAnswers),
        ContactEmailAddressSummary.row(userAnswers),
        ContactByPhoneSummary.row(userAnswers),
        ContactCapturePhoneDetailsSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def nfmSummaryList(using messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        NominateFilingMemberYesNoSummary.row(userAnswers),
        NfmNameRegistrationSummary.row(userAnswers),
        NfmRegisteredAddressSummary.row(userAnswers, countryOptions),
        NfmContactNameSummary.row(userAnswers),
        NfmEmailAddressSummary.row(userAnswers),
        NfmPhonePreferenceSummary.row(userAnswers),
        NfmContactPhoneSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyNameNfmSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyRegNfmSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyUtrNfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyNameNfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyRegNfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyUtrNfmSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def upeSummaryList(using messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        UpeNameRegistrationSummary.row(userAnswers),
        UpeRegisteredAddressSummary.row(userAnswers, countryOptions),
        UpeContactNameSummary.row(userAnswers),
        UpeContactEmailSummary.row(userAnswers),
        UpePhonePreferenceSummary.row(userAnswers),
        UPEContactPhoneSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyNameUpeSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyRegUpeSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyUtrUpeSummary.row(userAnswers),
        EntityTypePartnershipCompanyNameUpeSummary.row(userAnswers),
        EntityTypePartnershipCompanyRegUpeSummary.row(userAnswers),
        EntityTypePartnershipCompanyUtrUpeSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")

  private def groupDetailSummaryList(using messages: Messages, userAnswers: UserAnswers) =
    SummaryListViewModel(
      rows = Seq(
        MneOrDomesticSummary.row(userAnswers),
        GroupAccountingPeriodSummary.row(userAnswers),
        GroupAccountingPeriodStartDateSummary.row(userAnswers),
        GroupAccountingPeriodEndDateSummary.row(userAnswers)
      ).flatten
    ).withCssClass("govuk-!-margin-bottom-9")
}
