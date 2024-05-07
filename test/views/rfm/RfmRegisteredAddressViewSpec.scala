package views.rfm

import base.ViewSpecBase
import forms.RfmRegisteredAddressFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.RfmRegisteredAddressView

class RfmRegisteredAddressViewSpec extends ViewSpecBase {

  val formProvider = new RfmRegisteredAddressFormProvider
  val page         = inject[RfmRegisteredAddressView]

  val view = Jsoup.parse(page(formProvider(), NormalMode, "John Doe", Seq.empty)(request, appConfig, messages).toString())

  "Rfm Registered Address View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("What is the registered office address?")
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Group details")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("What is the registered office address of John Doe?")
    }

    "have an address line 1 label" in {
      view.getElementsByClass("govuk-label").get(0).text must include("Address line 1")
    }

    "have an address line 2 label" in {
      view.getElementsByClass("govuk-label").get(1).text must include("Address line 2 (optional)")
    }

    "have a town or city label" in {
      view.getElementsByClass("govuk-label").get(2).text must include("Town or city")
    }

    "have a region label" in {
      view.getElementsByClass("govuk-label").get(3).text must include("Region (optional)")
    }

    "have a postcode label" in {
      view.getElementsByClass("govuk-label").get(4).text must include("Postal code")
    }

    "have a country label" in {
      view.getElementsByClass("govuk-label").get(5).text must include("Country")
    }

    "have a country hint" in {
      view.getElementById("countryCode-hint").text must include("Enter text and then choose from the list.")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
    }
  }

}
