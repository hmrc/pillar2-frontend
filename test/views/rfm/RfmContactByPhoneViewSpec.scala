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
import forms.RfmContactByPhoneFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.api.mvc.{AnyContent, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import views.html.rfm.RfmContactByPhoneView

class RfmContactByPhoneViewSpec extends ViewSpecBase {

  lazy val formProvider = new RfmContactByPhoneFormProvider
  lazy val page:     RfmContactByPhoneView = inject[RfmContactByPhoneView]
  lazy val username: String                = "John Doe"
  lazy val rfmRequest: Request[AnyContent] =
    FakeRequest("GET", controllers.rfm.routes.RfmContactByPhoneController.onPageLoad(NormalMode).url).withCSRFToken
  lazy val view:      Document = Jsoup.parse(page(formProvider(username), NormalMode, username)(rfmRequest, appConfig, messages).toString())
  lazy val pageTitle: String   = "Can we contact by phone"

  "Rfm Contact By Phone View" should {

    "have a title" in {
      view.getElementsByTag("title").text mustBe "Can we contact by phone?"
      view.title() mustBe s"$pageTitle? - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a non-clickable banner" in {
      val serviceName = view.getElementsByClass("govuk-header__service-name").first()
      serviceName.text mustBe "Report Pillar 2 Top-up Taxes"
      serviceName.getElementsByTag("a") mustBe empty
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Contact details"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe s"Can we contact $username by phone?"
    }

    "have radio items" in {
      val radioItems: Elements = view.getElementsByClass("govuk-label govuk-radios__label")
      radioItems.get(0).text mustBe "Yes"
      radioItems.get(1).text mustBe "No"
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Save and continue"
    }
  }

  "when form is submitted with a missing value" should {
    val errorView: Document = Jsoup.parse(
      page(
        formProvider(username).bind(
          Map(
            "value" -> ""
          )
        ),
        NormalMode,
        username
      )(request, appConfig, messages).toString()
    )

    "show missing values error summary" in {
      val errorSummaryElements: Elements = errorView.getElementsByClass("govuk-error-summary")
      errorSummaryElements.size() mustBe 1

      val errorSummary: Element  = errorSummaryElements.first()
      val errorsList:   Elements = errorSummary.getElementsByTag("li")

      errorSummary.getElementsByClass("govuk-error-summary__title").text() mustBe "There is a problem"

      errorsList.get(0).text() mustBe "Select yes if we can contact John Doe by phone"
    }

    "show field-specific errors" in {
      val fieldErrors: Elements = errorView.getElementsByClass("govuk-error-message")

      fieldErrors.get(0).text() mustBe "Error: Select yes if we can contact John Doe by phone"
    }
  }
}
