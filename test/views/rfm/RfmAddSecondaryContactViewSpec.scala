package views.rfm

import base.ViewSpecBase
import forms.RfmAddSecondaryContactFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.RfmAddSecondaryContactView

class RfmAddSecondaryContactViewSpec extends ViewSpecBase {

  val formProvider = new RfmAddSecondaryContactFormProvider
  val page         = inject[RfmAddSecondaryContactView]

  val view = Jsoup.parse(page(formProvider(), "John Doe", NormalMode)(request, appConfig, messages).toString())

  "Rfm Add Secondary Contact View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(messages("rfm.addSecondaryContact.title"))
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include(messages("taskList.task.contact.heading"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("rfm.addSecondaryContact.heading").replace("{0}", "John Doe"))
    }

    "have a hint description" in {
      view.getElementsByClass("govuk-hint").get(0).text must include(messages("rfm.addSecondaryContact.hint"))
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include(messages("site.save-and-continue"))
    }
  }
}
