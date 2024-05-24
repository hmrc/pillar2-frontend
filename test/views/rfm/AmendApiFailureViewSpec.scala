package views.rfm

import base.ViewSpecBase
import org.jsoup.Jsoup
import views.html.rfm.AmendApiFailureView

class AmendApiFailureViewSpec extends ViewSpecBase {

  val page = inject[AmendApiFailureView]

  val view = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Amend Api Failure View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("Sorry, there is a problem with the service")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Sorry, there is a problem with the service")
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").get(0).text must include("Please try again later")
      view.getElementsByClass("govuk-body").get(1).text must
        include(
          "You can go back to replace the filing member for a Pillar 2 top-up taxes account to try again."
        )
    }
  }
}
