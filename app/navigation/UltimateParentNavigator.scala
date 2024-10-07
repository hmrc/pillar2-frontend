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
class UltimateParentNavigator @Inject() {
  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }

  private lazy val reviewAndSubmitCheckYourAnswers = controllers.routes.CheckYourAnswersController.onPageLoad
  private lazy val upeCheckYourAnswers             = controllers.registration.routes.UpeCheckYourAnswersController.onPageLoad
  private val normalRoutes: Page => UserAnswers => Call = {
    case UpeRegisteredInUKPage    => domesticOrNotRoute
    case UpeNameRegistrationPage  => _ => controllers.registration.routes.UpeRegisteredAddressController.onPageLoad(NormalMode)
    case UpeRegisteredAddressPage => _ => controllers.registration.routes.UpeContactNameController.onPageLoad(NormalMode)
    case UpeContactNamePage       => _ => controllers.registration.routes.UpeContactEmailController.onPageLoad(NormalMode)
    case UpeContactEmailPage      => _ => controllers.registration.routes.ContactUPEByTelephoneController.onPageLoad(NormalMode)
    case UpePhonePreferencePage   => telephonePreferenceLogic
    case UpeCapturePhonePage      => _ => upeCheckYourAnswers
    case _                        => _ => routes.IndexController.onPageLoad
  }

  private def domesticOrNotRoute(userAnswers: UserAnswers) =
    userAnswers
      .get(UpeRegisteredInUKPage)
      .map { ukBased =>
        if (ukBased) {
          controllers.registration.routes.EntityTypeController.onPageLoad(NormalMode)
        } else {
          controllers.registration.routes.UpeNameRegistrationController.onPageLoad(NormalMode)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def telephonePreferenceLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(UpePhonePreferencePage)
      .map { provided =>
        if (provided) {
          controllers.registration.routes.CaptureTelephoneDetailsController.onPageLoad(NormalMode)
        } else {
          upeCheckYourAnswers
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private val checkRouteMap: Page => UserAnswers => Call = {
    case UpeRegisteredInUKPage  => domesticOrNotRoute
    case UpePhonePreferencePage => telephoneCheckRouteLogic
    case _                      => whichCheckYourAnswerPageContact
  }

  private def whichCheckYourAnswerPageContact(userAnswers: UserAnswers): Call =
    if (userAnswers.get(CheckYourAnswersLogicPage).isDefined) {
      reviewAndSubmitCheckYourAnswers
    } else {
      upeCheckYourAnswers
    }

  private def telephoneCheckRouteLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(UpePhonePreferencePage)
      .map { nominatedPhoneNumber =>
        if (nominatedPhoneNumber & userAnswers.get(UpeCapturePhonePage).isEmpty) {
          controllers.registration.routes.CaptureTelephoneDetailsController.onPageLoad(CheckMode)
        } else if (userAnswers.get(CheckYourAnswersLogicPage).isDefined) {
          reviewAndSubmitCheckYourAnswers
        } else {
          upeCheckYourAnswers
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

}
