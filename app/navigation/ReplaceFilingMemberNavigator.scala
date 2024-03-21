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

  private lazy val reviewAndSubmitCheckYourAnswers   = ??? // TODO route to final check answers page for rfm journey
  private lazy val securityQuestionsCheckYourAnswers = controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad(CheckMode)
  private lazy val noIdCheckYourAnswers              = controllers.rfm.routes.NoIdCheckYourAnswersController.onPageLoad(CheckMode)

  private val normalRoutes: Page => UserAnswers => Call = {
    case RfmPillar2ReferencePage      => _ => controllers.rfm.routes.GroupRegistrationDateReportController.onPageLoad(NormalMode)
    case RfmRegistrationDatePage      => _ => controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad(NormalMode)
    case RfmNoIdNameRegistrationPage  => _ => controllers.rfm.routes.NoIdRegisteredAddressController.onPageLoad(NormalMode)
    case RfmNoIdRegisteredAddressPage => _ => controllers.rfm.routes.NoIdCheckYourAnswersController.onPageLoad(NormalMode)
    case _                            => _ => controllers.rfm.routes.StartPageController.onPageLoad
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case RfmPillar2ReferencePage      => whichCheckYourAnswerPageSecurityQuestions
    case RfmRegistrationDatePage      => whichCheckYourAnswerPageSecurityQuestions
    case RfmNoIdNameRegistrationPage  => whichCheckYourAnswerPageNoIdQuestions
    case RfmNoIdRegisteredAddressPage => whichCheckYourAnswerPageNoIdQuestions
    case _                            => _ => controllers.rfm.routes.StartPageController.onPageLoad
  }

  private def whichCheckYourAnswerPageSecurityQuestions(userAnswers: UserAnswers): Call =
    userAnswers.get(RfmCheckYourAnswersLogicPage) match {
      case Some(true) => reviewAndSubmitCheckYourAnswers
      case _          => securityQuestionsCheckYourAnswers
    }

  private def whichCheckYourAnswerPageNoIdQuestions(userAnswers: UserAnswers): Call =
    userAnswers.get(RfmCheckYourAnswersLogicPage) match {
      case Some(true) => reviewAndSubmitCheckYourAnswers
      case _          => noIdCheckYourAnswers
    }

}
