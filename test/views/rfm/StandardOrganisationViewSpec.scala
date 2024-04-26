package views.rfm

import base.ViewSpecBase
import org.jsoup.Jsoup
import views.html.rfm.StandardOrganisationView

class StandardOrganisationViewSpec extends ViewSpecBase {

  val page = inject[StandardOrganisationView]

  val view = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Standard Organisation View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(messages("rfm.standardOrganisation.title"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("rfm.standardOrganisation.heading"))
    }

    "have a body" in {
      view.getElementsByClass("govuk-body").get(0).text must include(messages("rfm.standardOrganisation.p1"))
      view.getElementsByClass("govuk-body").get(1).text must include(messages("rfm.standardOrganisation.p2"))
      view.getElementsByClass("govuk-body").get(2).text must include(messages("rfm.standardOrganisation.p3"))
    }

    "have a link" in {
      view.getElementsByClass("govuk-link").text must include(messages("rfm.standardOrganisation.link"))
    }
  }

}
