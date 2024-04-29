package views.rfm

import base.ViewSpecBase
import forms.GroupRegistrationDateReportFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.GroupRegistrationDateReportView

class GroupRegistrationDateReportViewSpec extends ViewSpecBase {

  val formProvider = new GroupRegistrationDateReportFormProvider
  val page         = inject[GroupRegistrationDateReportView]

  val view = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

  "Group Registration Date Report View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(
        "Enter the group’s registration date to the Report Pillar 2 " +
          "top-up taxes service"
      )
    }

    "have a caption" in {
      view.getElementById("section-header").text must include("Replace filing member")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(
        "Enter the group’s registration date to the Report Pillar 2 " +
          "top-up taxes service"
      )
    }

    "have a hint description" in {
      view.getElementsByClass("govuk-hint").get(0).text must include(
        "This will be the date when your group first " +
          "registered to report their pillar 2 taxes in the UK."
      )
    }

    "have a registration date hint" in {
      view.getElementsByClass("govuk-hint").get(1).text must include("For example, 27 3 2026")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }
  }
}
