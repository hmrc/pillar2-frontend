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
import controllers.routes
import models._
import pages._

class RepaymentNavigatorSpec extends SpecBase {

  val navigator = new RepaymentNavigator

  private lazy val journeyRecovery   = routes.JourneyRecoveryController.onPageLoad()
  private lazy val underConstruction = routes.UnderConstructionController.onPageLoad
  "Navigator" when {

    "in Normal mode" must {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, None, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }
      "go to type of bank account page after submitting their reason for requesting a refund" in {
        navigator.nextPage(
          ReasonForRequestingRefundPage,
          None,
          NormalMode,
          emptyUserAnswers.setOrException(ReasonForRequestingRefundPage, "because")
        ) mustBe
          controllers.repayments.routes.UkOrAbroadBankAccountController.onPageLoad(None, NormalMode)
      }
      "go to under construction page if they choose a UK bank account" in {
        val userAnswers = emptyUserAnswers.setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.UkBankAccount)
        navigator.nextPage(UkOrAbroadBankAccountPage, None, NormalMode, userAnswers) mustBe underConstruction
      }
      "go to non-UK bank account page if they choose a non-UK bank account" in {
        val userAnswers = emptyUserAnswers.setOrException(UkOrAbroadBankAccountPage, UkOrAbroadBankAccount.ForeignBankAccount)
        navigator.nextPage(UkOrAbroadBankAccountPage, None, NormalMode, userAnswers) mustBe
          controllers.repayments.routes.NonUKBankController.onPageLoad(mode = NormalMode)
      }
      "go to journey recovery page if they somehow manage to submit an empty form" in {
        navigator.nextPage(UkOrAbroadBankAccountPage, None, NormalMode, emptyUserAnswers) mustBe journeyRecovery
      }

    }

    "in Check mode" must {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, None, CheckMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }

    }
  }
}
