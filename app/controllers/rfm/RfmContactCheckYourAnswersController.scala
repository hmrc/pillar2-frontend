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
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, FeatureFlagActionFactory, IdentifierAction}
import models.requests.DataRequest
import models.{InternalIssueError, UnexpectedResponse}
import pages.PlrReferencePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.rfm.RfmContactCheckYourAnswersView

import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class RfmContactCheckYourAnswersController @Inject() (
  override val messagesApi:            MessagesApi,
  getData:                             DataRetrievalAction,
  @Named("RfmIdentifier") rfmIdentify: IdentifierAction,
  Identify:                            IdentifierAction,
  requireData:                         DataRequiredAction,
  featureAction:                       FeatureFlagActionFactory,
  val controllerComponents:            MessagesControllerComponents,
  userAnswersConnectors:               UserAnswersConnectors,
  subscriptionService:                 SubscriptionService,
  sessionRepository:                   SessionRepository,
  view:                                RfmContactCheckYourAnswersView,
  countryOptions:                      CountryOptions
)(implicit ec:                         ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (featureAction.rfmAccessAction andThen Identify andThen getData andThen requireData).async {
    implicit request =>
      val address = SummaryListViewModel(
        rows = Seq(RfmContactAddressSummary.row(request.userAnswers, countryOptions)).flatten
      )
      sessionRepository.get(request.userId).map { optionalUserAnswer =>
        (for {
          userAnswer <- optionalUserAnswer
          _          <- userAnswer.get(PlrReferencePage)
        } yield Redirect(controllers.rfm.routes.RfmCannotReturnAfterConfirmationController.onPageLoad))
          .getOrElse(
            Ok(
              view(
                request.userAnswers.rfmCorporatePositionSummaryList(countryOptions),
                request.userAnswers.rfmPrimaryContactList,
                request.userAnswers.rfmSecondaryContactList,
                address
              )
            )
          )
      }
  }

  def onSubmit(): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData) async { implicit request =>
    if (request.userAnswers.isRfmJourneyCompleted) {
      replaceFilingMemberDetails(request)
    } else {
      Future.successful(Redirect(controllers.rfm.routes.RfmIncompleteDataController.onPageLoad))
    }

  }

  private def replaceFilingMemberDetails(request: DataRequest[AnyContent])(implicit hc: HeaderCarrier): Future[Result] =
    (for {
      newFilingMemberInformation <- OptionT.fromOption[Future](request.userAnswers.getNewFilingMemberDetail)
      subscriptionData           <- OptionT.liftF(subscriptionService.readSubscription(newFilingMemberInformation.plrReference))
      amendData <- OptionT.liftF(
                     subscriptionService.createAmendObjectForReplacingFilingMember(subscriptionData, newFilingMemberInformation, request.userAnswers)
                   )
      _ <- OptionT.liftF(subscriptionService.amendFilingMemberDetails(request.userAnswers.id, amendData))
      _ <- OptionT.liftF(subscriptionService.deallocateEnrolment(newFilingMemberInformation.plrReference))
      upeEnrolmentInfo <- OptionT.liftF(
                            subscriptionService.getUltimateParentEnrolmentInformation(
                              subscriptionData = subscriptionData,
                              pillar2Reference = newFilingMemberInformation.plrReference,
                              request.userIdForEnrolment
                            )
                          )
      groupId <- OptionT.fromOption[Future](request.groupId)
      _ <- OptionT.liftF(
             subscriptionService.allocateEnrolment(groupId = groupId, plrReference = newFilingMemberInformation.plrReference, upeEnrolmentInfo)
           )
      _          <- OptionT.liftF(userAnswersConnectors.remove(request.userId))
      dataToSave <- OptionT.liftF(Future.fromTry(request.userAnswers.set(PlrReferencePage, newFilingMemberInformation.plrReference)))
      _          <- OptionT.liftF(sessionRepository.set(dataToSave))
    } yield {
      logger.info(s"successfully replaced filing member for group with id : $groupId ")
      Redirect(controllers.rfm.routes.RfmConfirmationController.onPageLoad)
    })
      .recover {
        case InternalIssueError | UnexpectedResponse =>
          Redirect(controllers.rfm.routes.AmendApiFailureController.onPageLoad)
        case _: Exception =>
          logger.warn("Replace filing member failed as expected a value for RfmUkBased page but could not find one")
          Redirect(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad)
      }
      .getOrElse(Redirect(controllers.rfm.routes.RfmJourneyRecoveryController.onPageLoad))

}
