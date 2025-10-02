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
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.RegistrationInProgressView

class RegistrationInProgressViewSpec extends ViewSpecBase {
  lazy val page:   RegistrationInProgressView = inject[RegistrationInProgressView]
  lazy val plrRef: String                     = "XMPLR0012345678"

  lazy val view: Document = Jsoup.parse(
    page(plrRef)(request, messages, appConfig).toString()
  )

  "RegistrationInProgressView" should {
    "have a title" in {
      view.title() mustBe "Registration in progress - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByClass("govuk-heading-l")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe "Your Pillar 2 Top-up Taxes account"
    }

    "display the PLR reference correctly" in {
      val plrRefElement: Element = view.getElementsByClass("govuk-body").get(1)
      plrRefElement.text() must include(plrRef)
    }

    "have a notification banner with registration in progress message" in {
      val banners: Elements = view.getElementsByClass("govuk-notification-banner")
      banners.size() mustBe 1

      val banner:        Element = banners.first()
      val bannerHeading: Element = banner.getElementsByClass("govuk-notification-banner__heading").first()
      bannerHeading.text() mustBe "Your registration is in progress"

      val bannerMessage: Element = banner.getElementsByClass("govuk-body").first()
      bannerMessage.text() mustBe "We are processing your registration. You can check this page in one hour to see your full group account homepage."
    }

    "have a banner with important label" in {
      val importantLabel: Element = view.getElementsByClass("govuk-notification-banner__title").first()
      importantLabel.text() mustBe "Important"
    }

    "use full-width layout without back link" in {
      view.getElementsByClass("govuk-back-link").size() mustBe 0
    }

    "have correct notification banner structure" in {
      val banners: Elements = view.getElementsByClass("govuk-notification-banner")
      banners.size() mustBe 1

      val banner = banners.first()
      banner.getElementsByClass("govuk-notification-banner__title").size() mustBe 1
      banner.getElementsByClass("govuk-notification-banner__heading").size() mustBe 1
      banner.getElementsByClass("govuk-body").size() mustBe 1
    }
  }
}
