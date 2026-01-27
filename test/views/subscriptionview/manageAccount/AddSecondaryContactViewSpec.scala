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
import forms.AddSecondaryContactFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.subscriptionview.manageAccount.AddSecondaryContactView

class AddSecondaryContactViewSpec extends ViewSpecBase {

  lazy val formProvider: AddSecondaryContactFormProvider = new AddSecondaryContactFormProvider
  lazy val page:         AddSecondaryContactView         = inject[AddSecondaryContactView]
  lazy val username:     String                          = "John Doe"
  lazy val view:         Document                        =
    Jsoup.parse(page(formProvider(username), username, isAgent = false, Some("OrgName"))(request, appConfig, messages).toString())
  lazy val pageTitle: String = "Add a secondary contact"

  "AddSecondaryContactView" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Contact details"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      view.getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
    }

    "have two description paragraphs" in {
      val paragraphs: Elements = view.getElementsByClass("govuk-body")

      paragraphs.get(0).text mustBe "We use the secondary contact if we do not get a response from the primary " +
        "contact. We encourage you to provide a secondary contact, if possible."

      paragraphs.get(1).text mustBe "This can be a team mailbox or another contact who is able to deal with " +
        "enquiries about the group’s management of Pillar 2 Top-up Taxes."
    }

    "have a legend heading" in {
      view.getElementsByClass("govuk-fieldset__heading").text mustBe "Do you have a second contact?"
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Continue"
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("view", view),
        ViewScenario(
          "agentViewNoOrg",
          Jsoup.parse(page(formProvider(username), username, isAgent = true, None)(request, appConfig, messages).toString())
        ),
        ViewScenario(
          "agentViewSomeOrg",
          Jsoup.parse(page(formProvider(username), username, isAgent = true, Some("OrgName"))(request, appConfig, messages).toString())
        )
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
