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

  def nextPage(page: Page, clientPillar2Id: Option[String] = None, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(clientPillar2Id)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(clientPillar2Id)(userAnswers)
  }

  private val normalRoutes: Page => Option[String] => UserAnswers => Call = {
    case RepaymentsRefundAmountPage    => id => _ => controllers.repayments.routes.ReasonForRequestingRefundController.onPageLoad(id, NormalMode)
    case ReasonForRequestingRefundPage => id => _ => controllers.repayments.routes.UkOrAbroadBankAccountController.onPageLoad(id, NormalMode)
    case UkOrAbroadBankAccountPage     => id => data => ukOrAbroadBankAccountLogic(id, data)
    case BankAccountDetailsPage        => id => _ => controllers.repayments.routes.RepaymentsContactNameController.onPageLoad(id, NormalMode)
    case NonUKBankPage                 => id => _ => controllers.repayments.routes.RepaymentsContactNameController.onPageLoad(id, NormalMode)
    case RepaymentsContactNamePage     => id => _ => controllers.repayments.routes.RepaymentsContactEmailController.onPageLoad(id, NormalMode)
    case RepaymentsContactEmailPage    => id => _ => controllers.repayments.routes.RepaymentsContactByTelephoneController.onPageLoad(id, NormalMode)
    case RepaymentsContactByTelephonePage => id => data => telephonePreferenceNormalMode(id, data)
    case RepaymentsTelephoneDetailsPage   => id => _ => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad(id)
    case _                                => id => _ => controllers.repayments.routes.RequestRefundBeforeStartController.onPageLoad(id)
  }

  private def ukOrAbroadBankAccountLogic(maybeClientId: Option[String], userAnswers: UserAnswers): Call =
    userAnswers
      .get(UkOrAbroadBankAccountPage)
      .map { ukOrAbroad =>
        if (ukOrAbroad == UkOrAbroadBankAccount.UkBankAccount) {
          controllers.repayments.routes.BankAccountDetailsController.onPageLoad(clientPillar2Id = maybeClientId, mode = NormalMode)
        } else {
          controllers.repayments.routes.NonUKBankController.onPageLoad(clientPillar2Id = maybeClientId, mode = NormalMode)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private val checkRouteMap: Page => Option[String] => UserAnswers => Call = {
    case RepaymentsRefundAmountPage       => id => _ => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad(id)
    case ReasonForRequestingRefundPage    => id => _ => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad(id)
    case UkOrAbroadBankAccountPage        => id => data => ukOrAbroadBankAccountLogicCheckMode(id, data)
    case NonUKBankPage                    => id => _ => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad(id)
    case BankAccountDetailsPage           => id => _ => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad(id)
    case RepaymentsContactNamePage        => id => _ => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad(id)
    case RepaymentsContactEmailPage       => id => _ => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad(id)
    case RepaymentsContactByTelephonePage => id => data => telephonePreferenceCheckMode(id, data)
    case RepaymentsTelephoneDetailsPage   => id => _ => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad(id)
    case _                                => id => _ => controllers.repayments.routes.RequestRefundBeforeStartController.onPageLoad(id)
  }

  private def ukOrAbroadBankAccountLogicCheckMode(maybeClientId: Option[String], userAnswers: UserAnswers): Call =
    userAnswers
      .get(UkOrAbroadBankAccountPage)
      .map { ukOrAbroad =>
        if (ukOrAbroad == UkOrAbroadBankAccount.UkBankAccount) {
          userAnswers.get(BankAccountDetailsPage) match {
            case Some(value) => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad(clientPillar2Id = maybeClientId)
            case _ => controllers.repayments.routes.BankAccountDetailsController.onPageLoad(clientPillar2Id = maybeClientId, mode = CheckMode)
          }
        } else {
          userAnswers.get(NonUKBankPage) match {
            case Some(value) => controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad(clientPillar2Id = maybeClientId)
            case _           => controllers.repayments.routes.NonUKBankController.onPageLoad(clientPillar2Id = maybeClientId, mode = CheckMode)
          }
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def telephonePreferenceNormalMode(maybeClientId: Option[String], userAnswers: UserAnswers): Call =
    userAnswers
      .get(RepaymentsContactByTelephonePage)
      .map {
        case true =>
          controllers.repayments.routes.RepaymentsTelephoneDetailsController.onPageLoad(clientPillar2Id = maybeClientId, NormalMode)
        case false =>
          controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad(clientPillar2Id = maybeClientId)
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def telephonePreferenceCheckMode(maybeClientId: Option[String], userAnswers: UserAnswers): Call =
    userAnswers
      .get(RepaymentsContactByTelephonePage)
      .map { PhoneNumber =>
        if (PhoneNumber & userAnswers.get(RepaymentsTelephoneDetailsPage).isEmpty) {
          controllers.repayments.routes.RepaymentsTelephoneDetailsController.onPageLoad(clientPillar2Id = maybeClientId, CheckMode)
        } else {
          controllers.repayments.routes.RepaymentsCheckYourAnswersController.onPageLoad(clientPillar2Id = maybeClientId)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

}
