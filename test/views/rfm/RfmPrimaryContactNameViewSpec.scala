package views.rfm

import base.ViewSpecBase
import forms.RfmPrimaryContactNameFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.RfmPrimaryContactNameView

class RfmPrimaryContactNameViewSpec extends ViewSpecBase {

  val formProvider = new RfmPrimaryContactNameFormProvider
  val page         = inject[RfmPrimaryContactNameView]

  val view = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

  "Rfm Primary Contact Name View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(
        "What is the name of the person or team we should contact " +
          "about Pillar 2 top-up taxes?"
      )
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Contact details")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(
        "What is the name of the person or team we should contact about " +
          "Pillar 2 top-up taxes?"
      )
    }

    "have a hint" in {
      view.getElementsByClass("govuk-hint").text must include("For example, ‘Tax team’ or ‘Ashley Smith’.")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
    }
  }
}
