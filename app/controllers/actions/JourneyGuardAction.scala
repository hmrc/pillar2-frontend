/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.actions

import models.repayments.RepaymentsStatus.SuccessfullyCompleted
import models.requests.SessionDataRequest
import pages.RepaymentsStatusPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JourneyGuardAction @Inject() (val executionContext: ExecutionContext) extends ActionFilter[SessionDataRequest] {

  override protected def filter[A](request: SessionDataRequest[A]): Future[Option[Result]] =
    Future.successful {
      val completed = request.userAnswers.get(RepaymentsStatusPage).contains(SuccessfullyCompleted)
      if completed then Some(Redirect(controllers.repayments.routes.RepaymentErrorReturnController.onPageLoad()))
      else None
    }
}
