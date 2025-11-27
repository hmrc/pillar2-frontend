/*
 * Copyright 2025 HM Revenue & Customs
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
import controllers.actions.{IdentifierAction, SubscriptionDataRequiredAction, SubscriptionDataRetrievalAction}
import models.longrunningsubmissions.LongRunningSubmission.*
import models.longrunningsubmissions.{LongRunningSubmission, SubmissionLookupError, SubmissionState}
import models.requests.UserIdRequest
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.*
import services.LongRunningSubmissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.WaitingRoomView

import javax.inject.Named
import scala.concurrent.ExecutionContext

class WaitingRoomController @Inject() (
  val controllerComponents:                        MessagesControllerComponents,
  @Named("EnrolmentIdentifier") identifyEnrolment: IdentifierAction,
  identifyUserWithNoEnrolmentRestriction:          IdentifierAction,
  retrieveSubscription:                            SubscriptionDataRetrievalAction,
  requireSubscription:                             SubscriptionDataRequiredAction,
  longRunningSubmissionService:                    LongRunningSubmissionService,
  waitingRoomView:                                 WaitingRoomView
)(implicit
  ec:     ExecutionContext,
  config: FrontendAppConfig
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(submission: LongRunningSubmission): Action[AnyContent] =
    pageLoadAction(submission).async { implicit request =>
      longRunningSubmissionService
        .getCurrentState(submission)
        .map {
          case Right(SubmissionState.Submitted) =>
            logger.info(s"Submission $submission completed, redirecting to ${submission.submittedPage}")
            Redirect(submission.submittedPage)

          case Right(SubmissionState.Processing) =>
            val interval = config.longRunningSubmissionConfig(submission).pollingIntervalSeconds
            logger.info(
              s"Submission $submission processing, instructing redirection to ${routes.WaitingRoomController.onPageLoad(submission)} after $interval seconds."
            )
            Ok(waitingRoomView(viewmodels.WaitingRoom.fromLongRunningSubmission.apply(submission)))
              .withHeaders(
                "Refresh"       -> s"$interval, url=${routes.WaitingRoomController.onPageLoad(submission)}",
                "Cache-Control" -> "no-store, no-cache, must-revalidate",
                "Pragma"        -> "no-cache",
                "Expires"       -> "0"
              )

          case Right(error: SubmissionState.Error) =>
            logger.error(s"Failure to fetch current state of submission $submission due to $error")
            Redirect(submission.errorPage(Right(error)))

          case Left(error: SubmissionLookupError) =>
            logger.error(s"Failure to fetch current state of submission $submission due to $error")
            Redirect(submission.errorPage(Left(error)))
        }
    }

  private val pageLoadAction: LongRunningSubmission => ActionBuilder[UserIdRequest, AnyContent] = {
    case BTN                                                    => identifyEnrolment andThen retrieveSubscription andThen requireSubscription
    case ManageContactDetails | ManageGroupDetails | Repayments => identifyEnrolment andThen retrieveSubscription
    case Registration | RFM                                     => identifyUserWithNoEnrolmentRestriction
  }
}
