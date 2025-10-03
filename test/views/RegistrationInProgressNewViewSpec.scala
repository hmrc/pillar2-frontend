/*
 * Copyright 2025 HM Revenue & Customs
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
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.RegistrationInProgressNewView

class RegistrationInProgressNewViewSpec extends ViewSpecBase {
  lazy val page:   RegistrationInProgressNewView = inject[RegistrationInProgressNewView]
  lazy val plrRef: String                        = "XMPLR0012345678"

  lazy val view: Document = Jsoup.parse(
    page(plrRef)(request, appConfig, messages).toString()
  )

  "RegistrationInProgressNewView" should {
    "have a title" in {
      view.title() mustBe "Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByClass("govuk-heading-l")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "Pillar 2 Top-up Taxes"
    }

    "display the PLR reference correctly" in {
      val plrRefElement: Element = view.getElementsByClass("govuk-body").first()
      plrRefElement.text() mustBe s"ID: $plrRef"
    }

    "have a banner with registration in progress message" in {
      val bannerHeading: Element = view.getElementsByClass("govuk-heading-s").first()
      bannerHeading.text() mustBe "Your registration is in progress"

      val bannerMessage: Element = view.getElementsByClass("govuk-body").get(1)
      bannerMessage.text() mustBe "We are processing your registration. You can check this page in one hour to see your full group account homepage."
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      view.getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
    }

    "use full-width layout without back link" in {
      view.getElementsByClass("govuk-back-link").size() mustBe 0
    }

    "have correct structure with horizontal rules" in {
      val horizontalRules: Elements = view.getElementsByClass("govuk-section-break")
      horizontalRules.size() mustBe 2

      val visibleRules: Elements = view.getElementsByClass("govuk-section-break--visible")
      visibleRules.size() mustBe 2
    }

    "have a two-thirds column for the banner content" in {
      val twoThirdsColumn: Elements = view.getElementsByClass("govuk-grid-column-two-thirds")
      twoThirdsColumn.size() mustBe 1
    }
  }
}
