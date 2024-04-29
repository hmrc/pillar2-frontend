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
      view.getElementsByTag("title").text must
        include("We need to match the details of the new nominated filing member to HMRC records")
    }

    "have a caption" in {
      view.getElementById("section-header").text must include("Group details")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must
        include("We need to match the details of the new nominated filing member to HMRC records")
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").get(0).text must
        include(
          "If the new filing member is registered in the UK, we will ask you for identifying " +
            "information so we can best match it with our records."
        )

      view.getElementsByClass("govuk-body").get(1).text must
        include(
          "If the new filing member is registered outside of the UK or if they are not a listed entity type, " +
            "we will ask you for identifying information so we can create a new HMRC record."
        )
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }
  }
}
