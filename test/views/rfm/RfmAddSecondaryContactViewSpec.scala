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
      view.getElementsByTag("title").text must include("Is there someone else we can contact?")
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Contact details")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Is there someone else we can contact if John Doe is not available?")
    }

    "have a hint description" in {
      view.getElementsByClass("govuk-hint").get(0).text must include(
        "This can be a team mailbox or another contact " +
          "who is able to deal with enquiries about the group’s management of Pillar 2 top-up taxes."
      )
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
    }
  }
}
