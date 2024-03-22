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

import base.SpecBase
import controllers.routes
import models._
import pages._

class ReplaceFilingMemberNavigatorSpec extends SpecBase {

  val navigator = new ReplaceFilingMemberNavigator
  private val ukAddress = UKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = "m19hgs",
    countryCode = "AB"
  )
  private lazy val jr = controllers.routes.JourneyRecoveryController.onPageLoad()
  "Navigator" when {

    "in Normal mode" must {
      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }

      "go to contact email page from contact name page" in {
        navigator.nextPage(RfmPrimaryContactNamePage, NormalMode, emptyUserAnswers.setOrException(UpeContactNamePage, "Paddington")) mustBe
          controllers.rfm.routes.RfmPrimaryContactEmailController.onPageLoad(NormalMode)
      }
      "go to telephone preference page from contact email page" in {
        navigator.nextPage(
          RfmPrimaryContactEmailPage,
          NormalMode,
          emptyUserAnswers.setOrException(UpeContactEmailPage, "something@something.com")
        ) mustBe
          controllers.rfm.routes.RfmContactByTelephoneController.onPageLoad(NormalMode)
      }
      "go to a page where we capture their telephone number" in {
        navigator.nextPage(RfmContactByTelephonePage, NormalMode, emptyUserAnswers.setOrException(RfmContactByTelephonePage, true)) mustBe
          controllers.rfm.routes.RfmCapturePrimaryTelephoneController.onPageLoad(NormalMode)
      }
      "go to journey recovery if no answer for UpePhonePreference page can be found" in {
        navigator.nextPage(RfmContactByTelephonePage, NormalMode, emptyUserAnswers) mustBe
          jr
      }
      "go to CYA page if they have chosen not to nominate a contact number" in {
        navigator.nextPage(
          RfmContactByTelephonePage,
          NormalMode,
          emptyUserAnswers.setOrException(RfmContactByTelephonePage, false)
        ) mustBe controllers.routes.UnderConstructionController.onPageLoad

      }
      "go to CYA page from a page where they enter their phone details" in {
        navigator.nextPage(
          RfmCapturePrimaryTelephonePage,
          NormalMode,
          emptyUserAnswers.setOrException(RfmCapturePrimaryTelephonePage, "12321321")
        ) mustBe
          controllers.routes.UnderConstructionController.onPageLoad
      }
      // add unit test for check mode later
    }

  }
}
