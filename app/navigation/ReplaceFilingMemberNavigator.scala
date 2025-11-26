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
import models.*
import models.rfm.CorporatePosition
import pages.*
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class ReplaceFilingMemberNavigator @Inject() {
  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }

  private lazy val reviewAndSubmitCheckYourAnswers   = controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad
  private lazy val securityQuestionsCheckYourAnswers = controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad(CheckMode)
  private lazy val rfmCheckYourAnswers               = controllers.rfm.routes.RfmCheckYourAnswersController.onPageLoad(NormalMode)

  private val normalRoutes: Page => UserAnswers => Call = {

    case RfmPrimaryContactNamePage       => _ => controllers.rfm.routes.RfmPrimaryContactEmailController.onPageLoad(NormalMode)
    case RfmPrimaryContactEmailPage      => _ => controllers.rfm.routes.RfmContactByPhoneController.onPageLoad(NormalMode)
    case RfmContactByPhonePage           => phonePreferenceLogicNormal
    case RfmCapturePrimaryPhonePage      => _ => controllers.rfm.routes.RfmAddSecondaryContactController.onPageLoad(NormalMode)
    case RfmCorporatePositionPage        => rfmCorporatePosition
    case RfmUkBasedPage                  => rfmUkBasedLogic
    case RfmAddSecondaryContactPage      => rfmAddSecondaryContactRouteNormal
    case RfmSecondaryContactNamePage     => _ => controllers.rfm.routes.RfmSecondaryContactEmailController.onPageLoad(NormalMode)
    case RfmSecondaryEmailPage           => _ => controllers.rfm.routes.RfmSecondaryPhonePreferenceController.onPageLoad(NormalMode)
    case RfmSecondaryPhonePreferencePage => rfmSecondaryPhonePreferenceNormal
    case RfmSecondaryCapturePhonePage    => _ => controllers.rfm.routes.RfmContactAddressController.onPageLoad(NormalMode)
    case RfmCheckYourAnswersPage         => rfmRegistrationDetailsCheckRoute
    case RfmContactAddressPage           => _ => controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad
    case RfmPillar2ReferencePage         => _ => controllers.rfm.routes.GroupRegistrationDateReportController.onPageLoad(NormalMode)
    case RfmRegistrationDatePage         => _ => controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad(NormalMode)
    case RfmNameRegistrationPage         => _ => controllers.rfm.routes.RfmRegisteredAddressController.onPageLoad(NormalMode)
    case RfmRegisteredAddressPage        => _ => controllers.rfm.routes.RfmCheckYourAnswersController.onPageLoad(NormalMode)
    case _                               => _ => controllers.rfm.routes.StartPageController.onPageLoad
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case RfmPillar2ReferencePage         => _ => securityQuestionsCheckYourAnswers
    case RfmRegistrationDatePage         => _ => securityQuestionsCheckYourAnswers
    case RfmNameRegistrationPage         => whichCheckYourAnswerPageRfmQuestions
    case RfmRegisteredAddressPage        => whichCheckYourAnswerPageRfmQuestions
    case RfmCorporatePositionPage        => rfmCorporatePositionCheckRouteLogic
    case RfmUkBasedPage                  => rfmUkBasedCheckRouteLogic
    case RfmPrimaryContactNamePage       => _ => controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad
    case RfmPrimaryContactEmailPage      => _ => controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad
    case RfmContactByPhonePage           => phonePreferenceLogicCheck
    case RfmCapturePrimaryPhonePage      => _ => controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad
    case RfmAddSecondaryContactPage      => rfmAddSecondaryContactRouteCheck
    case RfmSecondaryContactNamePage     => rfmSecondaryContactCheck
    case RfmSecondaryEmailPage           => rfmSecondaryEmailCheck
    case RfmSecondaryPhonePreferencePage => rfmSecondaryPhonePreferenceCheck
    case RfmSecondaryCapturePhonePage    => _ => controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad
    case RfmContactAddressPage           => _ => controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad
    case RfmCheckYourAnswersPage         => _ => controllers.rfm.routes.RfmContactDetailsRegistrationController.onPageLoad()
    case _                               => _ => controllers.rfm.routes.StartPageController.onPageLoad
  }

  private def whichCheckYourAnswerPageRfmQuestions(userAnswers: UserAnswers): Call =
    userAnswers.get(RfmContactAddressPage) match {
      case Some(value) => reviewAndSubmitCheckYourAnswers
      case _           => rfmCheckYourAnswers
    }

  private def rfmRegistrationDetailsCheckRoute(userAnswers: UserAnswers): Call =
    userAnswers.get(RfmContactAddressPage) match {
      case Some(value) => reviewAndSubmitCheckYourAnswers
      case _           => controllers.rfm.routes.RfmContactDetailsRegistrationController.onPageLoad()
    }

  private def phonePreferenceLogicNormal(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmContactByPhonePage)
      .map {
        case true =>
          controllers.rfm.routes.RfmCapturePrimaryPhoneController.onPageLoad(NormalMode)
        case false =>
          controllers.rfm.routes.RfmAddSecondaryContactController.onPageLoad(NormalMode)
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def phonePreferenceLogicCheck(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmContactByPhonePage)
      .map {
        case true  => controllers.rfm.routes.RfmCapturePrimaryPhoneController.onPageLoad(CheckMode)
        case false => controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def rfmAddSecondaryContactRouteNormal(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmAddSecondaryContactPage)
      .map {
        case true =>
          controllers.rfm.routes.RfmSecondaryContactNameController.onPageLoad(NormalMode)
        case false =>
          controllers.rfm.routes.RfmContactAddressController.onPageLoad(NormalMode)
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def rfmAddSecondaryContactRouteCheck(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmAddSecondaryContactPage)
      .map {
        case true  => controllers.rfm.routes.RfmSecondaryContactNameController.onPageLoad(CheckMode)
        case false => controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def rfmSecondaryPhonePreferenceNormal(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmSecondaryPhonePreferencePage)
      .map {
        case true =>
          controllers.rfm.routes.RfmSecondaryPhoneController.onPageLoad(NormalMode)
        case false =>
          controllers.rfm.routes.RfmContactAddressController.onPageLoad(NormalMode)
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def rfmSecondaryPhonePreferenceCheck(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmSecondaryPhonePreferencePage)
      .map {
        case true  => controllers.rfm.routes.RfmSecondaryPhoneController.onPageLoad(CheckMode)
        case false => controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def rfmSecondaryContactCheck(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmAddSecondaryContactPage)
      .map {
        case true if userAnswers.get(RfmSecondaryEmailPage).isEmpty =>
          controllers.rfm.routes.RfmSecondaryContactEmailController.onPageLoad(CheckMode)
        case _ => controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def rfmSecondaryEmailCheck(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmSecondaryEmailPage) match {
      case Some(value) if userAnswers.get(RfmSecondaryPhonePreferencePage).isEmpty =>
        controllers.rfm.routes.RfmSecondaryPhonePreferenceController.onPageLoad(CheckMode)
      case _ => controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad
    }

  private def rfmUkBasedLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmUkBasedPage)
      .map { rfmUkBased =>
        if rfmUkBased then {
          controllers.rfm.routes.RfmEntityTypeController.onPageLoad(NormalMode)
        } else {
          controllers.rfm.routes.RfmNameRegistrationController.onPageLoad(NormalMode)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def rfmUkBasedCheckRouteLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmUkBasedPage)
      .map { rfmUkBased =>
        if rfmUkBased then {
          controllers.rfm.routes.RfmEntityTypeController.onPageLoad(NormalMode)
        } else {
          controllers.rfm.routes.RfmNameRegistrationController.onPageLoad(NormalMode)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def rfmCorporatePosition(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmCorporatePositionPage)
      .map { corporatePosition =>
        if corporatePosition == CorporatePosition.Upe then {
          controllers.rfm.routes.RfmContactDetailsRegistrationController.onPageLoad()
        } else {
          controllers.rfm.routes.CheckNewFilingMemberController.onPageLoad(NormalMode)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def rfmCorporatePositionCheckRouteLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmCorporatePositionPage)
      .map { corporatePosition =>
        if !userAnswers.isRfmJourneyCompleted && corporatePosition == CorporatePosition.NewNfm then {
          controllers.rfm.routes.CheckNewFilingMemberController.onPageLoad(NormalMode)
        } else { reviewAndSubmitCheckYourAnswers }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

}
