package views.rfm

import base.ViewSpecBase
import models.{NonUKAddress, NormalMode}
import org.jsoup.Jsoup
import org.mockito.Mockito.when
import pages.{RfmNameRegistrationPage, RfmRegisteredAddressPage}
import viewmodels.checkAnswers.{RfmNameRegistrationSummary, RfmRegisteredAddressSummary}
import views.html.rfm.RfmCheckYourAnswersView
import viewmodels.govuk.summarylist._

class RfmCheckYourAnswersViewSpec extends ViewSpecBase {
  val userName = "John Doe"
  val countryCode = "US"
  val nonUkAddress = NonUKAddress("addressLine1", None, "addressLine3", None, None, countryCode = countryCode)

  val userAnswer = emptyUserAnswers
    .setOrException(RfmNameRegistrationPage, userName)
    .setOrException(RfmRegisteredAddressPage, nonUkAddress)

  when(mockCountryOptions.getCountryNameFromCode(countryCode)).thenReturn("United States")

  val list = SummaryListViewModel(
    rows = Seq(
      RfmNameRegistrationSummary.row(userAnswer)(messages),
      RfmRegisteredAddressSummary.row(userAnswer, mockCountryOptions)(messages)
    ).flatten
  )

  val page = inject[RfmCheckYourAnswersView]

  val view = Jsoup.parse(page(NormalMode, list)(request, appConfig, messages).toString())

  "Rfm Check Your Answers View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(messages("rfm.rfmCheckYourAnswers.title"))
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text must include(messages("rfm.rfmCheckYourAnswers.heading.caption"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("rfm.rfmCheckYourAnswers.heading"))
    }

    "have a summary list keys" in {
      view.getElementsByClass("govuk-summary-list__key").get(0).text must include(messages("rfm.nameRegistration.checkYourAnswersLabel"))
      view.getElementsByClass("govuk-summary-list__key").get(1).text must include(messages("rfm.registeredAddress.checkYourAnswersLabel"))
    }

    "have a summary list items" in {
      view.getElementsByClass("govuk-summary-list__value").get(0).text must include(userName)
      view.getElementsByClass("govuk-summary-list__value").get(1).text must include(nonUkAddress.fullAddress + countryCode)
    }

    "have a summary list links" in {
      view.getElementsByClass("govuk-summary-list__actions").get(0).text must include("Change")
      view.getElementsByClass("govuk-summary-list__actions").get(1).text must include("Change")
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text must include(messages("site.confirm-and-continue"))
    }
  }
}
