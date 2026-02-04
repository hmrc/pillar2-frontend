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
import org.jsoup.select.Elements
import org.scalatest.LoneElement
import viewmodels.WaitingRoom
import views.behaviours.ViewScenario
import views.html.WaitingRoomView

import scala.jdk.CollectionConverters.*

class WaitingRoomViewSpec extends ViewSpecBase with LoneElement {

  lazy val params: WaitingRoom = viewmodels.WaitingRoom(
    pageTitle = "some test page title",
    h1Message = "test h1 message",
    h2Message = "some h2 message",
    afterHeadingsContent = Some("message below the spinner and under the other headings")
  )

  lazy val page: WaitingRoomView = inject[WaitingRoomView]
  lazy val doc:  Document        = Jsoup.parse(page(params)(request, appConfig, messages).toString())

  "Waiting room view" must {
    "have the passed in title" in {
      doc.title() mustBe s"${params.pageTitle} - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have the passed in h1 message" in {
      val h1Elements: Elements = doc.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe params.h1Message
    }

    "have a banner without a link" in {
      val className: String = "govuk-header__service-name"
      doc.getElementsByClass(className).attr("href") mustBe empty
    }

    "have a single visible subheading" in {
      val h2s = doc
        .getElementsByTag("h2")
        .asScala
        .filter(!_.classNames().contains("govuk-visually-hidden"))

      h2s must have size 1
      h2s.loneElement.text() mustBe params.h2Message
    }

    "have a post-headings message" when {
      "present in params" in {
        val body = doc.getElementsByClass("govuk-body")
        body.size() mustBe 1
        body.text() mustBe params.afterHeadingsContent.value
      }
    }

    "not have a post-headings message" when {
      "missing from params" in {
        val doc: Document = Jsoup.parse(page(params.copy(afterHeadingsContent = None))(request, appConfig, messages).toString())
        val body = doc.getElementsByClass("govuk-body")
        body.size() mustBe 0
        body.text() mustBe empty
      }
    }

    "display the spinner" in {
      doc.getElementsByClass("hods-loading-spinner__spinner").size() mustBe 1
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("doc", doc)
      )

    behaveLikeAccessiblePage(viewScenarios)
  }

}
