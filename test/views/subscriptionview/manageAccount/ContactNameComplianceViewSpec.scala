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
import controllers.routes
import forms.ContactNameComplianceFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.data.Form
import views.html.subscriptionview.manageAccount.ContactNameComplianceView

class ContactNameComplianceViewSpec extends ViewSpecBase {

  lazy val formProvider = new ContactNameComplianceFormProvider
  lazy val form:      Form[String]              = formProvider()
  lazy val page:      ContactNameComplianceView = inject[ContactNameComplianceView]
  lazy val pageTitle: String                    = "Who should we contact about compliance for Pillar 2 Top-up Taxes?"
  def view(isAgent: Boolean = false, orgName: Option[String] = None): Document =
    Jsoup.parse(page(form, isAgent, orgName)(request, appConfig, messages).toString())

  "Contact Name Compliance View" should {

    "have a title" in {
      view().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view().getElementsByClass("govuk-caption-l").text mustBe "Contact details"
      view(isAgent = true, orgName = None).getElementsByClass("govuk-caption-l").text mustBe "Contact details"
      view(isAgent = true, orgName = Some("orgName")).getElementsByClass("govuk-caption-l").text mustBe "orgName"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view().getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      view().getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
      view(isAgent = true).getElementsByClass(className).attr("href") mustBe routes.DashboardController.onPageLoad.url
    }

    "display the hint text" in {
      view().getElementsByClass("govuk-hint").text mustBe "You can enter a person or team name."
    }

    "display the continue button" in {
      view().getElementsByClass("govuk-button").text mustBe "Continue"
    }

    "display an error summary when form has errors" in {
      val errorView: Document = Jsoup.parse(
        page(form.bind(Map("value" -> "")), isAgent = false, organisationName = None)(request, appConfig, messages).toString()
      )
      errorView.getElementsByClass("govuk-error-summary__title").text mustBe "There is a problem"
      errorView.getElementsByClass("govuk-list govuk-error-summary__list").text mustBe
        "Enter name of the person or team we should contact about compliance for Pillar 2 Top-up Taxes"
    }

    "display character limit error message when input exceeds maximum length" in {
      val longInput = "A" * 161
      val errorView: Document = Jsoup.parse(
        page(form.bind(Map("value" -> longInput)), isAgent = false, organisationName = None)(request, appConfig, messages).toString()
      )
      errorView.getElementsByClass("govuk-error-summary__title").text mustBe "There is a problem"
      errorView.getElementsByClass("govuk-list govuk-error-summary__list").text mustBe
        "Name of the contact person or team should be 160 characters or less"
    }

    "display XSS validation error messages when special characters are entered" in {
      val xssInput = Map(
        "value" -> "Test <script>alert('xss')</script> & Company"
      )

      val errorView: Document = Jsoup.parse(
        page(form.bind(xssInput), isAgent = false, organisationName = None)(request, appConfig, messages).toString()
      )

      errorView.getElementsByClass("govuk-error-summary__title").text mustBe "There is a problem"

      val errorList = errorView.getElementsByClass("govuk-list govuk-error-summary__list").text
      errorList mustBe "The name you enter must not include the following characters <, >, \" or &"

      val fieldErrors = errorView.getElementsByClass("govuk-error-message").text
      fieldErrors mustBe "Error: The name you enter must not include the following characters <, >, \" or &"
    }
  }
}
