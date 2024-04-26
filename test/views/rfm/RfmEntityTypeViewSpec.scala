package views.rfm

import base.ViewSpecBase
import forms.RfmEntityTypeFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.RfmEntityTypeView

class RfmEntityTypeViewSpec extends ViewSpecBase {

  val formProvider = new RfmEntityTypeFormProvider
  val page         = inject[RfmEntityTypeView]

  val view = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

  "Rfm Entity Type View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(messages("rfmEntityType.title"))
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include(messages("rfmEntityType.caption"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("rfmEntityType.heading"))
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include(messages("site.save-and-continue"))
    }
  }
}
