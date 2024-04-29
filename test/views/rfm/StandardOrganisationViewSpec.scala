package views.rfm

import base.ViewSpecBase
import org.jsoup.Jsoup
import views.html.rfm.StandardOrganisationView

class StandardOrganisationViewSpec extends ViewSpecBase {

  val page = inject[StandardOrganisationView]

  val view = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Standard Organisation View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("Sorry, you’re unable to use this service")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Sorry, you’re unable to use this service")
    }

    "have a body" in {
      view.getElementsByClass("govuk-body").get(0).text must include(
        "You’ve signed in with a standard organisation " +
          "account."
      )

      view.getElementsByClass("govuk-body").get(1).text must include(
        "Only Government Gateway accounts with an " +
          "administrator role can replace their nominated filing member."
      )

      view.getElementsByClass("govuk-body").get(2).text must include(
        "Someone with an administrator’s Government " +
          "Gateway user ID who is the new nominated filing member will need to replace the current filing member."
      )
    }

    "have a link" in {
      view.getElementsByClass("govuk-link").text must include("Find out more about who can use this service")
    }
  }

}
