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
      view.getElementsByTag("title").text must include(messages("rfm-input-business-email.title"))
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include(messages("rfm-input-business-email.caption"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("rfm-input-business-email.heading").replace("{0}", "John Doe"))
    }

    "have a hint" in {
      view.getElementsByClass("govuk-hint").text must include(messages("rfm-input-business-email.hint"))
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include(messages("site.save-and-continue"))
    }
  }
}
