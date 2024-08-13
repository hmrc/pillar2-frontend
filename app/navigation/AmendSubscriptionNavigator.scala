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
import models.subscription.SubscriptionLocalData
import pages._
import play.api.mvc.Call
import javax.inject.{Inject, Singleton}

@Singleton
class AmendSubscriptionNavigator @Inject() {

  def nextPage(page: Page, subscriptionUserAnswers: SubscriptionLocalData): Call =
    checkRouteMap(page)(subscriptionUserAnswers)
  private lazy val groupDetailCheckYourAnswerRoute: Call =
    controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad
  private lazy val contactDetailCheckYourAnswersRoute: Call =
    controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad

  private val checkRouteMap: Page => SubscriptionLocalData => Call = {
    case SubMneOrDomesticPage            => _ => groupDetailCheckYourAnswerRoute
    case SubAccountingPeriodPage         => _ => groupDetailCheckYourAnswerRoute
    case SubPrimaryContactNamePage       => _ => contactDetailCheckYourAnswersRoute
    case SubPrimaryEmailPage             => _ => contactDetailCheckYourAnswersRoute
    case SubPrimaryPhonePreferencePage   => data => primaryTelephoneCheckRouteLogic(data)
    case SubPrimaryCapturePhonePage      => _ => contactDetailCheckYourAnswersRoute
    case SubAddSecondaryContactPage      => data => addSecondaryContactCheckRoute(data)
    case SubSecondaryContactNamePage     => data => emailOrCYA(data)
    case SubSecondaryEmailPage           => data => phonePrefOrCYA(data)
    case SubSecondaryPhonePreferencePage => data => secondaryTelephoneCheckRouteLogic(data)
    case SubSecondaryCapturePhonePage    => _ => contactDetailCheckYourAnswersRoute
    case SubRegisteredAddressPage        => _ => contactDetailCheckYourAnswersRoute
    case _                               => _ => routes.IndexController.onPageLoad
  }

  private def phonePrefOrCYA(subscriptionUserAnswers: SubscriptionLocalData): Call =
    subscriptionUserAnswers
      .get(SubAddSecondaryContactPage)
      .map { nominatedSecondaryContact =>
        if (nominatedSecondaryContact & subscriptionUserAnswers.get(SubSecondaryPhonePreferencePage).isEmpty) {
          controllers.subscription.manageAccount.routes.SecondaryTelephonePreferenceController.onPageLoad
        } else {
          contactDetailCheckYourAnswersRoute
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def emailOrCYA(subscriptionUserAnswers: SubscriptionLocalData): Call =
    subscriptionUserAnswers
      .get(SubAddSecondaryContactPage)
      .map { nominatedSecondaryContact =>
        if (nominatedSecondaryContact & subscriptionUserAnswers.get(SubSecondaryEmailPage).isEmpty) {
          controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad
        } else {
          contactDetailCheckYourAnswersRoute
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def addSecondaryContactCheckRoute(subscriptionUserAnswers: SubscriptionLocalData): Call =
    subscriptionUserAnswers
      .get(SubAddSecondaryContactPage)
      .map { nominatedSecondaryContact =>
        if (nominatedSecondaryContact & subscriptionUserAnswers.get(SubSecondaryContactNamePage).isEmpty) {
          controllers.subscription.manageAccount.routes.SecondaryContactNameController.onPageLoad
        } else {
          contactDetailCheckYourAnswersRoute
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def primaryTelephoneCheckRouteLogic(subscriptionUserAnswers: SubscriptionLocalData): Call =
    subscriptionUserAnswers
      .get(SubPrimaryPhonePreferencePage)
      .map { nominatedPhoneNumber =>
        if (nominatedPhoneNumber & subscriptionUserAnswers.get(SubPrimaryCapturePhonePage).isEmpty) {
          controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onPageLoad
        } else {
          contactDetailCheckYourAnswersRoute
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def secondaryTelephoneCheckRouteLogic(subscriptionUserAnswers: SubscriptionLocalData): Call =
    subscriptionUserAnswers
      .get(SubSecondaryPhonePreferencePage)
      .map { nominatedPhoneNumber =>
        if (nominatedPhoneNumber & subscriptionUserAnswers.get(SubSecondaryCapturePhonePage).isEmpty) {
          controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onPageLoad
        } else {
          contactDetailCheckYourAnswersRoute
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

}
