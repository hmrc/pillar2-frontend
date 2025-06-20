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
import views.html.rfm.RfmCannotReturnAfterConfirmationView

class RfmCannotReturnAfterConfirmationViewSpec extends ViewSpecBase {

  val page: RfmCannotReturnAfterConfirmationView = inject[RfmCannotReturnAfterConfirmationView]
  val view: Document                             = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Rfm Cannot Return After Confirmation View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("Register your group")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("You cannot return, you have replaced the filing member")
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").get(0).text must include(
        "You have successfully replaced the filing member for your Pillar 2 Top-up Taxes account."
      )
      view.getElementsByClass("govuk-body").get(1).text must include(
        "You can now "
      )
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")
      link.text         must include("report and manage your Pillar 2 Top-up Taxes")
      link.attr("href") must include("/report-pillar2-top-up-taxes/home")
    }

  }

}
