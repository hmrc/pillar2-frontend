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
        "Enter a telephone number, like 01632 960 001, " +
          "07700 900 982. For international numbers include the country code, like +44 808 157 0192 or 0044 808 157 0192."
      )
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
    }
  }
}
