package views.rfm

import base.ViewSpecBase
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.CheckNewFilingMemberView

class CheckNewFilingMemberViewSpec extends ViewSpecBase {

  val page = inject[CheckNewFilingMemberView]

  val view = Jsoup.parse(page(NormalMode)(request, appConfig, messages).toString())

  "Check New Filing Member View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(messages("checkNewFilingMember.title"))
    }

    "have a caption" in {
      view.getElementById("section-header").text must include(messages("checkNewFilingMember.heading.caption"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("checkNewFilingMember.heading"))
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").get(0).text must include(messages("checkNewFilingMember.p1"))
      view.getElementsByClass("govuk-body").get(1).text must include(messages("checkNewFilingMember.p2"))
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include(messages("site.continue"))
    }
  }
}
