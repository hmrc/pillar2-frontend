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
import views.html.rfm.JourneyRecoveryView

class JourneyRecoveryViewSpec extends ViewSpecBase {

  val page = inject[JourneyRecoveryView]

  val view = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Replace filing member journey recovery view" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("There has been an error")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("There has been an error")
    }

    "have a link with the correct text and url" in {
      val expectedLink = "/report-pillar2-top-up-taxes/replace-filing-member/start"
      val linkExists   = view.getElementsByAttributeValue("href", expectedLink).first() != null
      linkExists mustBe true

      view.getElementsByTag("p").text must include(
        messages("You can go back to")
          + " "
          + messages("replace the filing member for a Pillar 2 top-up taxes account to try again")
      )

    }

  }
}
