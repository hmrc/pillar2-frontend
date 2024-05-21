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
import models.subscription.SubscriptionLocalData
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class AmendSubscriptionNavigator @Inject() {

  def nextPage(page: Page, clientPillar2Id: Option[String] = None, subscriptionUserAnswers: SubscriptionLocalData): Call =
    checkRouteMap(page)(clientPillar2Id)(subscriptionUserAnswers)
  private lazy val groupDetailCheckYourAnswerRoute: Option[String] => Call = (maybeClientId: Option[String]) =>
    controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad(maybeClientId)
  private lazy val contactDetailCheckYourAnswersRoute: Option[String] => Call = (maybeClientId: Option[String]) =>
    controllers.subscription.manageAccount.routes.ManageContactCheckYourAnswersController.onPageLoad(maybeClientId)

  private val checkRouteMap: Page => Option[String] => SubscriptionLocalData => Call = {
    case SubMneOrDomesticPage            => id => _ => groupDetailCheckYourAnswerRoute(id)
    case SubAccountingPeriodPage         => id => _ => groupDetailCheckYourAnswerRoute(id)
    case SubPrimaryContactNamePage       => id => _ => contactDetailCheckYourAnswersRoute(id)
    case SubPrimaryEmailPage             => id => _ => contactDetailCheckYourAnswersRoute(id)
    case SubPrimaryPhonePreferencePage   => id => data => primaryTelephoneCheckRouteLogic(id, data)
    case SubPrimaryCapturePhonePage      => id => _ => contactDetailCheckYourAnswersRoute(id)
    case SubAddSecondaryContactPage      => id => data => addSecondaryContactCheckRoute(id, data)
    case SubSecondaryContactNamePage     => id => data => emailOrCYA(id, data)
    case SubSecondaryEmailPage           => id => data => phonePrefOrCYA(id, data)
    case SubSecondaryPhonePreferencePage => id => data => secondaryTelephoneCheckRouteLogic(id, data)
    case SubSecondaryCapturePhonePage    => id => _ => contactDetailCheckYourAnswersRoute(id)
    case SubRegisteredAddressPage        => id => _ => contactDetailCheckYourAnswersRoute(id)
    case _                               => _ => _ => routes.IndexController.onPageLoad
  }

  private def phonePrefOrCYA(maybeClientId: Option[String], subscriptionUserAnswers: SubscriptionLocalData): Call =
    subscriptionUserAnswers
      .get(SubAddSecondaryContactPage)
      .map { nominatedSecondaryContact =>
        if (nominatedSecondaryContact & subscriptionUserAnswers.get(SubSecondaryPhonePreferencePage).isEmpty) {
          controllers.subscription.manageAccount.routes.SecondaryTelephonePreferenceController.onPageLoad()
        } else {
          contactDetailCheckYourAnswersRoute(maybeClientId)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def emailOrCYA(maybeClientId: Option[String], subscriptionUserAnswers: SubscriptionLocalData): Call =
    subscriptionUserAnswers
      .get(SubAddSecondaryContactPage)
      .map { nominatedSecondaryContact =>
        if (nominatedSecondaryContact & subscriptionUserAnswers.get(SubSecondaryEmailPage).isEmpty) {
          controllers.subscription.manageAccount.routes.SecondaryContactEmailController.onPageLoad()
        } else {
          contactDetailCheckYourAnswersRoute(maybeClientId)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def addSecondaryContactCheckRoute(maybeClientId: Option[String], subscriptionUserAnswers: SubscriptionLocalData): Call =
    subscriptionUserAnswers
      .get(SubAddSecondaryContactPage)
      .map { nominatedSecondaryContact =>
        if (nominatedSecondaryContact & subscriptionUserAnswers.get(SubSecondaryContactNamePage).isEmpty) {
          controllers.subscription.manageAccount.routes.SecondaryContactNameController.onPageLoad()
        } else {
          contactDetailCheckYourAnswersRoute(maybeClientId)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def primaryTelephoneCheckRouteLogic(maybeClientId: Option[String], subscriptionUserAnswers: SubscriptionLocalData): Call =
    subscriptionUserAnswers
      .get(SubPrimaryPhonePreferencePage)
      .map { nominatedPhoneNumber =>
        if (nominatedPhoneNumber & subscriptionUserAnswers.get(SubPrimaryCapturePhonePage).isEmpty) {
          controllers.subscription.manageAccount.routes.ContactCaptureTelephoneDetailsController.onPageLoad()
        } else {
          contactDetailCheckYourAnswersRoute(maybeClientId)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def secondaryTelephoneCheckRouteLogic(maybeClientId: Option[String], subscriptionUserAnswers: SubscriptionLocalData): Call =
    subscriptionUserAnswers
      .get(SubSecondaryPhonePreferencePage)
      .map { nominatedPhoneNumber =>
        if (nominatedPhoneNumber & subscriptionUserAnswers.get(SubSecondaryCapturePhonePage).isEmpty) {
          controllers.subscription.manageAccount.routes.SecondaryTelephoneController.onPageLoad()
        } else {
          contactDetailCheckYourAnswersRoute(maybeClientId)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

}
