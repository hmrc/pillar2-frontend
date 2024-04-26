package views.rfm

import base.ViewSpecBase
import forms.RfmContactAddressFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.RfmContactAddressView

class RfmContactAddressViewSpec extends ViewSpecBase {

  val formProvider = new RfmContactAddressFormProvider
  val page         = inject[RfmContactAddressView]

  val view = Jsoup.parse(page(formProvider(), NormalMode, Seq.empty)(request, appConfig, messages).toString())

  "Rfm Contact Address View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(messages("rfmContactAddress.title"))
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include(messages("rfmContactAddress.heading.caption"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("rfmContactAddress.heading"))
    }

    "have an address line 1 label" in {
      view.getElementsByClass("govuk-label").get(0).text must include(messages("rfmContactAddress.addressLine1"))
    }

    "have an address line 2 label" in {
      view.getElementsByClass("govuk-label").get(1).text must include(messages("rfmContactAddress.addressLine2"))
    }

    "have a town or city label" in {
      view.getElementsByClass("govuk-label").get(2).text must include(messages("rfmContactAddress.town_city"))
    }

    "have a region label" in {
      view.getElementsByClass("govuk-label").get(3).text must include(messages("rfmContactAddress.region"))
    }

    "have a postcode label" in {
      view.getElementsByClass("govuk-label").get(4).text must include(messages("rfmContactAddress.postcode"))
    }

    "have a country label" in {
      view.getElementsByClass("govuk-label").get(5).text must include(messages("rfmContactAddress.country"))
    }

    "have a country hint" in {
      view.getElementById("countryCode-hint").text must include(messages("rfmContactAddress.country.hint"))
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include(messages("site.save-and-continue"))
    }
  }

}
