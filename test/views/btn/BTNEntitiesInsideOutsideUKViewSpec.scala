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

package views.btn

import base.ViewSpecBase
import controllers.routes
import forms.BTNEntitiesInsideOutsideUKFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import views.html.btn.BTNEntitiesInsideOutsideUKView

class BTNEntitiesInsideOutsideUKViewSpec extends ViewSpecBase {
  lazy val formProvider: BTNEntitiesInsideOutsideUKFormProvider = new BTNEntitiesInsideOutsideUKFormProvider
  lazy val page:         BTNEntitiesInsideOutsideUKView         = inject[BTNEntitiesInsideOutsideUKView]
  lazy val pageTitle:    String                                 = "Does the group still have entities located in both the UK and outside the UK?"

  def view(isAgent: Boolean = false): Document =
    Jsoup.parse(page(formProvider(), isAgent, Some("orgName"), NormalMode)(request, appConfig, messages).toString())

  "BTN Entities Both In UK And Outside View" should {

    "have a title" in {
      view().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view().getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a banner with a link to the Homepage" in {
      val className: String = "govuk-header__link govuk-header__service-name"
      view().getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
      view(isAgent = true).getElementsByClass(className).attr("href") mustBe routes.HomepageController.onPageLoad().url
    }

    "have radio items" in {
      val radioButtons: Elements = view().getElementsByClass("govuk-label govuk-radios__label")

      radioButtons.size() mustBe 2
      radioButtons.get(0).text mustBe "Yes"
      radioButtons.get(1).text mustBe "No"
    }

    "have a 'Continue' button" in {
      val continueButton: Element = view().getElementsByClass("govuk-button").first()
      continueButton.text mustBe "Continue"
      continueButton.attr("type") mustBe "submit"
    }

    "have a caption displaying the organisation name for an agent view" in {
      view(isAgent = true).getElementsByClass("govuk-caption-m").text mustBe "orgName"
    }
  }
}
