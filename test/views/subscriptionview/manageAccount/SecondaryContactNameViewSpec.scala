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
import forms.SecondaryContactNameFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.data.Form
import views.html.subscriptionview.manageAccount.SecondaryContactNameView

class SecondaryContactNameViewSpec extends ViewSpecBase {

  lazy val formProvider: SecondaryContactNameFormProvider = new SecondaryContactNameFormProvider
  lazy val form:         Form[String]                     = formProvider()
  lazy val page:         SecondaryContactNameView         = inject[SecondaryContactNameView]
  lazy val pageTitle:    String                           = "Who should we contact about compliance for Pillar 2 Top-up Taxes?"

  "Secondary Contact Name page" should {
    def view(isAgent: Boolean = false, orgName: Option[String] = None): Document = Jsoup.parse(
      page(form, isAgent, orgName)(request, appConfig, messages).toString()
    )

    "display the correct page title" in {
      view().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view().getElementsByClass("govuk-caption-l").text mustBe "Contact details"
      view(isAgent = true, orgName = None).getElementsByClass("govuk-caption-l").text mustBe "Contact details"
      view(isAgent = true, orgName = Some("orgName")).getElementsByClass("govuk-caption-l").text mustBe "orgName"
    }

    "display the main heading asking for alternative contact details" in {
      val h1Elements: Elements = view().getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      view().getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
      view(isAgent = true).getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
    }

    "include a helpful hint with examples" in {
      view().getElementsByClass("govuk-hint").text mustBe "You can enter a person or team name."
    }

    "display a 'Continue' button" in {
      view().getElementsByClass("govuk-button").text mustBe "Continue"
    }

    "show appropriate error when the name field is left empty" in {
      val errorView = Jsoup.parse(
        page(form.bind(Map("value" -> "")), isAgent = false, organisationName = None)(request, appConfig, messages).toString()
      )
      val errorSummary = errorView.getElementsByClass("govuk-error-summary").first()
      errorSummary.getElementsByClass("govuk-error-summary__title").first().text mustBe "There is a problem"

      val errorList = errorSummary.getElementsByClass("govuk-list govuk-error-summary__list").first()
      errorList.text mustBe
        "Enter the name of the alternative person or team we should contact about compliance for Pillar 2 Top-up Taxes"

      val fieldError = errorView.getElementsByClass("govuk-error-message")
      fieldError.text mustBe
        "Error: Enter the name of the alternative person or team we should contact about compliance for Pillar 2 Top-up Taxes"
    }

    "show error when name is too long (over 160 characters)" in {
      val longInput = "A" * 161
      val errorView = Jsoup.parse(
        page(form.bind(Map("value" -> longInput)), isAgent = false, organisationName = None)(request, appConfig, messages).toString()
      )
      val errorSummary = errorView.getElementsByClass("govuk-error-summary").first()
      errorSummary.getElementsByClass("govuk-error-summary__title").first().text mustBe "There is a problem"

      val errorList = errorSummary.getElementsByClass("govuk-list govuk-error-summary__list").first()
      errorList.text mustBe "Name of the alternative contact person or team should be 160 characters or less"

      val fieldError = errorView.getElementsByClass("govuk-error-message")
      fieldError.text mustBe "Error: Name of the alternative contact person or team should be 160 characters or less"
    }

    "show error when name contains invalid characters" in {
      val invalidInput = "Test <script>alert('xss')</script> & Company"
      val errorView    = Jsoup.parse(
        page(form.bind(Map("value" -> invalidInput)), isAgent = false, organisationName = None)(request, appConfig, messages).toString()
      )
      val errorSummary = errorView.getElementsByClass("govuk-error-summary").first()
      errorSummary.getElementsByClass("govuk-error-summary__title").first().text mustBe "There is a problem"

      val errorList = errorSummary.getElementsByClass("govuk-list govuk-error-summary__list").first()
      errorList.text mustBe
        "The name you enter must not include the following characters <, >, \" or &"

      val fieldError = errorView.getElementsByClass("govuk-error-message")
      fieldError.text mustBe
        "Error: The name you enter must not include the following characters <, >, \" or &"
    }
  }
}
