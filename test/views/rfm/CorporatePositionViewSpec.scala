package views.rfm

import base.ViewSpecBase
import forms.RfmCorporatePositionFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.rfm.CorporatePositionView

class CorporatePositionViewSpec extends ViewSpecBase {

  val formProvider = new RfmCorporatePositionFormProvider
  val page         = inject[CorporatePositionView]

  val view = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

  "Corporate Position View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("What is your position in the corporate structure of the group?")
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Replace filing member")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("What is your position in the corporate structure of the group?")
    }

    "have radio items" in {
      view.getElementsByClass("govuk-label govuk-radios__label").get(0).text must include("New nominated filing member")
      view.getElementsByClass("govuk-label govuk-radios__label").get(1).text must include("Ultimate parent entity (UPE)")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Save and continue")
    }
  }
}
