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
      view.getElementsByTag("title").text must include(messages("rfm.secondaryTelephonePreference.title"))
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include(messages("rfm.secondaryContactEmail.heading.caption"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("rfm.secondaryTelephonePreference.heading").replace("{0}", "John Doe"))
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include(messages("site.save-and-continue"))
    }
  }
}
