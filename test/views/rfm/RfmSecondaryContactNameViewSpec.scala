package views.rfm

import base.ViewSpecBase
import forms.RfmSecondaryContactNameFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.RfmSecondaryContactNameView

class RfmSecondaryContactNameViewSpec extends ViewSpecBase {

  val formProvider = new RfmSecondaryContactNameFormProvider
  val page         = inject[RfmSecondaryContactNameView]

  val view = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

  "Rfm Secondary Contact Name View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(messages("rfm.secondaryContactName.title"))
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include(messages("taskList.task.contact.heading"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("rfm.secondaryContactName.heading"))
    }

    "have a hint" in {
      view.getElementsByClass("govuk-hint").text must include(messages("rfm.secondaryContactName.hint"))
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include(messages("site.save-and-continue"))
    }
  }
}
