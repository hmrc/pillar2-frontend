package views.rfm

import base.ViewSpecBase
import forms.RfmSecondaryContactEmailFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.RfmSecondaryContactEmailView

class RfmSecondaryContactEmailViewSpec extends ViewSpecBase {

  val formProvider = new RfmSecondaryContactEmailFormProvider
  val page         = inject[RfmSecondaryContactEmailView]

  val view = Jsoup.parse(page(formProvider("John Doe"), NormalMode, "John Doe")(request, appConfig, messages).toString())

  "Rfm Secondary Contact Email View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(messages("rfm.secondaryContactEmail.title"))
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include(messages("rfm.secondaryContactEmail.heading.caption"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("rfm.secondaryContactEmail.heading").replace("{0}", "John Doe"))
    }

    "have a hint" in {
      view.getElementsByClass("govuk-hint").text must include(messages("rfm.secondaryContactEmail.hint"))
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include(messages("site.save-and-continue"))
    }
  }
}
