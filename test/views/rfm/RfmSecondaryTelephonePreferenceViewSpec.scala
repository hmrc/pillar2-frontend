package views.rfm

import base.ViewSpecBase
import forms.RfmSecondaryTelephonePreferenceFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.RfmSecondaryTelephonePreferenceView

class RfmSecondaryTelephonePreferenceViewSpec extends ViewSpecBase {

  val formProvider = new RfmSecondaryTelephonePreferenceFormProvider
  val page         = inject[RfmSecondaryTelephonePreferenceView]

  val view = Jsoup.parse(page(formProvider("John Doe"), NormalMode, "John Doe")(request, appConfig, messages).toString())

  "Rfm Secondary Telephone Preference View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("Can we contact by telephone?")
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Contact details")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Can we contact John Doe by telephone?")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
    }
  }
}
