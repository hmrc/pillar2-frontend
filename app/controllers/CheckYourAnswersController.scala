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
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{RegisterWithoutIdService, SubscriptionService, TaxEnrolmentService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Pillar2SessionKeys
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi:              MessagesApi,
  identify:                              IdentifierAction,
  getData:                               DataRetrievalAction,
  requireData:                           DataRequiredAction,
  override val registerWithoutIdService: RegisterWithoutIdService,
  override val subscriptionService:      SubscriptionService,
  override val userAnswersConnectors:    UserAnswersConnectors,
  override val taxEnrolmentService:      TaxEnrolmentService,
  val controllerComponents:              MessagesControllerComponents,
  view:                                  CheckYourAnswersView,
  countryOptions:                        CountryOptions
)(implicit ec:                           ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with RegisterAndSubscribe
    with Logging {

  // noinspection ScalaStyle
  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val groupDetailList = SummaryListViewModel(
      rows = Seq(
        MneOrDomesticSummary.row(request.userAnswers),
        GroupAccountingPeriodSummary.row(request.userAnswers),
        GroupAccountingPeriodStartDateSummary.row(request.userAnswers),
        GroupAccountingPeriodEndDateSummary.row(request.userAnswers)
      ).flatten
    )
    val upeSummaryList = SummaryListViewModel(
      rows = Seq(
        UpeNameRegistrationSummary.row(request.userAnswers),
        UpeRegisteredAddressSummary.row(request.userAnswers, countryOptions),
        UpeContactNameSummary.row(request.userAnswers),
        UpeContactEmailSummary.row(request.userAnswers),
        UpeTelephonePreferenceSummary.row(request.userAnswers),
        UPEContactTelephoneSummary.row(request.userAnswers),
        EntityTypeIncorporatedCompanyNameUpeSummary.row(request.userAnswers),
        EntityTypeIncorporatedCompanyRegUpeSummary.row(request.userAnswers),
        EntityTypeIncorporatedCompanyUtrUpeSummary.row(request.userAnswers),
        EntityTypePartnershipCompanyNameUpeSummary.row(request.userAnswers),
        EntityTypePartnershipCompanyRegUpeSummary.row(request.userAnswers),
        EntityTypePartnershipCompanyUtrUpeSummary.row(request.userAnswers)
      ).flatten
    )
    val nfmSummaryList = SummaryListViewModel(
      rows = Seq(
        NominateFilingMemberYesNoSummary.row(request.userAnswers),
        NfmNameRegistrationSummary.row(request.userAnswers),
        NfmRegisteredAddressSummary.row(request.userAnswers, countryOptions),
        NfmContactNameSummary.row(request.userAnswers),
        NfmEmailAddressSummary.row(request.userAnswers),
        NfmTelephonePreferenceSummary.row(request.userAnswers),
        NfmContactTelephoneSummary.row(request.userAnswers),
        EntityTypeIncorporatedCompanyNameNfmSummary.row(request.userAnswers),
        EntityTypeIncorporatedCompanyRegNfmSummary.row(request.userAnswers),
        EntityTypeIncorporatedCompanyUtrNfmSummary.row(request.userAnswers),
        EntityTypePartnershipCompanyNameNfmSummary.row(request.userAnswers),
        EntityTypePartnershipCompanyRegNfmSummary.row(request.userAnswers),
        EntityTypePartnershipCompanyUtrNfmSummary.row(request.userAnswers)
      ).flatten
    )
    val primaryContactList = SummaryListViewModel(
      rows = Seq(
        ContactNameComplianceSummary.row(request.userAnswers),
        ContactEmailAddressSummary.row(request.userAnswers),
        ContactByTelephoneSummary.row(request.userAnswers),
        ContactCaptureTelephoneDetailsSummary.row(request.userAnswers)
      ).flatten
    )
    val secondaryContactList = SummaryListViewModel(
      rows = Seq(
        AddSecondaryContactSummary.row(request.userAnswers),
        SecondaryContactNameSummary.row(request.userAnswers),
        SecondaryContactEmailSummary.row(request.userAnswers),
        SecondaryTelephonePreferenceSummary.row(request.userAnswers),
        SecondaryTelephoneSummary.row(request.userAnswers)
      ).flatten
    )
    val address = SummaryListViewModel(
      rows = Seq(ContactCorrespondenceAddressSummary.row(request.userAnswers, countryOptions)).flatten
    )
    if (request.session.get(Pillar2SessionKeys.plrId).isDefined) {
      Redirect(controllers.routes.CannotReturnAfterSubscriptionController.onPageLoad)
    } else {
      Ok(view(upeSummaryList, nfmSummaryList, groupDetailList, primaryContactList, secondaryContactList, address))
    }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    val upeRegInfo = request.userAnswers.getUpRegData
    val fmSafeID   = request.userAnswers.getFmSafeID
    (upeRegInfo, fmSafeID) match {
      case (Right(upe), Right(s)) if request.userAnswers.finalStatusCheck => createRegistrationAndSubscription(upe, s)
      case _ => Future.successful(Redirect(controllers.subscription.routes.InprogressTaskListController.onPageLoad))
    }
  }
}
