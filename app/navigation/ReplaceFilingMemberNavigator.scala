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
class ReplaceFilingMemberNavigator @Inject() {
  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }

  private lazy val reviewAndSubmitCheckYourAnswers =
    controllers.routes.UnderConstructionController.onPageLoad // TODO route to final check answers page for rfm journey
  private lazy val securityQuestionsCheckYourAnswers = controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad(CheckMode)
  private lazy val rfmCheckYourAnswers               = controllers.rfm.routes.RfmCheckYourAnswersController.onPageLoad(CheckMode)

  private val normalRoutes: Page => UserAnswers => Call = {

    case RfmPrimaryContactNamePage      => _ => controllers.rfm.routes.RfmPrimaryContactEmailController.onPageLoad(NormalMode)
    case RfmPrimaryContactEmailPage     => _ => controllers.rfm.routes.RfmContactByTelephoneController.onPageLoad(NormalMode)
    case RfmContactByTelephonePage      => telephonePreferenceLogic
    case RfmCapturePrimaryTelephonePage => _ => controllers.routes.UnderConstructionController.onPageLoad
    case RfmUkBasedPage                 => rfmUkBasedLogic
    case RfmPillar2ReferencePage        => _ => controllers.rfm.routes.GroupRegistrationDateReportController.onPageLoad(NormalMode)
    case RfmRegistrationDatePage        => _ => controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad(NormalMode)
    case RfmNameRegistrationPage        => _ => controllers.rfm.routes.RfmRegisteredAddressController.onPageLoad(NormalMode)
    case RfmRegisteredAddressPage       => _ => controllers.rfm.routes.RfmCheckYourAnswersController.onPageLoad(NormalMode)
    case _                              => _ => controllers.rfm.routes.StartPageController.onPageLoad
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case RfmPillar2ReferencePage  => _ => securityQuestionsCheckYourAnswers
    case RfmRegistrationDatePage  => _ => securityQuestionsCheckYourAnswers
    case RfmNameRegistrationPage  => whichCheckYourAnswerPageRfmQuestions
    case RfmRegisteredAddressPage => whichCheckYourAnswerPageRfmQuestions
    case _                        => _ => controllers.rfm.routes.StartPageController.onPageLoad
  }

  private def whichCheckYourAnswerPageRfmQuestions(userAnswers: UserAnswers): Call =
    userAnswers.get(RfmCheckYourAnswersLogicPage) match {
      case Some(true) => reviewAndSubmitCheckYourAnswers
      case _          => rfmCheckYourAnswers
    }

  private def telephonePreferenceLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmContactByTelephonePage)
      .map { provided =>
        if (provided) {
          controllers.rfm.routes.RfmCapturePrimaryTelephoneController.onPageLoad(NormalMode)
        } else {
          controllers.routes.UnderConstructionController.onPageLoad
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def rfmUkBasedLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmUkBasedPage)
      .map { rfmUkBased =>
        if (rfmUkBased) {
          controllers.routes.UnderConstructionController.onPageLoad
        } else {
          controllers.rfm.routes.RfmNameRegistrationController.onPageLoad(NormalMode)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

}