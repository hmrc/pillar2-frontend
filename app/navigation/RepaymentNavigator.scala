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
    case RepaymentsRefundAmountPage           => _ => controllers.repayments.routes.ReasonForRequestingRepaymentController.onPageLoad(NormalMode)
    case ReasonForRequestingRefundPage        => _ => controllers.repayments.routes.UkOrAbroadBankAccountController.onPageLoad(NormalMode)
    case UkOrAbroadBankAccountPage            => data => ukOrAbroadBankAccountLogic(data)
    case BankAccountDetailsPage               => _ => controllers.repayments.routes.RepaymentsContactNameController.onPageLoad(NormalMode)
    case NonUKBankPage                        => _ => controllers.repayments.routes.RepaymentsContactNameController.onPageLoad(NormalMode)
    case RepaymentsContactNamePage            => _ => controllers.repayments.routes.RepaymentsContactEmailController.onPageLoad(NormalMode)
    case RepaymentsContactEmailPage           => _ => controllers.repayments.routes.RepaymentsContactByTelephoneController.onPageLoad(NormalMode)
    case RepaymentsContactByTelephonePage     => data => telephonePreferenceNormalMode(data)
    case RepaymentsTelephoneDetailsPage       => _ => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad
    case RepaymentAccountNameConfirmationPage => data => accountNamePartialRoute(data, NormalMode)
    case BarsAccountNamePartialPage           => _ => controllers.repayments.routes.RepaymentErrorController.onPageLoadPartialNameError(NormalMode)
    case _                                    => _ => controllers.repayments.routes.RequestRepaymentBeforeStartController.onPageLoad
  }

  private def ukOrAbroadBankAccountLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(UkOrAbroadBankAccountPage)
      .map { ukOrAbroad =>
        if (ukOrAbroad == UkOrAbroadBankAccount.UkBankAccount) {
          controllers.repayments.routes.BankAccountDetailsController.onPageLoad(mode = NormalMode)
        } else {
          controllers.repayments.routes.NonUKBankController.onPageLoad(mode = NormalMode)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def accountNamePartialRoute(userAnswers: UserAnswers, mode: Mode): Call =
    userAnswers
      .get(RepaymentAccountNameConfirmationPage)
      .map { isCorrectAccountName =>
        if (isCorrectAccountName) {
          if (mode == NormalMode) {
            controllers.repayments.routes.RepaymentsContactNameController.onPageLoad(mode)
          } else { controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad }
        } else {
          controllers.repayments.routes.BankAccountDetailsController.onPageLoad(mode)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private val checkRouteMap: Page => UserAnswers => Call = {
    case RepaymentsRefundAmountPage           => _ => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad
    case ReasonForRequestingRefundPage        => _ => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad
    case UkOrAbroadBankAccountPage            => data => ukOrAbroadBankAccountLogicCheckMode(data)
    case NonUKBankPage                        => _ => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad
    case BankAccountDetailsPage               => _ => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad
    case RepaymentsContactNamePage            => _ => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad
    case RepaymentsContactEmailPage           => _ => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad
    case RepaymentsContactByTelephonePage     => data => telephonePreferenceCheckMode(data)
    case RepaymentsTelephoneDetailsPage       => _ => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad
    case RepaymentAccountNameConfirmationPage => data => accountNamePartialRoute(data, CheckMode)
    case BarsAccountNamePartialPage           => _ => controllers.repayments.routes.RepaymentErrorController.onPageLoadPartialNameError(CheckMode)
    case _                                    => _ => controllers.repayments.routes.RequestRepaymentBeforeStartController.onPageLoad
  }

  private def ukOrAbroadBankAccountLogicCheckMode(userAnswers: UserAnswers): Call =
    userAnswers
      .get(UkOrAbroadBankAccountPage)
      .map { ukOrAbroad =>
        if (ukOrAbroad == UkOrAbroadBankAccount.UkBankAccount) {
          userAnswers.get(BankAccountDetailsPage) match {
            case Some(_) => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad
            case _       => controllers.repayments.routes.BankAccountDetailsController.onPageLoad(mode = CheckMode)
          }
        } else {
          userAnswers.get(NonUKBankPage) match {
            case Some(_) => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad
            case _       => controllers.repayments.routes.NonUKBankController.onPageLoad(mode = CheckMode)
          }
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def telephonePreferenceNormalMode(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RepaymentsContactByTelephonePage)
      .map {
        case true =>
          controllers.repayments.routes.RepaymentsTelephoneDetailsController.onPageLoad(NormalMode)
        case false =>
          controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def telephonePreferenceCheckMode(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RepaymentsContactByTelephonePage)
      .map { PhoneNumber =>
        if (PhoneNumber & userAnswers.get(RepaymentsTelephoneDetailsPage).isEmpty) {
          controllers.repayments.routes.RepaymentsTelephoneDetailsController.onPageLoad(CheckMode)
        } else {
          controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

}
