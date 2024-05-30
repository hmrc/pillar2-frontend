package views.repayments

import base.ViewSpecBase
import forms.NonUKBankFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import views.html.repayments.NonUKBankView

class NonUKBankViewSpec extends ViewSpecBase {

  val formProvider = new NonUKBankFormProvider
  val page         = inject[NonUKBankView]

  val view = Jsoup.parse(page(formProvider(), NormalMode)(request, appConfig, messages).toString())

  "Non UK Bank View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("Bank account details")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Bank account details")
    }

    "have a label" in {
      view.getElementsByClass("govuk-label").get(0).text must include("Name of the bank")
      view.getElementsByClass("govuk-label").get(1).text must include("Name on the account")
      view.getElementsByClass("govuk-label").get(2).text must include("BIC or SWIFT code")
      view.getElementsByClass("govuk-label").get(3).text must include("IBAN")
    }

    "have a hint description" in {
      view.getElementsByClass("govuk-hint").get(0).text must include("Must be a business account.")
      view.getElementsByClass("govuk-hint").get(1).text must include("Must be a business account.")
      view.getElementsByClass("govuk-hint").get(2).text must include(
        "Must be between 8 and 11 characters. " +
          "You can ask your bank or check your bank statement."
      )
      view.getElementsByClass("govuk-hint").get(3).text must include(
        "You can ask your bank or check your " +
          "bank statement."
      )
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }
  }
}
