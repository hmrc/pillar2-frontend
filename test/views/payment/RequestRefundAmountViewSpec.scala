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

package views.payment

import base.ViewSpecBase
import forms.RequestRefundAmountFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.payment.{RequestRefundAmountView, RequestRefundBeforeStartView}

class RequestRefundAmountViewSpec extends ViewSpecBase {
  val formProvider = new RequestRefundAmountFormProvider
  val mode         = NormalMode
  val page         = inject[RequestRefundAmountView]

  val view = Jsoup.parse(page(formProvider(), mode)(request, appConfig, messages).toString())

  "Request Refund Amount View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("Enter your requested refund amount in pounds")
    }

    "have a h1 heading" in {
      view.getElementsByTag("h1").text must include("Enter your requested refund amount in pounds")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }

  }
}
