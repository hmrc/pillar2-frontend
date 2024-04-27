package views

import base.ViewSpecBase
import controllers.routes
import org.jsoup.Jsoup
import views.html.AgentClientNoMatch

class AgentClientNoMatchViewSpec extends ViewSpecBase {

  val page = inject[AgentClientNoMatch]

  val view = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Agent Client No Match View" should {

    "have a title" in {
      val title = "Your client’s details did not match HMRC records - Report Pillar 2 top-up taxes - GOV.UK"
      view.getElementsByTag("title").text mustBe title
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Your client’s details did not match HMRC records")
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").first().text must include("We could not match the details you entered with records held by HMRC.")
    }

    "have a link" in {
      val link = view.getElementsByClass("govuk-body").last().getElementsByTag("a")

      link.text         must include("Re-enter your client’s Pillar 2 top-up taxes ID to try again")
      link.attr("href") must include(routes.AgentController.onPageLoadClientPillarId.url)
    }

  }
}
