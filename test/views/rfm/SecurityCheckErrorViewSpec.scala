package views.rfm

import base.ViewSpecBase
import org.jsoup.Jsoup
import views.html.rfm.SecurityCheckErrorView

class SecurityCheckErrorViewSpec extends ViewSpecBase {

  val page = inject[SecurityCheckErrorView]

  val view = Jsoup.parse(page()(request, appConfig, messages).toString())

  "Security Check Error View" should {

    "have a title" in {
      val title = "You cannot replace the current filing member for this group - Report Pillar 2 top-up taxes - GOV.UK"
      view.getElementsByTag("title").text mustBe title
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("You cannot replace the current filing member for this group")
    }

    "have a paragraph body" in {
      view.getElementsByClass("govuk-body").first().text must include(
        "This service is for new nominated filing members to takeover the responsibilities from the current filing member."
      )
    }

    "have a link" in {
      val paragraphMessageWithLink = view.getElementsByClass("govuk-body").last()
      val link                     = paragraphMessageWithLink.getElementsByTag("a")

      paragraphMessageWithLink.text() must include(
        "If you need to manage who can access your Pillar 2 top-up tax returns, go to your business tax account."
      )
      link.text must include("go to your business tax account")
      link.attr("href") mustBe "https://www.gov.uk/guidance/sign-in-to-your-hmrc-business-tax-account"
    }

  }
}
