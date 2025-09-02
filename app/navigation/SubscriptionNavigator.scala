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
class SubscriptionNavigator @Inject() {

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
  private lazy val groupDetailCheckYourAnswerRoute: Call = controllers.subscription.routes.GroupDetailCheckYourAnswersController.onPageLoad
  private lazy val contactDetailCheckYourAnswersRoute = controllers.subscription.routes.ContactCheckYourAnswersController.onPageLoad
  private lazy val reviewAndSubmitCheckYourAnswers    = controllers.routes.CheckYourAnswersController.onPageLoad

  private val normalRoutes: Page => UserAnswers => Call = {
    case SubMneOrDomesticPage            => _ => controllers.subscription.routes.GroupAccountingPeriodController.onPageLoad(NormalMode)
    case SubAccountingPeriodPage         => _ => groupDetailCheckYourAnswerRoute
    case SubUsePrimaryContactPage        => usePrimaryContactRoute
    case SubPrimaryContactNamePage       => _ => controllers.subscription.routes.ContactEmailAddressController.onPageLoad(NormalMode)
    case SubPrimaryEmailPage             => _ => controllers.subscription.routes.ContactByPhoneController.onPageLoad(NormalMode)
    case SubPrimaryPhonePreferencePage   => primaryPhonePreference
    case SubPrimaryCapturePhonePage      => _ => controllers.subscription.routes.AddSecondaryContactController.onPageLoad(NormalMode)
    case SubAddSecondaryContactPage      => addSecondaryContactRoute
    case SubSecondaryContactNamePage     => _ => controllers.subscription.routes.SecondaryContactEmailController.onPageLoad(NormalMode)
    case SubSecondaryEmailPage           => _ => controllers.subscription.routes.SecondaryPhonePreferenceController.onPageLoad(NormalMode)
    case SubSecondaryPhonePreferencePage => secondaryPhonePreference
    case SubSecondaryCapturePhonePage    => _ => controllers.subscription.routes.CaptureSubscriptionAddressController.onPageLoad(NormalMode)
    case SubRegisteredAddressPage        => _ => contactDetailCheckYourAnswersRoute
    case _                               => _ => routes.IndexController.onPageLoad
  }

  private def usePrimaryContactRoute(userAnswers: UserAnswers): Call =
    userAnswers
      .get(SubUsePrimaryContactPage)
      .map { accepted =>
        if (accepted) {
          controllers.subscription.routes.AddSecondaryContactController.onPageLoad(NormalMode)
        } else {
          controllers.subscription.routes.ContactNameComplianceController.onPageLoad(NormalMode)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def addSecondaryContactRoute(userAnswers: UserAnswers): Call =
    userAnswers
      .get(SubAddSecondaryContactPage)
      .map { nominated =>
        if (nominated) {
          controllers.subscription.routes.SecondaryContactNameController.onPageLoad(NormalMode)
        } else {
          controllers.subscription.routes.CaptureSubscriptionAddressController.onPageLoad(NormalMode)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def primaryPhonePreference(userAnswers: UserAnswers): Call =
    userAnswers
      .get(SubPrimaryPhonePreferencePage)
      .map { provided =>
        if (provided) {
          controllers.subscription.routes.ContactCapturePhoneDetailsController.onPageLoad(NormalMode)
        } else {
          controllers.subscription.routes.AddSecondaryContactController.onPageLoad(NormalMode)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def secondaryPhonePreference(userAnswers: UserAnswers): Call =
    userAnswers
      .get(SubSecondaryPhonePreferencePage)
      .map { provided =>
        if (provided) {
          controllers.subscription.routes.SecondaryPhoneController.onPageLoad(NormalMode)
        } else {
          controllers.subscription.routes.CaptureSubscriptionAddressController.onPageLoad(NormalMode)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private val checkRouteMap: Page => UserAnswers => Call = {
    case SubMneOrDomesticPage            => whichCheckYourAnswerPageGroup
    case SubAccountingPeriodPage         => whichCheckYourAnswerPageGroup
    case SubPrimaryContactNamePage       => whichCheckYourAnswerPageContact
    case SubPrimaryEmailPage             => whichCheckYourAnswerPageContact
    case SubPrimaryPhonePreferencePage   => primaryPhoneCheckRouteLogic
    case SubPrimaryCapturePhonePage      => whichCheckYourAnswerPageContact
    case SubAddSecondaryContactPage      => addSecondaryContactCheckRoute
    case SubSecondaryContactNamePage     => secondaryContactNameRoute
    case SubSecondaryEmailPage           => secondaryContactEmailRoute
    case SubSecondaryPhonePreferencePage => secondaryPhoneCheckRouteLogic
    case SubSecondaryCapturePhonePage    => whichCheckYourAnswerPageContact
    case SubRegisteredAddressPage        => whichCheckYourAnswerPageContact
    case _                               => whichCheckYourAnswerPageContact
  }

  private def whichCheckYourAnswerPageContact(userAnswers: UserAnswers): Call =
    if (userAnswers.get(CheckYourAnswersLogicPage).isDefined) {
      reviewAndSubmitCheckYourAnswers
    } else {
      contactDetailCheckYourAnswersRoute
    }

  private def whichCheckYourAnswerPageGroup(userAnswers: UserAnswers): Call =
    if (userAnswers.get(CheckYourAnswersLogicPage).isDefined) reviewAndSubmitCheckYourAnswers else groupDetailCheckYourAnswerRoute

  private def secondaryContactNameRoute(userAnswers: UserAnswers): Call =
    if (!userAnswers.finalStatusCheck) {
      controllers.subscription.routes.SecondaryContactEmailController.onPageLoad(CheckMode)
    } else if (userAnswers.get(CheckYourAnswersLogicPage).isDefined) {
      reviewAndSubmitCheckYourAnswers
    } else {
      contactDetailCheckYourAnswersRoute
    }

  private def secondaryContactEmailRoute(userAnswers: UserAnswers): Call =
    if (!userAnswers.finalStatusCheck) {
      controllers.subscription.routes.SecondaryPhonePreferenceController.onPageLoad(CheckMode)
    } else if (userAnswers.get(CheckYourAnswersLogicPage).isDefined) {
      reviewAndSubmitCheckYourAnswers
    } else {
      contactDetailCheckYourAnswersRoute
    }

  private def addSecondaryContactCheckRoute(userAnswers: UserAnswers): Call =
    userAnswers
      .get(SubAddSecondaryContactPage)
      .map { nominatedSecondaryContact =>
        if (nominatedSecondaryContact & userAnswers.get(SubSecondaryContactNamePage).isEmpty) {
          controllers.subscription.routes.SecondaryContactNameController.onPageLoad(CheckMode)
        } else if (userAnswers.get(CheckYourAnswersLogicPage).isDefined) {
          reviewAndSubmitCheckYourAnswers
        } else {
          contactDetailCheckYourAnswersRoute
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def primaryPhoneCheckRouteLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(SubPrimaryPhonePreferencePage)
      .map { nominatedPhoneNumber =>
        if (nominatedPhoneNumber & userAnswers.get(SubPrimaryCapturePhonePage).isEmpty) {
          controllers.subscription.routes.ContactCapturePhoneDetailsController.onPageLoad(CheckMode)
        } else if (userAnswers.get(CheckYourAnswersLogicPage).isDefined) {
          reviewAndSubmitCheckYourAnswers
        } else {
          contactDetailCheckYourAnswersRoute
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def secondaryPhoneCheckRouteLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(SubSecondaryPhonePreferencePage)
      .map { nominatedPhoneNumber =>
        if (nominatedPhoneNumber & userAnswers.get(SubSecondaryCapturePhonePage).isEmpty) {
          controllers.subscription.routes.SecondaryPhoneController.onPageLoad(CheckMode)
        } else if (userAnswers.get(CheckYourAnswersLogicPage).isDefined) {
          reviewAndSubmitCheckYourAnswers
        } else {
          contactDetailCheckYourAnswersRoute
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

}
