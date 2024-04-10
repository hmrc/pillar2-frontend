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
class AmendSubscriptionNavigator @Inject() {

  def nextPage(page: Page, mode: Mode = CheckMode, userAnswers: UserAnswers): Call = checkRouteMap(page)(userAnswers)
  private lazy val groupDetailCheckYourAnswerRoute: Call =
    controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad
  private lazy val contactDetailCheckYourAnswersRoute =
    controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad

  private val checkRouteMap: Page => UserAnswers => Call = {
    case SubMneOrDomesticPage            => _ => groupDetailCheckYourAnswerRoute
    case SubAccountingPeriodPage         => _ => groupDetailCheckYourAnswerRoute
    case SubPrimaryContactNamePage       => _ => contactDetailCheckYourAnswersRoute
    case SubPrimaryEmailPage             => _ => contactDetailCheckYourAnswersRoute
    case SubPrimaryPhonePreferencePage   => primaryTelephoneCheckRouteLogic
    case SubPrimaryCapturePhonePage      => _ => contactDetailCheckYourAnswersRoute
    case SubAddSecondaryContactPage      => addSecondaryContactCheckRoute
    case SubSecondaryContactNamePage     => emailOrCYA
    case SubSecondaryEmailPage           => phonePrefOrCYA
    case SubSecondaryPhonePreferencePage => secondaryTelephoneCheckRouteLogic
    case SubSecondaryCapturePhonePage    => _ => contactDetailCheckYourAnswersRoute
    case SubRegisteredAddressPage        => _ => contactDetailCheckYourAnswersRoute
    case _                               => _ => routes.IndexController.onPageLoad
  }

  private def phonePrefOrCYA(userAnswers: UserAnswers): Call =
    userAnswers
      .get(SubAddSecondaryContactPage)
      .map { nominatedSecondaryContact =>
        if (nominatedSecondaryContact & userAnswers.get(SubSecondaryPhonePreferencePage).isEmpty) {
          controllers.subscription.manageAccount.routes.SecondaryTelephonePreferenceController.onPageLoad()
        } else {
          contactDetailCheckYourAnswersRoute
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def emailOrCYA(userAnswers: UserAnswers): Call =
    userAnswers
      .get(SubAddSecondaryContactPage)
      .map { nominatedSecondaryContact =>
        if (nominatedSecondaryContact & userAnswers.get(SubSecondaryEmailPage).isEmpty) {
          controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad()
        } else {
          contactDetailCheckYourAnswersRoute
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def addSecondaryContactCheckRoute(userAnswers: UserAnswers): Call =
    userAnswers
      .get(SubAddSecondaryContactPage)
      .map { nominatedSecondaryContact =>
        if (nominatedSecondaryContact & userAnswers.get(SubSecondaryContactNamePage).isEmpty) {
          controllers.subscription.manageAccount.routes.SecondaryContactNameController.onPageLoad()
        } else {
          contactDetailCheckYourAnswersRoute
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def primaryTelephoneCheckRouteLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(SubPrimaryPhonePreferencePage)
      .map { nominatedPhoneNumber =>
        if (nominatedPhoneNumber & userAnswers.get(SubPrimaryCapturePhonePage).isEmpty) {
          controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onPageLoad()
        } else {
          contactDetailCheckYourAnswersRoute
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def secondaryTelephoneCheckRouteLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(SubSecondaryPhonePreferencePage)
      .map { nominatedPhoneNumber =>
        if (nominatedPhoneNumber & userAnswers.get(SubSecondaryCapturePhonePage).isEmpty) {
          controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onPageLoad()
        } else {
          contactDetailCheckYourAnswersRoute
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

}
