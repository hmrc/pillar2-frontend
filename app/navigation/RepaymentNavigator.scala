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

import controllers.routes
import models._
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class RepaymentNavigator @Inject() {

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }

  private val normalRoutes: Page => UserAnswers => Call = {
    case RepaymentsRefundAmountPage    => _ => controllers.repayments.routes.ReasonForRequestingRefundController.onPageLoad(NormalMode)
    case ReasonForRequestingRefundPage => _ => controllers.repayments.routes.UkOrAbroadBankAccountController.onPageLoad(NormalMode)
    case UkOrAbroadBankAccountPage     => data => ukOrAbroadBankAccountLogic(data)
    case NonUKBankPage                 => _ => controllers.repayments.routes.RepaymentsContactNameController.onPageLoad(NormalMode)
    case RepaymentsContactNamePage     => _ => controllers.repayments.routes.RepaymentsContactEmailController.onPageLoad(NormalMode)
    case RepaymentsContactEmailPage    => _ => routes.UnderConstructionController.onPageLoad
    case _                             => _ => routes.IndexController.onPageLoad
  }

  private def ukOrAbroadBankAccountLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(UkOrAbroadBankAccountPage)
      .map { ukOrAbroad =>
        if (ukOrAbroad == UkOrAbroadBankAccount.UkBankAccount) {
          routes.UnderConstructionController.onPageLoad
        } else {
          controllers.repayments.routes.NonUKBankController.onPageLoad(mode = NormalMode)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private val checkRouteMap: Page => UserAnswers => Call = _ => _ => routes.IndexController.onPageLoad

}
