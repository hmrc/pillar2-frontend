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
import javax.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, RfmIdentifierAction}
import models.UserAnswers
import pages.PlrReferencePage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.rfm.RfmContactCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class RfmContactCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  getData:                  DataRetrievalAction,
  rfmIdentify:              RfmIdentifierAction,
  requireData:              DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  sessionRepository:        SessionRepository,
  view:                     RfmContactCheckYourAnswersView,
  countryOptions:           CountryOptions
)(implicit ec:              ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (rfmIdentify andThen getData andThen requireData).async { implicit request =>
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
      sessionRepository.get(request.userId).map { optionalUserAnswer =>
        (for {
          userAnswer <- optionalUserAnswer
          _          <- userAnswer.get(PlrReferencePage)
        } yield Redirect(controllers.rfm.routes.RfmCannotReturnAfterConfirmationController.onPageLoad))
          .getOrElse(
            if (request.userAnswers.rfmContactDetailStatus) {
              Ok(view(rfmCorporatePositionSummaryList, rfmPrimaryContactList, rfmSecondaryContactList, address))
            } else {
              Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
          )
      }
    } else {
      Future(Redirect(controllers.routes.UnderConstructionController.onPageLoad))
    }
  }

  def onSubmit(): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData).async { implicit request =>
    // TODO - PIL-768, save plrReference to sessionRepository once subscriptionService.amendFilingMemberDetails is successful
    //    (for {
    //      newFilingMemberInformation <- OptionT.fromOption[Future](request.userAnswers.getNewFilingMemberDetail)
    //      ...
    //      ...
    //      _ <- OptionT.liftF(subscriptionService.amendFilingMemberDetails(request.userAnswers.id, amendData))
    //      dataToSave <- UserAnswers(request.userAnswers.id).setOrException(PlrReferencePage, newFilingMemberInformation.plrReference)
    //      _ <- OptionT.liftF(sessionRepository.set(dataToSave))
    //    } yield {
    //      Future.successful(Redirect(controllers.routes.UnderConstructionController.onPageLoad.url))
    //    })
    Future.successful(Redirect(controllers.routes.UnderConstructionController.onPageLoad.url))
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
