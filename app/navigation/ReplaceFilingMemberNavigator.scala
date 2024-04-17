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
import models.rfm.CorporatePosition

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
  private lazy val rfmContactDetailsCheckYourAnswers = controllers.rfm.routes.ContactDetailsCheckYourAnswersController.onPageLoad

  private val normalRoutes: Page => UserAnswers => Call = {

    case RfmPrimaryContactNamePage       => _ => controllers.rfm.routes.RfmPrimaryContactEmailController.onPageLoad(NormalMode)
    case RfmPrimaryContactEmailPage      => _ => controllers.rfm.routes.RfmContactByTelephoneController.onPageLoad(NormalMode)
    case RfmContactByTelephonePage       => telephonePreferenceLogic
    case RfmCapturePrimaryTelephonePage  => rfmPrimaryPhoneCaptureRoute
    case RfmCorporatePositionPage        => rfmCorporatePosition
    case RfmUkBasedPage                  => rfmUkBasedLogic
    case RfmAddSecondaryContactPage      => rfmAddSecondaryContactRoute
    case RfmSecondaryContactNamePage     => _ => controllers.rfm.routes.RfmSecondaryContactEmailController.onPageLoad(NormalMode)
    case RfmSecondaryEmailPage           => _ => controllers.rfm.routes.RfmSecondaryTelephonePreferenceController.onPageLoad(NormalMode)
    case RfmSecondaryPhonePreferencePage => rfmSecondaryPhonePreference
    case RfmSecondaryCapturePhonePage    => rfmSecondaryPhoneCaptureRoute
    case RfmCheckYourAnswersPage         => rfmRegistrationDetailsCheckRoute
    case RfmContactAddressPage           => _ => controllers.rfm.routes.ContactDetailsCheckYourAnswersController.onPageLoad
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
    case RfmPrimaryContactNamePage       => whichCheckYourAnswerPageContactQuestions
    case RfmPrimaryContactEmailPage      => whichCheckYourAnswerPageContactQuestions
    case RfmContactByTelephonePage       => telephonePreferenceLogic
    case RfmCapturePrimaryTelephonePage  => whichCheckYourAnswerPageContactQuestions
    case RfmAddSecondaryContactPage      => rfmAddSecondaryContactRoute
    case RfmSecondaryContactNamePage     => whichCheckYourAnswerPageContactQuestions
    case RfmSecondaryEmailPage           => whichCheckYourAnswerPageContactQuestions
    case RfmSecondaryPhonePreferencePage => rfmSecondaryPhonePreference
    case RfmSecondaryCapturePhonePage    => whichCheckYourAnswerPageContactQuestions
    case RfmContactAddressPage           => whichCheckYourAnswerPageContactQuestions
    case RfmCheckYourAnswersPage         => _ => controllers.rfm.routes.RfmContactDetailsRegistrationController.onPageLoad
    case _                               => _ => controllers.rfm.routes.StartPageController.onPageLoad
  }

  private def whichCheckYourAnswerPageRfmQuestions(userAnswers: UserAnswers): Call =
    userAnswers.get(RfmCheckYourAnswersLogicPage) match {
      case Some(true) => reviewAndSubmitCheckYourAnswers
      case _          => rfmCheckYourAnswers
    }

  private def whichCheckYourAnswerPageContactQuestions(userAnswers: UserAnswers): Call =
    userAnswers.get(RfmCheckYourAnswersLogicPage) match {
      case Some(true) => reviewAndSubmitCheckYourAnswers
      case _          => rfmContactDetailsCheckYourAnswers
    }

  private def rfmRegistrationDetailsCheckRoute(userAnswers: UserAnswers): Call =
    userAnswers.get(RfmCheckYourAnswersLogicPage) match {
      case Some(true) => reviewAndSubmitCheckYourAnswers
      case _          => controllers.rfm.routes.RfmContactDetailsRegistrationController.onPageLoad
    }

  private def telephonePreferenceLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmContactByTelephonePage)
      .map {
        case true if userAnswers.get(RfmCapturePrimaryTelephonePage).isEmpty =>
          controllers.rfm.routes.RfmCapturePrimaryTelephoneController.onPageLoad(NormalMode)
        case true =>
          whichCheckYourAnswerPageContactQuestions(userAnswers)
        case false if userAnswers.get(RfmAddSecondaryContactPage).isEmpty =>
          controllers.rfm.routes.RfmAddSecondaryContactController.onPageLoad(NormalMode)
        case false =>
          whichCheckYourAnswerPageContactQuestions(userAnswers)
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def rfmAddSecondaryContactRoute(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmAddSecondaryContactPage)
      .map {
        case true if userAnswers.get(RfmSecondaryContactNamePage).isEmpty =>
          controllers.rfm.routes.RfmSecondaryContactNameController.onPageLoad(NormalMode)
        case false if userAnswers.get(RfmContactAddressPage).isEmpty =>
          controllers.rfm.routes.RfmContactAddressController.onPageLoad(NormalMode)
        case _ =>
          whichCheckYourAnswerPageContactQuestions(userAnswers)
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def rfmSecondaryPhonePreference(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmSecondaryPhonePreferencePage)
      .map {
        case true if userAnswers.get(RfmSecondaryCapturePhonePage).isEmpty =>
          controllers.rfm.routes.RfmSecondaryTelephoneController.onPageLoad(NormalMode)
        case false if userAnswers.get(RfmContactAddressPage).isEmpty =>
          controllers.rfm.routes.RfmContactAddressController.onPageLoad(NormalMode)
        case _ =>
          whichCheckYourAnswerPageContactQuestions(userAnswers)
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def rfmSecondaryPhoneCaptureRoute(userAnswers: UserAnswers): Call =
    if (userAnswers.get(RfmContactAddressPage).isEmpty) {
      controllers.rfm.routes.RfmContactAddressController.onPageLoad(NormalMode)
    } else {
      whichCheckYourAnswerPageContactQuestions(userAnswers)
    }

  private def rfmPrimaryPhoneCaptureRoute(userAnswers: UserAnswers): Call =
    if (userAnswers.get(RfmAddSecondaryContactPage).isEmpty) {
      controllers.rfm.routes.RfmAddSecondaryContactController.onPageLoad(NormalMode)
    } else {
      whichCheckYourAnswerPageContactQuestions(userAnswers)
    }

  private def rfmUkBasedLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmUkBasedPage)
      .map { rfmUkBased =>
        if (rfmUkBased) {
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
        if (rfmUkBased) {
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
        if (corporatePosition == CorporatePosition.Upe) {
          controllers.rfm.routes.RfmContactDetailsRegistrationController.onPageLoad
        } else if (corporatePosition == CorporatePosition.NewNfm) {
          controllers.rfm.routes.CheckNewFilingMemberController.onPageLoad(NormalMode)
        } else {
          routes.JourneyRecoveryController.onPageLoad()
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def rfmCorporatePositionCheckRouteLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmCorporatePositionPage)
      .map { corporatePosition =>
        if (corporatePosition == CorporatePosition.Upe) {
          reviewAndSubmitCheckYourAnswers
        } else if (corporatePosition == CorporatePosition.NewNfm) {
          controllers.rfm.routes.CheckNewFilingMemberController.onPageLoad(NormalMode)
        } else {
          routes.JourneyRecoveryController.onPageLoad()
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

}
