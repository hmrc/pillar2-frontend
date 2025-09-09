/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package views.rfm

import base.ViewSpecBase
import models.rfm.CorporatePosition.Upe
import models.{CheckMode, NonUKAddress, UserAnswers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.mockito.Mockito.when
import pages._
import play.api.mvc.{AnyContent, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.rfm.RfmContactCheckYourAnswersView

class RfmContactCheckYourAnswersViewSpec extends ViewSpecBase {

  lazy val countryCode: String = "GB"
  lazy val country:     String = "United Kingdom"

  lazy val contactAddress: NonUKAddress = NonUKAddress(
    addressLine1 = "RFM Address Line 1",
    addressLine2 = None,
    addressLine3 = "RFM City",
    addressLine4 = None,
    postalCode = Some("EH5 5WY"),
    countryCode = "GB"
  )

  lazy val userAnswers: UserAnswers = emptyUserAnswers
    .setOrException(RfmCorporatePositionPage, Upe)
    .setOrException(RfmPrimaryContactNamePage, "RFM test contact")
    .setOrException(RfmPrimaryContactEmailPage, "rfm@email.com")
    .setOrException(RfmContactByPhonePage, false)
    .setOrException(RfmAddSecondaryContactPage, false)
    .setOrException(RfmContactAddressPage, contactAddress)

  when(mockCountryOptions.getCountryNameFromCode(countryCode)).thenReturn(country)

  val rfmCorporatePositionSummaryList: SummaryList =
    SummaryListViewModel(
      rows = Seq(
        RfmCorporatePositionSummary.row(userAnswers),
        RfmNameRegistrationSummary.row(userAnswers),
        RfmRegisteredAddressSummary.row(userAnswers, mockCountryOptions),
        EntityTypeIncorporatedCompanyNameRfmSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyRegRfmSummary.row(userAnswers),
        EntityTypeIncorporatedCompanyUtrRfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyNameRfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyRegRfmSummary.row(userAnswers),
        EntityTypePartnershipCompanyUtrRfmSummary.row(userAnswers)
      ).flatten
    )

  val rfmPrimaryContactList: SummaryList = SummaryListViewModel(
    rows = Seq(
      RfmPrimaryContactNameSummary.row(userAnswers),
      RfmPrimaryContactEmailSummary.row(userAnswers),
      RfmContactByPhoneSummary.row(userAnswers),
      RfmCapturePrimaryPhoneSummary.row(userAnswers)
    ).flatten
  )

  val rfmSecondaryContactList: SummaryList = SummaryListViewModel(
    rows = Seq(
      RfmAddSecondaryContactSummary.row(userAnswers),
      RfmSecondaryContactNameSummary.row(userAnswers),
      RfmSecondaryContactEmailSummary.row(userAnswers),
      RfmSecondaryPhonePreferenceSummary.row(userAnswers),
      RfmSecondaryPhoneSummary.row(userAnswers)
    ).flatten
  )

  val address: SummaryList = SummaryListViewModel(
    rows = Seq(RfmContactAddressSummary.row(userAnswers, mockCountryOptions)).flatten
  )

  lazy val rfmRequest: Request[AnyContent] =
    FakeRequest("GET", controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url).withCSRFToken
  lazy val page: RfmContactCheckYourAnswersView = inject[RfmContactCheckYourAnswersView]
  lazy val view: Document = Jsoup.parse(
    page(rfmCorporatePositionSummaryList, rfmPrimaryContactList, rfmSecondaryContactList, address)(rfmRequest, appConfig, messages).toString()
  )
  lazy val pageTitle:        String   = "Check your answers before submitting"
  lazy val h2Elements:       Elements = view.getElementsByTag("h2")
  lazy val summaryListKeys:  Elements = view.getElementsByClass("govuk-summary-list__key")
  lazy val summaryListItems: Elements = view.getElementsByClass("govuk-summary-list__value")
  lazy val summaryListLinks: Elements = view.getElementsByClass("govuk-summary-list__actions")

  "Rfm Contact Check Your Answers View" should {

    "have a title" in {
      view.title() mustBe s"$pageTitle - Report Pillar 2 Top-up Taxes - GOV.UK"
    }

    "have a non-clickable banner" in {
      val serviceName = view.getElementsByClass("govuk-header__service-name").first()
      serviceName.text mustBe "Report Pillar 2 Top-up Taxes"
      serviceName.getElementsByTag("a") mustBe empty
    }

    "have a caption" in {
      view.getElementsByClass("govuk-caption-l").text mustBe "Review and submit"
    }

    "have a unique H1 heading" in {
      val h1Elements: Elements = view.getElementsByTag("h1")
      h1Elements.size mustBe 1
      h1Elements.text mustBe pageTitle
    }

    "have filing member details section" must {
      "have h2 heading" in {
        h2Elements.get(1).text mustBe "Filing member details"
      }

      "have a summary list" in {
        summaryListKeys.get(0).text mustBe "Position in the group’s corporate structure"
        summaryListItems.get(0).text mustBe "Ultimate Parent Entity (UPE)"
        summaryListLinks.get(0).text mustBe "Change the position in the group’s corporate structure"
        summaryListLinks.get(0).getElementsByTag("a").attr("href") mustBe
          controllers.rfm.routes.CorporatePositionController.onPageLoad(CheckMode).url
      }
    }

    "have primary contact section" must {
      "have h2 heading" in {
        h2Elements.get(2).text mustBe "Primary contact"
      }

      "have a summary list" in {
        summaryListKeys.get(1).text mustBe "Contact name"
        summaryListItems.get(1).text mustBe "RFM test contact"
        summaryListLinks.get(1).text mustBe "Change the primary contact name"
        summaryListLinks.get(1).getElementsByTag("a").attr("href") mustBe controllers.rfm.routes.RfmPrimaryContactNameController
          .onPageLoad(CheckMode)
          .url

        summaryListKeys.get(2).text mustBe "Email address"
        summaryListItems.get(2).text mustBe "rfm@email.com"
        summaryListLinks.get(2).text mustBe "Change the primary contact email address"
        summaryListLinks.get(2).getElementsByTag("a").attr("href") mustBe controllers.rfm.routes.RfmPrimaryContactEmailController
          .onPageLoad(CheckMode)
          .url

        summaryListKeys.get(3).text mustBe "Can we contact the primary contact by phone?"
        summaryListItems.get(3).text mustBe "No"
        summaryListLinks.get(3).text mustBe "Change can we contact the primary contact by phone"
        summaryListLinks.get(3).getElementsByTag("a").attr("href") mustBe controllers.rfm.routes.RfmContactByPhoneController
          .onPageLoad(CheckMode)
          .url
      }
    }

    "have a secondary contact section" must {
      "have h2 heading" in {
        h2Elements.get(3).text mustBe "Secondary contact"
      }

      "have a summary list" in {
        summaryListKeys.get(4).text mustBe "Do you have a second contact?"
        summaryListItems.get(4).text mustBe "No"
        summaryListLinks.get(4).text mustBe "Change do you have a secondary contact"
        summaryListLinks.get(4).getElementsByTag("a").attr("href") mustBe
          controllers.rfm.routes.RfmAddSecondaryContactController.onPageLoad(CheckMode).url
      }
    }

    "have a contact address section" must {
      "have h2 heading" in {
        h2Elements.get(4).text mustBe "Contact address"
      }

      "have a summary list" in {
        summaryListKeys.get(5).text mustBe "Address"
        summaryListItems.get(5).text mustBe "RFM Address Line 1 RFM City EH5 5WY United Kingdom"
        summaryListLinks.get(5).text mustBe "Change the contact address"
        summaryListLinks.get(5).getElementsByTag("a").attr("href") mustBe
          controllers.rfm.routes.RfmContactAddressController.onPageLoad(CheckMode).url
      }
    }

    "have a keep a record of your answers section" must {
      "have h2 heading" in {
        h2Elements.get(5).text mustBe "Do you need to keep a record of your answers?"
      }

      "have a paragraph" in {
        view.getElementsByClass("govuk-body").get(0).text mustBe "You can print or save a copy of your answers using the 'Print this page' link."
      }

      "display print this page link" in {
        val printLink = view.select("a:contains(Print this page)")
        printLink.size()         must be >= 1
        printLink.first().text() must include("Print this page")
      }
    }

    "have a submission section" must {
      "have h2 heading" in {
        h2Elements.get(6).text mustBe "Now submit your details to replace the current filing member"
      }

      "have a paragraph" in {
        view
          .getElementsByClass("govuk-body")
          .get(1)
          .text mustBe "By submitting these details, you are confirming that you are able to act as a new filing member for your group and the information is correct and complete to the best of your knowledge."
      }
    }

    "have a button" in {
      view.getElementsByClass("govuk-button").text mustBe "Confirm and submit"
    }
  }
}
