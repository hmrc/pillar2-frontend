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
import forms.ContactEmailAddressFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.data.Form
import views.html.subscriptionview.manageAccount.ContactEmailAddressView

class ContactEmailAddressViewSpec extends ViewSpecBase {

  lazy val formProvider: ContactEmailAddressFormProvider = new ContactEmailAddressFormProvider
  lazy val form:         Form[String]                    = formProvider("Test Contact")
  lazy val page:         ContactEmailAddressView         = inject[ContactEmailAddressView]
  lazy val contactName:  String                          = "Test Contact"
  lazy val pageTitle:    String                          = "What is the email address?"

  "ContactEmailAddressView" should {
    def view(isAgent: Boolean = false, orgName: Option[String] = None): Document = Jsoup.parse(
      page(form, contactName, isAgent, orgName)(request, appConfig, messages).toString()
    )

    "have a title" in {
      view().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view().getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe s"What is the email address for $contactName?"
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      view().getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad.url
      view(isAgent = true).getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad.url
    }

    "have a caption" in {
      view().getElementsByClass("govuk-caption-l").text mustBe "Contact details"
      view(isAgent = true, orgName = None).getElementsByClass("govuk-caption-l").text mustBe "Contact details"
      view(isAgent = true, orgName = Some("orgName")).getElementsByClass("govuk-caption-l").text mustBe "orgName"
    }

    "have a hint description" in {
      view().getElementsByClass("govuk-hint").text.contains("only use this to contact you about Pillar 2 Top-up Taxes") mustBe true
    }

    "have a button" in {
      view().getElementsByClass("govuk-button").text mustBe "Continue"
    }

    "show appropriate error when the email field is left empty" in {
      val errorView = Jsoup.parse(
        page(form.bind(Map("emailAddress" -> "")), isAgent = false, UserName = contactName, organisationName = None)(
          request,
          appConfig,
          messages
        ).toString()
      )
      val errorSummary = errorView.getElementsByClass("govuk-error-summary").first()
      errorSummary.getElementsByClass("govuk-error-summary__title").first().text mustBe "There is a problem"

      val errorList = errorSummary.getElementsByClass("govuk-list govuk-error-summary__list").first()
      errorList.text mustBe s"Enter the email address for $contactName"

      val fieldError = errorView.getElementsByClass("govuk-error-message")
      fieldError.text mustBe s"Error: Enter the email address for $contactName"
    }

    "show error when email format is invalid" in {
      val errorView = Jsoup.parse(
        page(form.bind(Map("emailAddress" -> "invalid-email")), isAgent = false, UserName = contactName, organisationName = None)(
          request,
          appConfig,
          messages
        ).toString()
      )
      val errorSummary = errorView.getElementsByClass("govuk-error-summary").first()
      errorSummary.getElementsByClass("govuk-error-summary__title").first().text mustBe "There is a problem"

      val errorList = errorSummary.getElementsByClass("govuk-list govuk-error-summary__list").first()
      errorList.text mustBe "Enter an email address in the correct format, like name@example.com"

      val fieldError = errorView.getElementsByClass("govuk-error-message")
      fieldError.text mustBe "Error: Enter an email address in the correct format, like name@example.com"
    }

    "show error when email is too long (over 132 characters)" in {
      val longEmail = "a" * 130 + "@email.com"
      val errorView = Jsoup.parse(
        page(form.bind(Map("emailAddress" -> longEmail)), isAgent = false, UserName = contactName, organisationName = None)(
          request,
          appConfig,
          messages
        ).toString()
      )
      val errorSummary = errorView.getElementsByClass("govuk-error-summary").first()
      errorSummary.getElementsByClass("govuk-error-summary__title").first().text mustBe "There is a problem"

      val errorList = errorSummary.getElementsByClass("govuk-list govuk-error-summary__list").first()
      errorList.text mustBe "Email address must be 132 characters or less"

      val fieldError = errorView.getElementsByClass("govuk-error-message")
      fieldError.text mustBe "Error: Email address must be 132 characters or less"
    }

  }
}
