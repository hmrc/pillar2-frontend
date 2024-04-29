package views.rfm

import base.ViewSpecBase
import forms.RfmPrimaryContactEmailFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.RfmPrimaryContactEmailView

class RfmPrimaryContactEmailViewSpec extends ViewSpecBase {

  val formProvider = new RfmPrimaryContactEmailFormProvider
  val page         = inject[RfmPrimaryContactEmailView]

  val view = Jsoup.parse(page(formProvider("John Doe"), NormalMode, "John Doe")(request, appConfig, messages).toString())

  "Rfm Primary Contact Email View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("What is the email address?")
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Contact details")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("What is the email address forJohn Doe")
    }

    "have a hint" in {
      view.getElementsByClass("govuk-hint").text must include(
        "We will only use this to contact you about Pillar 2 " +
          "top-up taxes."
      )
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
    }
  }
}
