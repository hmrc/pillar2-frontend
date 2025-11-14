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

import base.SpecBase
import controllers.btn.routes.*
import controllers.routes
import models.*
import pages.*

class BTNNavigatorSpec extends SpecBase {

  val navigator = new BTNNavigator

  "BTN Navigator" when {

    "in Normal mode" when {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }

      "go from EntitiesInsideOutsideUKPage period page to CheckYourAnswersPage page when answer is Yes" in {
        navigator.nextPage(EntitiesInsideOutsideUKPage, NormalMode, emptyUserAnswers.setOrException(EntitiesInsideOutsideUKPage, true)) mustBe
          CheckYourAnswersController.onPageLoad
      }

      "go from EntitiesInsideOutsideUKPage period page to amend group details page when answer is No" in {
        navigator.nextPage(EntitiesInsideOutsideUKPage, NormalMode, emptyUserAnswers.setOrException(EntitiesInsideOutsideUKPage, false)) mustBe
          BTNEntitiesInsideOutsideUKController.onPageLoadAmendGroupDetails()
      }

    }

    "in Check mode" when {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }
    }
  }
}
