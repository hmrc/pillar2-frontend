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
import views.html.subscriptionview.manageAccount.ManageContactDetailsWaitingRoomView

class ManageContactDetailsWaitingRoomViewSpec extends ViewSpecBase {

  val page: ManageContactDetailsWaitingRoomView = inject[ManageContactDetailsWaitingRoomView]

  val inProgressView: Document = Jsoup.parse(page(Some(ManageContactDetailsStatus.InProgress))(request, appConfig, messages).toString())
  val completedView:  Document = Jsoup.parse(page(Some(ManageContactDetailsStatus.SuccessfullyCompleted))(request, appConfig, messages).toString())

  "Manage Contact Details Waiting Room View" should {

    "when status is InProgress" must {

      "have correct title" in {

        inProgressView.getElementsByTag("title").text must include("Submitting your contact details")
      }

      "have correct heading" in {
        inProgressView.getElementsByTag("h1").text must include("Submitting your contact details")
      }

      "have a sub heading" in {
        inProgressView.getElementsByTag("h2").text must include(
          "Do not press back in your browser or leave this page. It may take up to a minute to process this change."
        )
      }

      "display spinner" in {

        inProgressView.getElementsByClass("hods-loading-spinner__spinner").size() must be > 0
      }

      "have a meta refresh tag" in {

        val metaRefresh = Option(inProgressView.select("meta[http-equiv=refresh]").first())
        metaRefresh must not be None
      }
    }

    "when status is SuccessfullyCompleted" must {

      "have correct title" in {
        completedView.getElementsByTag("title").text must include("Submitting your contact details")
      }

      "have correct heading" in {
        completedView.getElementsByTag("h1").text must include("Submitting your contact details")
      }

      "have a sub heading" in {
        completedView.getElementsByTag("h2").text must include(
          "Do not press back in your browser or leave this page. It may take up to a minute to process this change."
        )
      }

      "have a meta refresh tag" in {
        val metaRefresh = Option(completedView.select("meta[http-equiv=refresh]").first())
        metaRefresh must not be None
      }
    }
  }
}
