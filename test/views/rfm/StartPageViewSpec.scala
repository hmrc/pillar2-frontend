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

package views.rfm

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.rfm.StartPageView

class StartPageViewSpec extends ViewSpecBase {

  val page: StartPageView = inject[StartPageView]

  val view: Document = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Start Page View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("Replace the filing member for a Pillar 2 top-up taxes account")
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Replace filing member")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Replace the filing member for a Pillar 2 top-up taxes account")
    }

    "have sub headings" in {
      view.getElementsByClass("govuk-heading-m").get(0).text must include(
        "Tell HMRC when you have replaced your " +
          "filing member"
      )
      view.getElementsByClass("govuk-heading-m").get(1).text must include("Who can replace a filing member")
      view.getElementsByClass("govuk-heading-m").get(2).text must include("Obligations as the filing member")
      view.getElementsByClass("govuk-heading-m").get(3).text must include("What you will need")
      view.getElementsByClass("govuk-heading-s").get(0).text must include(
        "By continuing you confirm you are able to act as a new filing member for your group"
      )
    }

    "have paragraphs" in {
      view.getElementsByClass("govuk-body").get(0).text must include(
        "Use this service to replace the filing member " +
          "for an existing Pillar 2 top-up taxes account."
      )

      view.getElementsByClass("govuk-body").get(1).text must include(
        "It is a legal requirement to replace your filing member’s details " +
          "within 6 months of the change occurring in your group."
      )

      view.getElementsByClass("govuk-body").get(2).text must include(
        "If your group has not yet registered, you will " +
          "need to register to report Pillar 2 top-up taxes. You can choose to nominate a filing member during registration."
      )

      view.getElementsByClass("govuk-body").get(3).text must include(
        "Only the new filing member can use this service. " +
          "This can either be the ultimate parent entity or another company member which has been nominated by the ultimate parent entity."
      )

      view.getElementsByClass("govuk-body").get(4).text must include(
        "As the new filing member, you will take over " +
          "the obligations to:"
      )

      view.getElementsByClass("govuk-body").get(5).text must include(
        "If you fail to meet your obligations as a " +
          "filing member, you may be liable for penalties."
      )

      view.getElementsByClass("govuk-body").get(6).text must include(
        "To replace the filing member, you’ll need to " +
          "provide the Government Gateway user ID for the new filing member."
      )

      view.getElementsByClass("govuk-body").get(7).text must include(
        "If the new filing member is a UK limited company, or limited liability partnership, " +
          "you must also provide the company registration number, and Unique Taxpayer Reference."
      )

      view.getElementsByClass("govuk-body").get(8).text must include(
        "You’ll also need to tell us:"
      )
    }

    "have bullet lists" in {
      view.getElementsByTag("li").get(0).text must include(
        "act as HMRC’s primary contact in relation to the group’s Pillar 2 top-up tax compliance"
      )

      view.getElementsByTag("li").get(1).text must include(
        "submit your group’s Pillar 2 top-up tax returns"
      )

      view.getElementsByTag("li").get(2).text must include(
        "ensure your group’s Pillar 2 top-up taxes account accurately reflects their records"
      )

      view.getElementsByTag("li").get(3).text must include("the group’s Pillar 2 top-up taxes ID")

      view.getElementsByTag("li").get(4).text must include(
        "the date the group first registered to report their Pillar 2 top up taxes in the UK"
      )

      view.getElementsByTag("li").get(5).text must include(
        "contact details and preferences, for one or 2 individuals or teams in the group"
      )

      view.getElementsByTag("li").get(6).text must include("a contact postal address for the group")

    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Confirm and continue")
    }
  }
}
