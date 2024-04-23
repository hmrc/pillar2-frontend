package views

import base.ViewSpecBase
import forms.AgentClientPillar2ReferenceFormProvider
import org.jsoup.Jsoup
import views.html.AgentClientPillarIdView

class AgentClientPillarIdViewSpec extends ViewSpecBase {

  val formProvider = new AgentClientPillar2ReferenceFormProvider
  val page         = inject[AgentClientPillarIdView]

  val view = Jsoup.parse(page(formProvider())(request, appConfig, messages).toString())

  "Agent Client PillarId View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("What is your client’s Pillar 2 top-up taxes ID?")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("What is your client’s Pillar 2 top-up taxes ID?")
    }

    "have a hint" in {
      view.getElementById("value-hint").text must include(
        "This is 15 characters, for example, XMPLR0123456789. The current filing member can find it on their Pillar 2 taxes top-up homepage."
      )
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }

  }

}
