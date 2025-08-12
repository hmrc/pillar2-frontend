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
import models.MneOrDomestic
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.btn.BTNAmendDetailsView

class BTNAmendDetailsViewSpec extends ViewSpecBase {

  lazy val page:           BTNAmendDetailsView = inject[BTNAmendDetailsView]
  lazy val pageTitle:      String              = "Based on your answer, you need to amend your details"
  lazy val pageTitleAgent: String              = "Group details amend needed"

  def viewUkOnly(isAgent: Boolean = false): Document =
    Jsoup.parse(page(MneOrDomestic.Uk, isAgent)(request, appConfig, messages).toString())
  def viewUkAndOther(isAgent: Boolean = false): Document =
    Jsoup.parse(page(MneOrDomestic.UkAndOther, isAgent)(request, appConfig, messages).toString())

  "BTNAmendDetailsView" when {
    "it's an organisation view" should {
      "have a title" in {
        viewUkOnly().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
        viewUkAndOther().title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val ukOnlyH1Elements: Elements = viewUkOnly().getElementsByTag("h1")
        ukOnlyH1Elements.size() mustBe 1
        ukOnlyH1Elements.text() mustBe pageTitle

        val ukAndOtherH1Elements: Elements = viewUkAndOther().getElementsByTag("h1")
        ukAndOtherH1Elements.size() mustBe 1
        ukAndOtherH1Elements.text() mustBe pageTitle
      }

      "have paragraph content and link" in {
        val ukOnlyParagraphs:     Elements = viewUkOnly().getElementsByClass("govuk-body")
        val ukAndOtherParagraphs: Elements = viewUkAndOther().getElementsByClass("govuk-body")

        ukOnlyParagraphs.get(0).text() mustBe "You reported that your group only has entities in the UK."
        ukOnlyParagraphs.get(1).text() mustBe "If this has changed, you must amend your group details to " +
          "update the location of your entities before submitting a BTN."
        ukOnlyParagraphs.get(2).getElementsByTag("a").text mustBe "Amend group details"
        ukOnlyParagraphs.get(2).getElementsByTag("a").attr("href") mustBe
          controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url

        ukAndOtherParagraphs.get(0).text mustBe "You reported that your group has entities both in and outside of the UK."
        ukAndOtherParagraphs.get(1).text() mustBe "If this has changed, you must amend your group details to " +
          "update the location of your entities before submitting a BTN."
        ukAndOtherParagraphs.get(2).getElementsByTag("a").text mustBe "Amend group details"
        ukAndOtherParagraphs.get(2).getElementsByTag("a").attr("href") mustBe
          controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url
      }

      "have a back link" in {
        viewUkOnly().getElementsByClass("govuk-back-link").text mustBe "Back"
        viewUkAndOther().getElementsByClass("govuk-back-link").text mustBe "Back"
      }
    }

    "it's an agent view" should {
      "have a title" in {
        viewUkOnly(isAgent = true).title() mustBe s"$pageTitleAgent - Report Pillar 2 Top-up Taxes - GOV.UK"
        viewUkAndOther(isAgent = true).title() mustBe s"$pageTitleAgent - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        viewUkOnly(isAgent = true).getElementsByTag("h1").text mustBe pageTitleAgent
        viewUkAndOther(isAgent = true).getElementsByTag("h1").text mustBe pageTitleAgent
      }

      "have paragraph content and link" in {
        val ukOnlyParagraphs:     Elements = viewUkOnly(isAgent = true).getElementsByClass("govuk-body")
        val ukAndOtherParagraphs: Elements = viewUkAndOther(isAgent = true).getElementsByClass("govuk-body")

        ukOnlyParagraphs.get(0).text() mustBe "You reported that the group only has entities in the UK."
        ukOnlyParagraphs.get(1).text() mustBe "If this has changed, you must amend the group details to update " +
          "the location of the entities before submitting a BTN."
        ukOnlyParagraphs.get(2).getElementsByTag("a").text mustBe "Amend group details"
        ukOnlyParagraphs.get(2).getElementsByTag("a").attr("href") mustBe
          controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url

        ukAndOtherParagraphs.get(0).text() mustBe "You reported that the group has entities both in and outside of the UK."
        ukAndOtherParagraphs.get(1).text() mustBe "If this has changed, you must amend the group details to update " +
          "the location of the entities before submitting a BTN."
        ukAndOtherParagraphs.get(2).getElementsByTag("a").text mustBe "Amend group details"
        ukAndOtherParagraphs.get(2).getElementsByTag("a").attr("href") mustBe
          controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad.url
      }

      "have a back link" in {
        viewUkOnly(isAgent = true).getElementsByClass("govuk-back-link").text mustBe "Back"
        viewUkAndOther(isAgent = true).getElementsByClass("govuk-back-link").text mustBe "Back"
      }
    }
  }
}
