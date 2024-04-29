package views.rfm

import base.ViewSpecBase
import forms.RfmSecondaryContactNameFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.RfmSecondaryContactNameView

class RfmSecondaryContactNameViewSpec extends ViewSpecBase {

  val formProvider = new RfmSecondaryContactNameFormProvider
  val page         = inject[RfmSecondaryContactNameView]

  val view = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

  "Rfm Secondary Contact Name View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(
        "What is the name of the person or team we should contact " +
          "about compliance with Pillar 2 top-up taxes?"
      )
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Contact details")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(
        "What is the name of the person or team we should contact about " +
          "compliance with Pillar 2 top-up taxes?"
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
