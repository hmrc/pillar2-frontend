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

package views.subscriptionview.manageAccount

import base.ViewSpecBase
import models.subscription.ManageContactDetailsStatus
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.subscriptionview.manageAccount.ManageContactDetailsWaitingRoomView

class ManageContactDetailsWaitingRoomViewSpec extends ViewSpecBase {

  lazy val page: ManageContactDetailsWaitingRoomView = inject[ManageContactDetailsWaitingRoomView]
  lazy val inProgressView: Document = Jsoup.parse(page(Some(ManageContactDetailsStatus.InProgress))(request, appConfig, messages).toString())
  lazy val completedView: Document =
    Jsoup.parse(page(Some(ManageContactDetailsStatus.SuccessfullyCompleted))(request, appConfig, messages).toString())
  lazy val pageTitle: String = "Submitting your contact details"

  "Manage Contact Details Waiting Room View" should {

    "when status is InProgress" must {
      "have a title" in {
        inProgressView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = inProgressView.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a sub heading" in {
        inProgressView.getElementsByTag("h2").first().text() mustBe
          "Do not press back in your browser or leave this page. It may take up to a minute to process this change."
      }

      "display spinner" in {
        inProgressView.getElementsByClass("hods-loading-spinner__spinner").size() must be > 0
      }

      "have a meta refresh tag" in {
        // FIXME: the meta-refresh should not be in the body - is HTML valid if in body???
        val metaRefresh = Option(inProgressView.select("meta[http-equiv=refresh]").first())
        metaRefresh must not be None
      }
    }

    "when status is SuccessfullyCompleted" must {
      "have a title" in {
        completedView.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val h1Elements: Elements = completedView.getElementsByTag("h1")
        h1Elements.size() mustBe 1
        h1Elements.text() mustBe pageTitle
      }

      "have a sub heading" in {
        completedView.getElementsByTag("h2").first().text() mustBe
          "Do not press back in your browser or leave this page. It may take up to a minute to process this change."
      }

      "have a meta refresh tag" in {
        // FIXME: the meta-refresh should not be in the body - is HTML valid if in body???
        val metaRefresh = Option(completedView.select("meta[http-equiv=refresh]").first())
        metaRefresh must not be None
      }
    }
  }
}
