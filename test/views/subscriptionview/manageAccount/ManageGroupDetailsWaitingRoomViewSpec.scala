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

package views.subscription.manageAccount

import base.ViewSpecBase
import models.subscription.ManageGroupDetailsStatus.SuccessfullyCompleted
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.subscriptionview.manageAccount.ManageGroupDetailsWaitingRoomView

class ManageGroupDetailsWaitingRoomViewSpec extends ViewSpecBase {

  val page: ManageGroupDetailsWaitingRoomView = inject[ManageGroupDetailsWaitingRoomView]
  val view: Document                          = Jsoup.parse(page(Some(SuccessfullyCompleted))(request, appConfig, messages).toString())

  "Manage Group Details Waiting Room View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(messages("Submitting your group details"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("Submitting your group details"))
    }

    "have a sub heading" in {
      view.getElementsByTag("h2").text must include("Do not press back in your browser or leave this page.")
    }

  }
}
