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

package views

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.CannotReturnAfterSubscriptionView

class CannotReturnAfterSubscriptionViewSpec extends ViewSpecBase {
  private lazy val page: CannotReturnAfterSubscriptionView = inject[CannotReturnAfterSubscriptionView]
  lazy val view:         Document                          = Jsoup.parse(page()(request, appConfig, messages).toString())
  lazy val pageTitle:    String                            = "Register your group"

  "CannotReturnAfterSubscriptionView" should {
    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "You cannot return, your registration is complete" // FIXME: inconsistency between title and H1
    }

    "display error message correctly" in {
      val message = view.getElementsByClass("govuk-body").first()
      message.text() mustBe "You have successfully registered to report Pillar 2 Top-up Taxes."
    }

    "display link section correctly" in {
      val linkSection = view.getElementsByClass("govuk-body").last()
      linkSection.text() mustEqual "You can now report and manage your Pillar 2 Top-up Taxes."

      val link = linkSection.getElementsByClass("govuk-link").first()
      link.text() mustBe "report and manage your Pillar 2 Top-up Taxes."
      link.attr("href") mustBe controllers.routes.DashboardController.onPageLoad.url
    }

    "not display back link" in {
      view.getElementsByClass("govuk-back-link").size() mustBe 0
    }
  }

}
