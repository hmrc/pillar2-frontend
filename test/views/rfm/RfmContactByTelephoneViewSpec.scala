package views.rfm

import base.ViewSpecBase
import forms.RfmContactByTelephoneFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.RfmContactByTelephoneView

class RfmContactByTelephoneViewSpec extends ViewSpecBase {

  val formProvider = new RfmContactByTelephoneFormProvider
  val page         = inject[RfmContactByTelephoneView]

  val view = Jsoup.parse(page(formProvider("John Doe"), NormalMode, "John Doe")(request, appConfig, messages).toString())

  "Rfm Contact By Telephone View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(messages("rfmContactByTelephone.title"))
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include(messages("rfmContactByTelephone.heading.caption"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("rfmContactByTelephone.heading").replace("{0}", "John Doe"))
    }

    "have radio items" in {
      view.getElementsByClass("govuk-label govuk-radios__label").get(0).text must include("Yes")
      view.getElementsByClass("govuk-label govuk-radios__label").get(1).text must include("No")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include(messages("site.save-and-continue"))
    }
  }
}
