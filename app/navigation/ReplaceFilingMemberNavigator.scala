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
    case _ => controllers.routes.UnderConstructionController.onPageLoad
  }
  private val normalRoutes: Page => UserAnswers => Call = {
    case RfmPrimaryContactNamePage      => _ => controllers.rfm.routes.RfmPrimaryContactEmailController.onPageLoad(NormalMode)
    case RfmPrimaryContactEmailPage     => _ => controllers.rfm.routes.RfmContactByTelephoneController.onPageLoad(NormalMode)
    case RfmContactByTelephonePage      => telephonePreferenceLogic
    case RfmCapturePrimaryTelephonePage => _ => controllers.routes.UnderConstructionController.onPageLoad
    case _                              => _ => routes.IndexController.onPageLoad
  }

  private def telephonePreferenceLogic(userAnswers: UserAnswers): Call =
    userAnswers
      .get(RfmContactByTelephonePage)
      .map { provided =>
        if (provided) {
          controllers.rfm.routes.RfmCapturePrimaryTelephoneController.onPageLoad(NormalMode)
        } else {
          controllers.rfm.routes.RfmContactAddressController.onPageLoad(NormalMode)
        }
      }
      .getOrElse(routes.JourneyRecoveryController.onPageLoad())

}
