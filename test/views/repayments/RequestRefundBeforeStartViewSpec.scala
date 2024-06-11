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

package views.repayments

import base.ViewSpecBase
import org.jsoup.Jsoup
import views.html.repayments.RequestRefundBeforeStartView

class RequestRefundBeforeStartViewSpec extends ViewSpecBase {

  val page = inject[RequestRefundBeforeStartView]

  val view = Jsoup.parse(page(Some("Pillar2id"))(request, appConfig, messages).toString())

  "Request Refund Before Start View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("Request a refund")
    }

    "have a h1 heading" in {
      view.getElementsByTag("h1").text must include("Request a refund")
    }

    "have two h2 headings" in {
      view.getElementsByTag("h2").text must include("Deadline for refund request")
      view.getElementsByTag("h2").text must include("Before you start")
    }

    "have following contents" in {
      view.getElementsByClass("govuk-body").text must include(
        "You can use this service to request a refund if you think you paid too much Pillar 2 top-up tax."
      )

      view.getElementsByClass("govuk-body").text must include(
        "The submission deadline for a refund request is 4 years after the end date of the accounting period in respect of which the payment was made."
      )

      view.getElementsByClass("govuk-body").text must include(
        "You will need the following details:"
      )
      view.getElementsByTag("li").text must include("refund amount")
      view.getElementsByTag("li").text must include("reason for your refund request")
      view.getElementsByTag("li").text must include("bank account details")
      view.getElementsByTag("li").text must include("contact details for someone we can contact about this request")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Request a refund")
    }

  }
}
