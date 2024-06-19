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
class RepaymentNavigator @Inject() {

  def nextPage(page: Page, clientPillar2Id: Option[String] = None, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(clientPillar2Id)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(clientPillar2Id)(userAnswers)
  }
  private val normalRoutes: Page => Option[String] => UserAnswers => Call = {
    case RepaymentsRefundAmountPage => _ => _ => routes.UnderConstructionController.onPageLoad
    case RepaymentsContactNamePage  => id => _ => controllers.repayments.routes.RepaymentsContactEmailController.onPageLoad(id, NormalMode)
    case RepaymentsContactEmailPage => id => _ => routes.UnderConstructionController.onPageLoad
    case _                          => _ => _ => routes.IndexController.onPageLoad
  }

  private val checkRouteMap: Page => Option[String] => UserAnswers => Call = _ => _ => _ => routes.IndexController.onPageLoad

}
