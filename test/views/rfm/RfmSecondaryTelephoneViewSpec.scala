package views.rfm

import base.ViewSpecBase
import forms.RfmSecondaryTelephoneFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.RfmSecondaryTelephoneView

class RfmSecondaryTelephoneViewSpec extends ViewSpecBase {

  val formProvider = new RfmSecondaryTelephoneFormProvider
  val page         = inject[RfmSecondaryTelephoneView]

  val view = Jsoup.parse(page(formProvider("John Doe"), NormalMode, "John Doe")(request, appConfig, messages).toString())

  "Rfm Secondary Telephone View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("What is the telephone number?")
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Contact details")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("What is the telephone number for John Doe?")
    }

    "have a hint" in {
      view.getElementsByClass("govuk-hint").text must include(
        "For international numbers include the country code."
      )
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
    }
  }
}
