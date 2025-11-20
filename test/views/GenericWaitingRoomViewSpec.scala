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
import org.jsoup.nodes.Document
import play.api.mvc.Call
import views.html.GenericWaitingRoomView

class GenericWaitingRoomViewSpec extends ViewSpecBase {

  lazy val page: GenericWaitingRoomView = inject[GenericWaitingRoomView]
  val pageTitle            = "Test Title"
  val heading              = "Test Heading"
  val subHeading           = "Test SubHeading"
  val pollInterval         = 3
  val redirectUrl          = Call("GET", "/test-url")
  val afterHeadingsMessage = Some("Test Redirect Message")

  lazy val view: Document = Jsoup.parse(
    page(
      pageTitle = pageTitle,
      heading = heading,
      subHeading = subHeading,
      pollInterval = pollInterval,
      redirectUrl = redirectUrl,
      afterHeadingsMessage = afterHeadingsMessage
    )(request, appConfig, messages).toString()
  )

  "Generic Waiting Room View" should {

    "have the correct title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have the correct H1" in {
      view.getElementsByTag("h1").text() mustBe heading
    }

    "have the correct H2" in {
      view.getElementsByTag("h2").first().text() mustBe subHeading
    }

    "have the correct body text" in {
      view.getElementsByClass("govuk-body").first().text() mustBe afterHeadingsMessage.get
    }

    "have a meta refresh tag" in {
      val meta = view.getElementsByTag("meta").last()
      meta.attr("http-equiv") mustBe "refresh"
      meta.attr("content") mustBe s"$pollInterval;url=${redirectUrl.url}"
    }

    "display spinner" in {
      view.getElementsByClass("hods-loading-spinner__spinner").size() must be > 0
    }
  }
}

