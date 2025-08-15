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

package views

import base.ViewSpecBase
import models.{CheckMode, NormalMode}
import models.tasklist.SectionStatus.{CannotStart, Completed, NotStarted}
import models.tasklist.SectionViewModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import views.html.TaskListView

class TaskListViewSpec extends ViewSpecBase {

  val groupDetailsSections: Seq[SectionViewModel] =
    Seq(
      SectionViewModel(
        "Edit Ultimate Parent Entity details",
        Some(controllers.registration.routes.StartPageRegistrationController.onPageLoad(CheckMode)),
        Completed
      ),
      SectionViewModel(
        "Add filing member details",
        Some(controllers.fm.routes.NominateFilingMemberYesNoController.onPageLoad(NormalMode)),
        NotStarted
      ),
      SectionViewModel("Further group details", None, CannotStart)
    )
  val contactDetailsSection: SectionViewModel = SectionViewModel("Contact details", None, CannotStart)
  val reviewAndSubmitSection: SectionViewModel =
    SectionViewModel("Check your answers before submitting your registration", None, CannotStart)
  val completedSectionCounter = 1

  lazy val page: TaskListView = inject[TaskListView]
  lazy val view: Document =
    Jsoup.parse(
      page(groupDetailsSections, contactDetailsSection, reviewAndSubmitSection, completedSectionCounter)(request, messages, appConfig).toString()
    )
  lazy val pageTitle: String = "Register your group"

  "Task List View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size() mustBe 1
      h1Elements.text() mustBe pageTitle
    }

    "have a paragraph" in {
      view.getElementsByClass("govuk-body").first.text mustBe
        "The information you enter will be saved as you progress. If you sign out, the information you have already entered will be saved for 28 days. After that time you will need to enter all of the information again."
    }

    "have a registration status H2 heading" in {
      view.getElementsByTag("h2").first.text mustBe "Registration incomplete"
    }

    "have a number of tasks completed paragraph" in {
      view.getElementsByClass("govuk-body").get(1).text mustBe "You have completed 1 of 5 tasks."
    }

    "have a group details H2" in {
      view.getElementsByTag("h2").get(1).text mustBe "Group details"
    }

    "have a group details section" in {
      view.getElementsByClass("app-task-list__item").get(1)
      val tasks    = view.getElementsByClass("app-task-list__task-name")
      val statuses = view.getElementsByClass("hmrc-status-tag")

      tasks.first.text mustBe "Edit Ultimate Parent Entity details"
      tasks.first.getElementsByTag("a").attr("href") mustBe controllers.registration.routes.StartPageRegistrationController.onPageLoad(CheckMode).url
      statuses.first.text mustBe "Completed"
      tasks.get(1).text mustBe "Add filing member details"
      tasks.get(1).getElementsByTag("a").attr("href") mustBe controllers.fm.routes.NominateFilingMemberYesNoController.onPageLoad(NormalMode).url
      statuses.get(1).text mustBe "Not started"
      tasks.get(2).text mustBe "Further group details"
      statuses.get(2).text mustBe "Cannot start yet"
    }

    "have a contact details H2" in {
      view.getElementsByTag("h2").get(2).text mustBe "Contact details"
    }

    "have a contact details section" in {
      val section = view.getElementsByClass("app-task-list__item").get(3)
      section.getElementsByClass("app-task-list__task-name").text mustBe "Contact details"
      section.getElementsByClass("hmrc-status-tag").text mustBe "Cannot start yet"
    }

    "have a review and submit H2" in {
      view.getElementsByTag("h2").get(3).text mustBe "Review and submit"
    }

    "have a review and submit section" in {
      val section = view.getElementsByClass("app-task-list__item").get(4)
      section.getElementsByClass("app-task-list__task-name").first.text mustBe "Check your answers before submitting your registration"
      section.getElementsByClass("hmrc-status-tag").first.text mustBe "Cannot start yet"
    }

    "have a review and submit paragraph" in {
      view.getElementsByClass("govuk-body").get(2).text mustBe
        "At the ‘Review and submit’ section of this registration, you can amend your answers and print or save them for your own records."
    }
  }
}
