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

class RepaymentNavigatorSpec extends SpecBase {

  val navigator = new RepaymentNavigator

  private lazy val underConstruction = routes.UnderConstructionController.onPageLoad
  "Navigator" when {

    "in Normal mode" must {

      "must go from a page that doesn't exist in the route map to Index" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, None, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }

      "must go to next page from request refund amount page" in {
        val userAnswers = emptyUserAnswers.setOrException(RepaymentsRefundAmountPage, BigDecimal(100.00))
        navigator.nextPage(RepaymentsRefundAmountPage, None, NormalMode, userAnswers) mustBe underConstruction
      }

      "must go to Repayments contact email page from Repayments contact name page" in {
        navigator.nextPage(
          RepaymentsContactNamePage,
          None,
          NormalMode,
          emptyUserAnswers.setOrException(RepaymentsContactNamePage, "ABC Limited")
        ) mustBe
          controllers.repayments.routes.RepaymentsContactEmailController.onPageLoad(None, NormalMode)
      }

      "must go to Repayments Contact By Telephone page from Repayments contact email page" in {
        navigator.nextPage(
          RepaymentsContactEmailPage,
          None,
          NormalMode,
          emptyUserAnswers.setOrException(RepaymentsContactEmailPage, "hello@bye.com")
        ) mustBe
          controllers.repayments.routes.RepaymentsContactByTelephoneController.onPageLoad(None, NormalMode)
      }

      "must go to Repayments Telephone Details page from Repayments Contact By Telephone page when True" in {
        navigator.nextPage(
          RepaymentsContactByTelephonePage,
          None,
          NormalMode,
          emptyUserAnswers.setOrException(RepaymentsContactByTelephonePage, true)
        ) mustBe
          controllers.repayments.routes.RepaymentsTelephoneDetailsController.onPageLoad(None, NormalMode)
      }

      "must go to UnderConstruction page from Repayments Contact By Telephone page when False" in {
        navigator.nextPage(
          RepaymentsContactByTelephonePage,
          None,
          NormalMode,
          emptyUserAnswers.setOrException(RepaymentsContactByTelephonePage, false)
        ) mustBe
          underConstruction
      }

      "must go to UnderConstruction page from Repayments Telephone Details page" in {
        navigator.nextPage(
          RepaymentsTelephoneDetailsPage,
          None,
          NormalMode,
          emptyUserAnswers.setOrException(RepaymentsTelephoneDetailsPage, "12345")
        ) mustBe
          underConstruction
      }

    }

    "in Check mode" must {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, None, CheckMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }

    }
  }
}
