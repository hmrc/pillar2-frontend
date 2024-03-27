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
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import models.hods.UpeCorrespAddressDetails.makeSubscriptionAddress
import models.subscription.AmendSubscriptionRequestParameters
import models.{BadRequestError, DuplicateSubmissionError, InternalServerError_, NotFoundError, ServiceUnavailableError, SubscriptionCreateError, UnprocessableEntityError}
import pages._
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AmendSubscriptionService, ReadSubscriptionService, ReferenceNumberService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Pillar2SessionKeys
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers.manageAccount._
import viewmodels.govuk.summarylist._
import views.html.subscriptionview.manageAccount.ManageContactCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}
class ManageContactCheckYourAnswersController @Inject() (
  val userAnswersConnectors:   UserAnswersConnectors,
  identify:                    IdentifierAction,
  getData:                     DataRetrievalAction,
  requireData:                 DataRequiredAction,
  val readSubscriptionService: ReadSubscriptionService,
  referenceNumberService:      ReferenceNumberService,
  val controllerComponents:    MessagesControllerComponents,
  view:                        ManageContactCheckYourAnswersView,
  countryOptions:              CountryOptions,
  amendSubscriptionService:    AmendSubscriptionService
)(implicit ec:                 ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {
  //scalastyle:off
  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    (for {
      plrReference <- OptionT.fromOption[Future](referenceNumberService.get(None, request.enrolments))
      subData      <- OptionT.liftF(readSubscriptionService.readSubscription(plrReference))
      primaryTelPref = request.userAnswers
                         .get(SubPrimaryPhonePreferencePage)
                         .getOrElse(if (subData.primaryContactDetails.telephone.isDefined) true else false)
      secondaryContactNominated =
        request.userAnswers.get(SubAddSecondaryContactPage).getOrElse(if (subData.secondaryContactDetails.isDefined) true else false)
      secondaryPhonePref = request.userAnswers
                             .get(SubSecondaryPhonePreferencePage)
                             .getOrElse(if (subData.secondaryContactDetails.flatMap(_.telephone).isDefined) true else false)
      updatedAnswers  <- OptionT.liftF(Future.fromTry(request.userAnswers.set(SubPrimaryPhonePreferencePage, primaryTelPref)))
      updatedAnswers1 <- OptionT.liftF(Future.fromTry(updatedAnswers.set(SubSecondaryPhonePreferencePage, secondaryPhonePref)))
      _               <- OptionT.liftF(userAnswersConnectors.save(updatedAnswers1.id, Json.toJson(updatedAnswers1.data)))
    } yield {

      val primaryPhoneSummary = if (request.userAnswers.get(SubPrimaryPhonePreferencePage).contains(true)) {
        ContactCaptureTelephoneDetailsSummary.row(request.userAnswers.get(SubPrimaryCapturePhonePage) orElse subData.primaryContactDetails.telephone)
      } else {
        None
      }
      val primaryContactList = SummaryListViewModel(
        List(
          Some(ContactNameComplianceSummary.row(request.userAnswers.get(SubPrimaryContactNamePage).getOrElse(subData.primaryContactDetails.name))),
          Some(ContactEmailAddressSummary.row(request.userAnswers.get(SubPrimaryEmailPage).getOrElse(subData.primaryContactDetails.emailAddress))),
          Some(ContactByTelephoneSummary.row(request.userAnswers.get(SubPrimaryPhonePreferencePage).getOrElse(primaryTelPref))),
          primaryPhoneSummary
        ).flatten
      )

      val secondaryPreference = SummaryListViewModel(
        rows = Seq(AddSecondaryContactSummary.row(request.userAnswers.get(SubAddSecondaryContactPage).getOrElse(secondaryContactNominated)))
      )

      val secondaryTelephoneSummary =
        if (request.userAnswers.get(SubSecondaryPhonePreferencePage).contains(true)) {
          SecondaryTelephoneSummary.row(
            request.userAnswers.get(SubSecondaryCapturePhonePage) orElse subData.secondaryContactDetails.flatMap(_.telephone)
          )
        } else {
          None
        }
      val secondaryContactList = SummaryListViewModel(
        rows = Seq(
          SecondaryContactNameSummary.row(request.userAnswers.get(SubSecondaryContactNamePage) orElse subData.secondaryContactDetails.map(_.name)),
          SecondaryContactEmailSummary.row(request.userAnswers.get(SubSecondaryEmailPage) orElse subData.secondaryContactDetails.map(_.emailAddress)),
          SecondaryTelephonePreferenceSummary.row(request.userAnswers.get(SubSecondaryPhonePreferencePage)),
          secondaryTelephoneSummary
        ).flatten
      )
      val address = SummaryListViewModel(
        rows = Seq(
          ContactCorrespondenceAddressSummary.row(
            request.userAnswers
              .get(SubRegisteredAddressPage)
              .getOrElse(makeSubscriptionAddress(subData.upeCorrespAddressDetails)),
            countryOptions
          )
        )
      )
      Ok(view(primaryContactList, secondaryPreference, secondaryContactNominated, secondaryContactList, address))
    }).getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) async { implicit request =>
    val showErrorScreens = appConfig.showErrorScreens

    amendSubscriptionService.amendSubscription(AmendSubscriptionRequestParameters(request.userId)).flatMap {
      case Right(s) =>
        userAnswersConnectors.remove(request.userId).map { _ =>
          logger.info(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Redirecting to Dashboard from contact details")
          Redirect(controllers.routes.DashboardController.onPageLoad)
        }

      case Left(error) if showErrorScreens =>
        val errorMessage = error match {
          case BadRequestError =>
            s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Bad request error."
          case NotFoundError =>
            s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - No subscription data found."
          case DuplicateSubmissionError =>
            s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Duplicate submission detected."
          case UnprocessableEntityError =>
            s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Unprocessable entity error."
          case InternalServerError_ =>
            s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Internal server error."
          case ServiceUnavailableError =>
            s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Service Unavailable error."
          case SubscriptionCreateError =>
            s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Subscription creation error."
        }
        logger.error(errorMessage)
        Future.successful(Redirect(routes.ViewAmendSubscriptionFailedController.onPageLoad))

      case _ =>
        logger.error(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - An error occurred during amend subscription processing")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

}
