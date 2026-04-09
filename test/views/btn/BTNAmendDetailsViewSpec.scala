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
import models.MneOrDomestic
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.behaviours.ViewScenario
import views.html.btn.BTNAmendDetailsView

class BTNAmendDetailsViewSpec extends ViewSpecBase {
  lazy val plrReference:    String              = "XMPLR0123456789"
  lazy val page:            BTNAmendDetailsView = inject[BTNAmendDetailsView]
  lazy val pageTitle:       String              = "Based on your answer, you need to amend your details"
  lazy val pageTitleAgent:  String              = "Group details amend needed"
  lazy val bannerClassName: String              = "govuk-header__link govuk-header__service-name"

  lazy val organisationViewUkOnly: Document =
    Jsoup.parse(page(MneOrDomestic.Uk, plrReference, isAgent = false, Some("orgName"))(request, appConfig, messages).toString())
  lazy val organisationViewUkAndOther: Document =
    Jsoup.parse(page(MneOrDomestic.UkAndOther, plrReference, false, Some("orgName"))(request, appConfig, messages).toString())
  def agentViewUkOnly(organisationName: Option[String] = Some("orgName")): Document =
    Jsoup.parse(page(MneOrDomestic.Uk, plrReference, isAgent = true, organisationName)(request, appConfig, messages).toString())
  def agentViewUkAndOther(organisationName: Option[String] = Some("orgName")): Document =
    Jsoup.parse(page(MneOrDomestic.UkAndOther, plrReference, isAgent = true, organisationName)(request, appConfig, messages).toString())

  "BTNAmendDetailsView" when {
    "it's an organisation view" should {
      "have a title" in {
        organisationViewUkOnly.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
        organisationViewUkAndOther.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a unique H1 heading" in {
        val ukOnlyH1Elements: Elements = organisationViewUkOnly.getElementsByTag("h1")
        ukOnlyH1Elements.size() mustBe 1
        ukOnlyH1Elements.text() mustBe pageTitle

        val ukAndOtherH1Elements: Elements = organisationViewUkAndOther.getElementsByTag("h1")
        ukAndOtherH1Elements.size() mustBe 1
        ukAndOtherH1Elements.text() mustBe pageTitle
      }

      "have a banner with a link to the Homepage" in {
        organisationViewUkOnly.getElementsByClass(bannerClassName).attr("href") mustBe routes.HomepageController.onPageLoad().url
        organisationViewUkAndOther.getElementsByClass(bannerClassName).attr("href") mustBe routes.HomepageController.onPageLoad().url
      }

      "have paragraph content and link" in {
        val ukOnlyParagraphs:     Elements = organisationViewUkOnly.getElementsByClass("govuk-body")
        val ukAndOtherParagraphs: Elements = organisationViewUkAndOther.getElementsByClass("govuk-body")

        ukOnlyParagraphs.get(0).text() mustBe "You reported that your group only has entities in the UK."
        ukOnlyParagraphs.get(1).text() mustBe "If this has changed, you must amend your group details to " +
          "update the location of your entities before submitting a BTN."
        ukOnlyParagraphs.get(2).getElementsByTag("a").text mustBe "Amend group details"
        ukOnlyParagraphs.get(2).getElementsByTag("a").attr("href") mustBe
          controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url

        ukAndOtherParagraphs.get(0).text mustBe "You reported that your group has entities both in and outside of the UK."
        ukAndOtherParagraphs.get(1).text() mustBe "If this has changed, you must amend your group details to " +
          "update the location of your entities before submitting a BTN."
        ukAndOtherParagraphs.get(2).getElementsByTag("a").text mustBe "Amend group details"
        ukAndOtherParagraphs.get(2).getElementsByTag("a").attr("href") mustBe
          controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url
      }

      "have a back link" in {
        organisationViewUkOnly.getElementsByClass("govuk-back-link").text mustBe "Back"
        organisationViewUkAndOther.getElementsByClass("govuk-back-link").text mustBe "Back"
      }
    }

    "it's an agent view" should {
      "have a title" in {
        agentViewUkOnly().title() mustBe s"$pageTitleAgent - Report Pillar 2 Top-up Taxes - GOV.UK"
        agentViewUkAndOther().title() mustBe s"$pageTitleAgent - Report Pillar 2 Top-up Taxes - GOV.UK"
      }

      "have a caption" in {
        agentViewUkOnly().getElementsByClass("govuk-caption-m").text mustBe "Group: orgName ID: XMPLR0123456789"
        agentViewUkAndOther().getElementsByClass("govuk-caption-m").text mustBe "Group: orgName ID: XMPLR0123456789"
        agentViewUkOnly(organisationName = None).getElementsByClass("govuk-caption-m").text mustBe "ID: XMPLR0123456789"
        agentViewUkAndOther(organisationName = None).getElementsByClass("govuk-caption-m").text mustBe "ID: XMPLR0123456789"
      }

      "have a unique H1 heading" in {
        agentViewUkOnly().getElementsByTag("h1").text mustBe pageTitleAgent
        agentViewUkAndOther().getElementsByTag("h1").text mustBe pageTitleAgent
      }

      "have a banner with a link to the Homepage" in {
        agentViewUkOnly().getElementsByClass(bannerClassName).attr("href") mustBe routes.HomepageController.onPageLoad().url
        agentViewUkAndOther().getElementsByClass(bannerClassName).attr("href") mustBe routes.HomepageController.onPageLoad().url
      }

      "have paragraph content and link" in {
        val ukOnlyParagraphs:     Elements = agentViewUkOnly().getElementsByClass("govuk-body")
        val ukAndOtherParagraphs: Elements = agentViewUkAndOther().getElementsByClass("govuk-body")

        ukOnlyParagraphs.get(0).text() mustBe "You reported that the group only has entities in the UK."
        ukOnlyParagraphs.get(1).text() mustBe "If this has changed, you must amend the group details to update " +
          "the location of the entities before submitting a BTN."
        ukOnlyParagraphs.get(2).getElementsByTag("a").text mustBe "Amend group details"
        ukOnlyParagraphs.get(2).getElementsByTag("a").attr("href") mustBe
          controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url

        ukAndOtherParagraphs.get(0).text() mustBe "You reported that the group has entities both in and outside of the UK."
        ukAndOtherParagraphs.get(1).text() mustBe "If this has changed, you must amend the group details to update " +
          "the location of the entities before submitting a BTN."
        ukAndOtherParagraphs.get(2).getElementsByTag("a").text mustBe "Amend group details"
        ukAndOtherParagraphs.get(2).getElementsByTag("a").attr("href") mustBe
          controllers.subscription.manageAccount.routes.ManageGroupDetailsCheckYourAnswersController.onPageLoad().url
      }

      "have a back link" in {
        agentViewUkOnly().getElementsByClass("govuk-back-link").text mustBe "Back"
        agentViewUkAndOther().getElementsByClass("govuk-back-link").text mustBe "Back"
      }
    }

    val viewScenarios: Seq[ViewScenario] =
      Seq(
        ViewScenario("organisationViewUkOnly", organisationViewUkOnly),
        ViewScenario("organisationViewUkAndOther", organisationViewUkAndOther),
        ViewScenario("agentViewUkOnly", agentViewUkOnly()),
        ViewScenario("agentViewUkAndOther", agentViewUkAndOther()),
        ViewScenario("agentNoOrgViewUkOnly", agentViewUkOnly(organisationName = None)),
        ViewScenario("agentNoOrgViewUkAndOther", agentViewUkAndOther(organisationName = None))
      )

    behaveLikeAccessiblePage(viewScenarios)
  }
}
