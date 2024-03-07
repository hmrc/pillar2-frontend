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

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import pages._
import models._

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case UpeRegisteredInUKPage => domesticOrNotRoute
    case UpeNameRegistrationPage => _ => controllers.registration.routes.UpeRegisteredAddressController.onPageLoad(NormalMode)
    case UpeRegisteredAddressPage => _ => controllers.registration.routes.UpeContactNameController.onPageLoad(NormalMode)
    case UpeContactNamePage => _ => controllers.registration.routes.UpeContactEmailController.onPageLoad(NormalMode)
    case UpeContactEmailPage => _ => controllers.registration.routes.ContactUPEByTelephoneController.onPageLoad(NormalMode)
    case UpePhonePreferencePage => telephonePreferenceLogic
    case UpeCapturePhonePage => _ => controllers.registration.routes.UpeCheckYourAnswersController.onPageLoad
    case _ => _ => routes.IndexController.onPageLoad
  }

  private def domesticOrNotRoute(userAnswers: UserAnswers): Call ={
    userAnswers.get(UpeRegisteredInUKPage).map{ ukBased=>
      if (ukBased) {
        controllers.registration.routes.UpeNameRegistrationController.onPageLoad(NormalMode)
      }else{
        controllers.registration.routes.EntityTypeController.onPageLoad(NormalMode)
      }
    }.getOrElse(routes.JourneyRecoveryController.onPageLoad())
  }

  private def telephonePreferenceLogic(userAnswers: UserAnswers): Call ={
    userAnswers.get(UpePhonePreferencePage).map{ provided=>
      if (provided) {
        controllers.registration.routes.CaptureTelephoneDetailsController.onPageLoad(NormalMode)
      }else{
        controllers.registration.routes.UpeCheckYourAnswersController.onPageLoad
      }
    }.getOrElse(routes.JourneyRecoveryController.onPageLoad())
  }

  private val checkRouteMap: Page => UserAnswers => Call = { case _ =>
    _ => routes.CheckYourAnswersController.onPageLoad
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}
