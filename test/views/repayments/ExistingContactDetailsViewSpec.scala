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
import controllers.repayments.ExistingContactDetailsController
import forms.ExistingContactDetailsFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.repayments.ExistingContactDetailsView

class ExistingContactDetailsViewSpec extends ViewSpecBase {

  val contactName  = "John Doe"
  val contactEmail = "mail@mail.com"
  val contactTel   = "07123456789"

  val list = ExistingContactDetailsController.contactSummaryList(contactName, contactEmail, Some(contactTel))

  val formProvider = new ExistingContactDetailsFormProvider
  val page: ExistingContactDetailsView = inject[ExistingContactDetailsView]

  val view: Document = Jsoup.parse(page(formProvider(), list)(request, appConfig, messages).toString())

  "Existing Contact Details View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("Contact details")
    }

    "have headings" in {
      view.getElementsByTag("h1").get(0).text must include("Contact details")
      view.getElementsByTag("h1").get(1).text must include("Do you want to use these details for this refund request?")
    }

    "have labels for summary list" in {
      view.getElementsByClass("govuk-summary-list__key").get(0).text must include("Name")
      view.getElementsByClass("govuk-summary-list__key").get(1).text must include("Email")
      view.getElementsByClass("govuk-summary-list__key").get(2).text must include("Telephone")
    }

    "have values for summary list" in {
      view.getElementsByClass("govuk-summary-list__value").get(0).text must include(contactName)
      view.getElementsByClass("govuk-summary-list__value").get(1).text must include(contactEmail)
      view.getElementsByClass("govuk-summary-list__value").get(2).text must include(contactTel)
    }

    "have labels for radio button" in {
      view.getElementsByClass("govuk-radios__label").get(0).text must include("Yes")
      view.getElementsByClass("govuk-radios__label").get(1).text must include("No")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }
  }
}
