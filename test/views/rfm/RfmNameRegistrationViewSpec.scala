package views.rfm

import base.ViewSpecBase
import forms.RfmPrimaryContactNameFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.RfmNameRegistrationView

class RfmNameRegistrationViewSpec extends ViewSpecBase {

  val formProvider = new RfmPrimaryContactNameFormProvider
  val page         = inject[RfmNameRegistrationView]

  val view = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

  "Rfm Name Registration View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(
        "What is the name of the new nominated filing member?"
      )
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Group details")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(
        "What is the name of the new nominated filing member?"
      )
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
    }
  }
}
