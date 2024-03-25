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
import models._
import models.rfm.RegistrationDate
import pages._

import java.time.LocalDate

class ReplaceFilingMemberNavigatorSpec extends SpecBase {

  val navigator = new ReplaceFilingMemberNavigator
  private val nonUKAddress = NonUKAddress(
    addressLine1 = "line1",
    addressLine2 = None,
    addressLine3 = "line3",
    addressLine4 = None,
    postalCode = None,
    countryCode = "AB"
  )

  private lazy val securityQuestionsCYA = controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad(CheckMode)
  private lazy val noIdQuestionsCYA     = controllers.rfm.routes.NoIdCheckYourAnswersController.onPageLoad(CheckMode)
  "Replace Filing Member Navigator" when {

    "in Normal mode" must {

      "must go from a page that doesn't exist in the route map to rfm start page" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe controllers.rfm.routes.StartPageController.onPageLoad
      }
      "go to registration date page from pillar 2 reference page" in {
        navigator.nextPage(
          RfmPillar2ReferencePage,
          NormalMode,
          emptyUserAnswers.setOrException(RfmPillar2ReferencePage, "XMPLR0123456789")
        ) mustBe
          controllers.rfm.routes.GroupRegistrationDateReportController.onPageLoad(NormalMode)
      }
      "go to security questions CYA page from registration date page" in {
        navigator.nextPage(
          RfmRegistrationDatePage,
          NormalMode,
          emptyUserAnswers.setOrException(RfmRegistrationDatePage, RegistrationDate(LocalDate.now()))
        ) mustBe
          controllers.rfm.routes.SecurityQuestionsCheckYourAnswersController.onPageLoad(NormalMode)
      }
      "go to registered address page from name registration page" in {
        navigator.nextPage(RfmNameRegistrationPage, NormalMode, emptyUserAnswers.setOrException(RfmNameRegistrationPage, "first last")) mustBe
          controllers.rfm.routes.RfmRegisteredAddressController.onPageLoad(NormalMode)
      }
      "go to no id CYA page from registered address page" in {
        navigator.nextPage(
          RfmRegisteredAddressPage,
          NormalMode,
          emptyUserAnswers.setOrException(RfmRegisteredAddressPage, nonUKAddress)
        ) mustBe
          controllers.rfm.routes.NoIdCheckYourAnswersController.onPageLoad(NormalMode)
      }
    }

    "in Check mode" must {

      "must go from a page that doesn't exist in the route map to rfm start page" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe controllers.rfm.routes.StartPageController.onPageLoad
      }
      "go to security questions CYA page from pillar 2 reference page" in {
        navigator.nextPage(
          RfmPillar2ReferencePage,
          CheckMode,
          emptyUserAnswers.setOrException(RfmPillar2ReferencePage, "XMPLR0123456789")
        ) mustBe
          securityQuestionsCYA
      }
      "go to security questions CYA page from registration date page" in {
        navigator.nextPage(
          RfmRegistrationDatePage,
          CheckMode,
          emptyUserAnswers.setOrException(RfmRegistrationDatePage, RegistrationDate(LocalDate.now()))
        ) mustBe
          securityQuestionsCYA
      }
      "go to no id CYA page from name registration page" in {
        navigator.nextPage(
          RfmNameRegistrationPage,
          CheckMode,
          emptyUserAnswers.setOrException(RfmNameRegistrationPage, "first last")
        ) mustBe
          noIdQuestionsCYA
      }
      "go to no id CYA page from registered address page" in {
        navigator.nextPage(
          RfmRegisteredAddressPage,
          CheckMode,
          emptyUserAnswers.setOrException(RfmRegisteredAddressPage, nonUKAddress)
        ) mustBe
          noIdQuestionsCYA
      }
    }
  }
}
