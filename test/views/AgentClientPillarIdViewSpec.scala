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
      view.getElementsByTag("title").text must include(messages("agent.pillar2Ref.title"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("agent.pillar2Ref.heading"))
    }

    "have a hint" in {
      view.getElementById("value-hint").text must include(messages("agent.pillar2Ref.hint"))
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include(messages("site.continue"))
    }

  }

}
