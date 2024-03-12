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
class NominatedFilingMemberNavigator @Inject() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case NominateFilingMemberPage => nfmLogic
    case FmRegisteredInUKPage     => domesticOrNotRoute
    case FmNameRegistrationPage   => _ => controllers.fm.routes.NfmRegisteredAddressController.onPageLoad(NormalMode)
    case FmRegisteredAddressPage  => _ => controllers.fm.routes.NfmContactNameController.onPageLoad(NormalMode)
    case FmContactNamePage        => _ => controllers.fm.routes.NfmEmailAddressController.onPageLoad(NormalMode)
    case FmContactEmailPage       => _ => controllers.fm.routes.ContactNfmByTelephoneController.onPageLoad(NormalMode)
    case FmPhonePreferencePage    => telephonePreferenceLogic
    case FmCapturePhonePage       => _ => controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad
    case _                        => _ => routes.IndexController.onPageLoad
  }

  private def nfmLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(NominateFilingMemberPage)
      .map { fmNominated =>
        if (fmNominated) {
          controllers.fm.routes.IsNfmUKBasedController.onPageLoad(NormalMode)
        } else {
          routes.TaskListController.onPageLoad
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())
  private def domesticOrNotRoute(userAnswers: UserAnswers): Call =
    userAnswers
      .get(FmRegisteredInUKPage)
      .map { ukBased =>
        if (ukBased) {
          controllers.fm.routes.NfmEntityTypeController.onPageLoad(NormalMode)
        } else {
          controllers.fm.routes.NfmNameRegistrationController.onPageLoad(NormalMode)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def telephonePreferenceLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(FmPhonePreferencePage)
      .map { provided =>
        if (provided) {
          controllers.fm.routes.NfmCaptureTelephoneDetailsController.onPageLoad(NormalMode)
        } else {
          controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private val checkRouteMap: Page => UserAnswers => Call = {
    case FmPhonePreferencePage => telephoneCheckRouteLogic
    case _                     => _ => controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad
  }

  private def telephoneCheckRouteLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(FmPhonePreferencePage)
      .map { nominatedPhoneNumber =>
        if (nominatedPhoneNumber) {
          controllers.fm.routes.NfmCaptureTelephoneDetailsController.onPageLoad(CheckMode)
        } else {
          controllers.fm.routes.NfmCheckYourAnswersController.onPageLoad
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}
