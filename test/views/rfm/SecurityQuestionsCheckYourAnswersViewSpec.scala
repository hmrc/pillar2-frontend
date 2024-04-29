package views.rfm

import base.ViewSpecBase
import models.NormalMode
import models.rfm.RegistrationDate
import org.jsoup.Jsoup
import pages.{RfmPillar2ReferencePage, RfmRegistrationDatePage}
import viewmodels.checkAnswers.RfmRegistrationDateSummary.dateHelper
import viewmodels.checkAnswers.{RfmRegistrationDateSummary, RfmSecurityCheckSummary}
import viewmodels.govuk.summarylist._
import views.html.rfm.SecurityQuestionsCheckYourAnswersView

class SecurityQuestionsCheckYourAnswersViewSpec extends ViewSpecBase {
  val plrReference = "XE1111123456789"

  val userAnswer = emptyUserAnswers
    .setOrException(RfmPillar2ReferencePage, plrReference)
    .setOrException(RfmRegistrationDatePage, RegistrationDate(registrationDate))

  val list = SummaryListViewModel(
    rows = Seq(
      RfmSecurityCheckSummary.row(userAnswer)(messages),
      RfmRegistrationDateSummary.row(userAnswer)(messages)
    ).flatten
  )

  val page = inject[SecurityQuestionsCheckYourAnswersView]

  val view = Jsoup.parse(page(NormalMode, list)(request, appConfig, messages).toString())

  "Security Questions Check Your Answers View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include("Check Your Answers")
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include("Replace filing member")
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include("Check your answers")
    }

    "have a summary list keys" in {
      view.getElementsByClass("govuk-summary-list__key").get(0).text must include("Pillar 2 top-up taxes ID")
      view.getElementsByClass("govuk-summary-list__key").get(1).text must include("Registration date")
    }

    "have a summary list items" in {
      view.getElementsByClass("govuk-summary-list__value").get(0).text must include(plrReference)
      view.getElementsByClass("govuk-summary-list__value").get(1).text must include(dateHelper.formatDateGDS(registrationDate))
    }

    "have a summary list links" in {
      view.getElementsByClass("govuk-summary-list__actions").get(0).text must include("Change")
      view.getElementsByClass("govuk-summary-list__actions").get(1).text must include("Change")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include("Confirm and continue")
    }
  }

}
