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

package navigation

import controllers.btn.routes._
import controllers.routes.{IndexController, JourneyRecoveryController}
import models._
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class BTNNavigator @Inject() {

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = page match {
    case booleanPage: QuestionPage[Boolean] => booleanNavigator(booleanPage, mode, userAnswers)
    case _ => IndexController.onPageLoad
  }

  private def booleanNavigator(page: QuestionPage[Boolean], mode: Mode, userAnswers: UserAnswers): Call = {
    val (yesRoute, noRoute) = page match {
      case EntitiesInsideOutsideUKPage =>
        (CheckYourAnswersController.onPageLoad, BTNEntitiesInsideOutsideUKController.onPageLoadAmendGroupDetails())
      case _ => (IndexController.onPageLoad, IndexController.onPageLoad)
    }

    userAnswers
      .get(page)
      .map(provided => if (provided) yesRoute else noRoute)
      .getOrElse(JourneyRecoveryController.onPageLoad())
  }
}
