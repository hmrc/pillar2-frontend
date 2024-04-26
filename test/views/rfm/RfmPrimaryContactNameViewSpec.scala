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
      view.getElementsByTag("title").text must include(messages("rfm.rfmPrimaryContactName.title"))
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include(messages("rfm.rfmPrimaryContactName.caption"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("rfm.rfmPrimaryContactName.heading"))
    }

    "have a hint" in {
      view.getElementsByClass("govuk-hint").text must include(messages("rfm.rfmPrimaryContactName.hint"))
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include(messages("site.save-and-continue"))
    }
  }
}
