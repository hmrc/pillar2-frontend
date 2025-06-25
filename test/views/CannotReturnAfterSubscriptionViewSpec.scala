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
import views.html.CannotReturnAfterSubscriptionView

class CannotReturnAfterSubscriptionViewSpec extends ViewSpecBase {
  private val page: CannotReturnAfterSubscriptionView = inject[CannotReturnAfterSubscriptionView]

  val doc: Document = Jsoup.parse(page()(request, appConfig, messages).toString())

  "CannotReturnAfterSubscriptionView" should {
    "display page header correctly" in {
      doc.getElementsByTag("h1").first().text() mustBe "You cannot return, your registration is complete"
    }

    "display error message correctly" in {
      val message = doc.getElementsByClass("govuk-body").first()
      message.text() mustBe "You have successfully registered to report Pillar 2 Top-up Taxes."
    }

    "display link section correctly" in {
      val linkSection = doc.getElementsByClass("govuk-body").last()
      linkSection.text() mustEqual "You can now report and manage your Pillar 2 Top-up Taxes."

      val link = linkSection.getElementsByClass("govuk-link").first()
      link.text() mustBe "report and manage your Pillar 2 Top-up Taxes."
      link.attr("href") mustBe "/report-pillar2-top-up-taxes/pillar2-top-up-tax-home"
    }
  }

  "CannotReturnAfterSubscriptionView layout" should {
    "have correct page title" in {
      doc.title() mustEqual "Register your group - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "not display back link" in {
      doc.getElementsByClass("govuk-back-link").size() mustBe 0
    }
  }
}
