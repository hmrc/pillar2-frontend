package views.repayments

import base.ViewSpecBase
import forms.RepaymentAccountNameConfirmationForm
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.repayments.AccountNameConfirmationView

class AccountNameConfirmationViewSpec extends ViewSpecBase {

  val page: AccountNameConfirmationView = inject[AccountNameConfirmationView]
  val formProvider = new RepaymentAccountNameConfirmationForm

  val view: Document = Jsoup.parse(page(formProvider(), "James", NormalMode)(request, appConfig, messages).toString())

  "Account Name Confirmation View" should {

    "have a title" in {
      val title = "Do you want to continue with these bank details? - Report Pillar 2 top-up taxes - GOV.UK"
      view.getElementsByTag("title").text mustBe title
    }

    "have a heading with the account holder's name" in {
      view.getElementsByTag("h2").first().text must include("This account belongs to James")
    }

    "have a subheading" in {
      val subheading = view.getElementsByTag("legend").text
      subheading must include("Do you want to continue with these bank details?")
    }

    "have a paragraph" in {
      view.getElementsByClass("govuk-body").first().text must include(
        "Is this who you want the refund to be sent to? If not, check the account details on your bank statement and try again."
      )
      view.getElementsByClass("govuk-body").get(1).text must include("We may not be able to recover your money if it goes to the wrong account.")
    }

    "have a yes or no form" in {
      view.getElementsByClass("govuk-radios__item").first().text must include("Yes")
      view.getElementsByClass("govuk-radios__item").get(1).text  must include("No")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Continue")
    }
  }
}
