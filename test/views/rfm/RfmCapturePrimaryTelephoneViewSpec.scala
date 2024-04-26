package views.rfm

import base.ViewSpecBase
import forms.RfmCaptureTelephoneDetailsFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.RfmCapturePrimaryTelephoneView

class RfmCapturePrimaryTelephoneViewSpec extends ViewSpecBase {

  val formProvider = new RfmCaptureTelephoneDetailsFormProvider
  val page         = inject[RfmCapturePrimaryTelephoneView]

  val view = Jsoup.parse(page(formProvider("John Doe"), NormalMode, "John Doe")(request, appConfig, messages).toString())

  "Rfm Capture Primary Telephone View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(messages("rfmCaptureTelephoneDetails.title"))
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include(messages("rfmCaptureTelephoneDetails.heading.caption"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("rfmCaptureTelephoneDetails.heading").replace("{0}", "John Doe"))
    }

    "have a hint description" in {
      view.getElementsByClass("govuk-hint").get(0).text must include(messages("rfmCaptureTelephoneDetails.hint"))
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include(messages("site.save-and-continue"))
    }
  }
}
