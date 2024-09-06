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
import cats.data.OptionT.{fromOption, liftF}
import cats.implicits._
import config.FrontendAppConfig
import connectors.UserAnswersConnectors
import controllers.actions.{DataRequiredAction, DataRetrievalAction, FeatureFlagActionFactory, IdentifierAction}
import models.rfm.CorporatePosition
import models.rfm.RfmStatus._
import models.subscription.NewFilingMemberDetail
import models.{InternalIssueError, UnexpectedResponse, UserAnswers}
import pages.{PlrReferencePage, RfmStatusPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Writes
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.Settable
import repositories.SessionRepository
import services.SubscriptionService
import services.audit.AuditService
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
  auditService:                        AuditService,
  sessionRepository:                   SessionRepository,
  view:                                RfmContactCheckYourAnswersView,
  countryOptions:                      CountryOptions
)(implicit ec:                         ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (featureAction.rfmAccessAction andThen Identify andThen getData andThen requireData).async {
    implicit request =>
      implicit val userAnswers: UserAnswers = request.userAnswers
      val address = SummaryListViewModel(
        rows = Seq(RfmContactAddressSummary.row(request.userAnswers, countryOptions)).flatten
      )
      sessionRepository.get(request.userId).map { optionalUserAnswer =>
        (for {
          userAnswer <- optionalUserAnswer
          rfm        <- userAnswer.get(RfmStatusPage)
        } yield rfm) match {
          case Some(InProgress) => Redirect(controllers.rfm.routes.RfmWaitingRoomController.onPageLoad())
          case _ =>
            (for {
              userAnswer <- optionalUserAnswer
              _          <- userAnswer.get(PlrReferencePage)
            } yield Redirect(controllers.rfm.routes.RfmCannotReturnAfterConfirmationController.onPageLoad))
              .getOrElse {
                removeRfmStatus(userAnswers)
                Ok(
                  view(
                    request.userAnswers.rfmCorporatePositionSummaryList(countryOptions),
                    request.userAnswers.rfmPrimaryContactList,
                    request.userAnswers.rfmSecondaryContactList,
                    address
                  )
                )
              }
        }
      }
  }

  def onSubmit(): Action[AnyContent] = (rfmIdentify andThen getData andThen requireData).async { implicit request =>
    if (request.userAnswers.isRfmJourneyCompleted) {
      updateSessionData(request.userAnswers, RfmStatusPage, InProgress)
      val rfmStatus = (for {
        newFilingMemberInformation <- fromOption[Future](request.userAnswers.getNewFilingMemberDetail)
        subscriptionData           <- liftF(subscriptionService.readSubscription(newFilingMemberInformation.plrReference))
        amendData <-
          liftF(
            subscriptionService.createAmendObjectForReplacingFilingMember(subscriptionData, newFilingMemberInformation, request.userAnswers)
          )
        _ <- liftF(subscriptionService.amendFilingMemberDetails(request.userAnswers.id, amendData))
        _ <- liftF(subscriptionService.deallocateEnrolment(newFilingMemberInformation.plrReference))
        upeEnrolmentInfo <- liftF(
                              subscriptionService.getUltimateParentEnrolmentInformation(
                                subscriptionData = subscriptionData,
                                pillar2Reference = newFilingMemberInformation.plrReference,
                                request.userIdForEnrolment
                              )
                            )
        groupId <- fromOption[Future](request.groupId)
        _ <- liftF(subscriptionService.allocateEnrolment(groupId = groupId, plrReference = newFilingMemberInformation.plrReference, upeEnrolmentInfo))
        _ <- liftF(userAnswersConnectors.remove(request.userId))
        _ <- liftF(updateSessionData(request.userAnswers, PlrReferencePage, newFilingMemberInformation.plrReference))
        _ <- if (isNewNfmNonUkBased(newFilingMemberInformation)) { liftF(auditService.auditReplaceFilingMember(newFilingMemberInformation)) }
             else { liftF(Future.unit) }
      } yield SuccessfullyCompleted).value
        .flatMap {
          case Some(result) => Future.successful(result)
          case _            => Future.successful(FailException)
        }
        .recover {
          case InternalIssueError | UnexpectedResponse => FailedInternalIssueError
          case _: Exception => FailException
        }
      for {
        updatedRfmStatus    <- rfmStatus
        optionalSessionData <- sessionRepository.get(request.userAnswers.id)
        sessionData = optionalSessionData.getOrElse(UserAnswers(request.userId))
        updatedSessionData <- Future.fromTry(sessionData.set(RfmStatusPage, updatedRfmStatus))
        _                  <- sessionRepository.set(updatedSessionData)
      } yield (): Unit
      Future.successful(Redirect(controllers.rfm.routes.RfmWaitingRoomController.onPageLoad()))
    } else {
      Future.successful(Redirect(controllers.rfm.routes.RfmIncompleteDataController.onPageLoad))
    }

  }

  private def isNewNfmNonUkBased(newFilingMemberInformation: NewFilingMemberDetail): Boolean =
    (newFilingMemberInformation.corporatePosition, newFilingMemberInformation.ukBased) match {
      case (CorporatePosition.NewNfm, Some(false)) => true
      case (_, _)                                  => false
    }

  private def removeRfmStatus(userAnswers: UserAnswers): Future[Unit] =
    for {
      optionalSessionData <- sessionRepository.get(userAnswers.id)
      sessionData = optionalSessionData.getOrElse(UserAnswers(userAnswers.id))
      updatedSessionData <- Future.fromTry(sessionData.remove(RfmStatusPage))
      _                  <- sessionRepository.set(updatedSessionData)
    } yield (): Unit

  private def updateSessionData[A](userAnswers: UserAnswers, page: Settable[A], value: A)(implicit writes: Writes[A]): Future[Unit] =
    for {
      optionalSessionData <- sessionRepository.get(userAnswers.id)
      sessionData = optionalSessionData.getOrElse(UserAnswers(userAnswers.id))
      updatedSessionData <- Future.fromTry(sessionData.set(page, value))
      _                  <- sessionRepository.set(updatedSessionData)
    } yield (): Unit

}
