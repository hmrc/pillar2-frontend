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

package views.btn

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.btn.BTNCannotReturnView

class BTNCannotReturnViewSpec extends ViewSpecBase {

  val page: BTNCannotReturnView = inject[BTNCannotReturnView]
  val view: Document            = Jsoup.parse(page()(request, appConfig, messages).toString())

  "BTNCannotReturnView" should {

    "have no back link" in {
      view.getElementsByClass("govuk-back-link").size mustBe 0
    }

    "have a h1 heading" in {
      view.getElementsByTag("h1").text mustEqual "You have submitted a Below-Threshold Notification"
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")

      link.text must include("Return to your group’s homepage")
      link.attr("href") mustEqual controllers.routes.DashboardController.onPageLoad.url
    }
  }
}
