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
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.btn.BTNWaitingRoomView

class BTNWaitingRoomViewSpec extends ViewSpecBase {

  lazy val page:      BTNWaitingRoomView = inject[BTNWaitingRoomView]
  lazy val view:      Document           = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle: String             = "Submitting your Below-Threshold Notification"

  "BTN Waiting Room View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "Submitting your notification"
    }

    "have a banner with without a link" in {
      val className: String = "govuk-header__service-name"
      view.getElementsByClass(className).attr("href") mustBe empty
    }

    "have a sub heading" in {
      view.getElementsByTag("h2").first().text() mustBe
        "Don’t leave this page"
    }

    "have a paragraph body" in {
      val paragraph: Element = view.getElementsByClass("govuk-body").first()
      paragraph.text mustBe "You will be redirected automatically when the submission is complete."
    }

    "display spinner" in {
      view.getElementsByClass("hods-loading-spinner__spinner").size() must be > 0
    }
  }
}
