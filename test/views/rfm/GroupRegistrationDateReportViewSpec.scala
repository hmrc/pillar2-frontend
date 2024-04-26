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
      view.getElementsByTag("title").text must include(messages("groupRegistrationDateReport.title"))
    }

    "have a caption" in {
      view.getElementById("section-header").text must include(messages("groupRegistrationDateReport.heading.caption"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("groupRegistrationDateReport.heading"))
    }

    "have a hint description" in {
      view.getElementsByClass("govuk-hint").get(0).text must include(messages("groupRegistrationDateReport.hint.desc"))
    }

    "have a registration date hint" in {
      view.getElementsByClass("govuk-hint").get(1).text must include(messages("groupRegistrationDateReport.registrationDate.hint"))
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include(messages("site.continue"))
    }
  }
}
