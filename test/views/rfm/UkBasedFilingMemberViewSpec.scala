package views.rfm

import base.ViewSpecBase
import forms.NFMRegisteredInUKConfirmationFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.UkBasedFilingMemberView

class UkBasedFilingMemberViewSpec extends ViewSpecBase {

  val formProvider = new NFMRegisteredInUKConfirmationFormProvider
  val page         = inject[UkBasedFilingMemberView]

  val view = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

  "Uk Based Filing Member View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(messages("isNFMRegisteredInUK.title"))
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include(messages("isNFMRegisteredInUK.heading.caption"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("isNFMRegisteredInUK.heading"))
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include(messages("site.save-and-continue"))
    }
  }

}
