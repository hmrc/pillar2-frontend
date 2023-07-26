/*
 * Copyright 2023 HM Revenue & Customs
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

package helpers

import play.api.i18n.DefaultLangs
import uk.gov.hmrc.govukfrontend.views.html.components._
import uk.gov.hmrc.hmrcfrontend.config.{AccessibilityStatementConfig, AssetsConfig, ContactFrontendConfig, TrackingConsentConfig}
import uk.gov.hmrc.hmrcfrontend.views.config.{HmrcFooterItems, StandardAlphaBanner}
import uk.gov.hmrc.hmrcfrontend.views.html.components._
import uk.gov.hmrc.hmrcfrontend.views.html.helpers._
import uk.gov.hmrc.play.language.LanguageUtils
import views.html._
import views.html.eligibilityview.EligibilityConfirmationView
import views.html.errors.ErrorTemplate
import views.html.fmview.{IsNFMUKBasedView, NominateFilingMemberYesNoView}
import views.html.registrationview._
import views.html.templates._

trait ViewInstances extends Configs with StubMessageControllerComponents {

  val viewNfmEntityType: NfmEntityTypeView =
    new NfmEntityTypeView(pillar2layout, formWithCSRF, govukErrorSummary, govukRadios, govukButton)

  val viewIsNFMUKBased: IsNFMUKBasedView =
    new IsNFMUKBasedView(pillar2layout, formWithCSRF, govukErrorSummary, govukRadios, govukButton)

  val viewEntityType: EntityTypeView =
    new EntityTypeView(pillar2layout, formWithCSRF, govukErrorSummary, govukRadios, govukButton)

  val hmrcTrackingConsent = new HmrcTrackingConsentSnippet(new TrackingConsentConfig(configuration))

  val accessibilityConfiguration = new AccessibilityStatementConfig(configuration)

  val govukHeader = new GovukHeader

  val govukTemplate = new GovukTemplate(govukHeader, new GovukFooter, new GovukSkipLink, new FixedWidthPageLayout)

  val hmrcStandardHeader = new HmrcStandardHeader(
    hmrcHeader = new HmrcHeader(
      hmrcBanner = new HmrcBanner(),
      hmrcUserResearchBanner = new HmrcUserResearchBanner(),
      govukPhaseBanner = new GovukPhaseBanner(govukTag = new GovukTag())
    )
  )
  val hmrcStandardFooter = new HmrcStandardFooter(
    new HmrcFooter,
    new HmrcFooterItems(new AccessibilityStatementConfig(configuration))
  )

  val hmrcNewTabLink = new HmrcNewTabLink

  val assetsConfig = new AssetsConfig()

  val hmrcScripts        = new HmrcScripts(assetsConfig)
  val hmrcTimeoutDilogue = new HmrcTimeoutDialog

  val languageUtils = new LanguageUtils(new DefaultLangs(), configuration)

  val govukHint         = new GovukHint
  val govukRadios       = new GovukRadios(new GovukErrorMessage, new GovukFieldset, new GovukHint, new GovukLabel)
  val govukInput        = new GovukInput(new GovukErrorMessage, new GovukHint, new GovukLabel)
  val govukDateInput    = new GovukDateInput(new GovukErrorMessage, new GovukHint, new GovukFieldset, govukInput)
  val govukCheckboxes   = new GovukCheckboxes(new GovukErrorMessage, new GovukFieldset, new GovukHint, new GovukLabel)
  val govukLabel        = new GovukLabel()
  val govukDetails      = new GovukDetails
  val govukPanel        = new GovukPanel
  val govukTable        = new GovukTable
  val govukButton       = new GovukButton
  val govukFieldSet     = new GovukFieldset
  val govukErrorSummary = new GovukErrorSummary
  val govukErrorMessage = new GovukErrorMessage
  val govukSummaryList  = new GovukSummaryList
  val govukSelect       = new GovukSelect(new GovukErrorMessage, new GovukHint, new GovukLabel)
  val govukBackLink     = new GovukBackLink
  val govukWarningText  = new GovukWarningText
  val formWithCSRF      = new FormWithCSRF

  val hmrcPageHeading = new HmrcPageHeading
  val govUkInsetText  = new GovukInsetText

  val govukNotificationBanner = new GovukNotificationBanner()

  val govukLayout = new GovukLayout(
    govukTemplate = govukTemplate,
    govukHeader = govukHeader,
    govukFooter = new GovukFooter,
    govukBackLink = govukBackLink,
    defaultMainContentLayout = new TwoThirdsMainContent,
    fixedWidthPageLayout = new FixedWidthPageLayout
  )

  val pillar2layout = new Layout(
    govukLayout,
    new GovukBackLink,
    new HmrcHead(hmrcTrackingConsent, assetsConfig),
    hmrcStandardHeader,
    hmrcStandardFooter,
    hmrcTrackingConsent,
    new HmrcLanguageSelect(),
    hmrcTimeoutDilogue,
    new HmrcReportTechnicalIssueHelper(new HmrcReportTechnicalIssue(), new ContactFrontendConfig(configuration)),
    hmrcScripts,
    new StandardAlphaBanner
  )

  val viewpageNotAvailable: ErrorTemplate = new ErrorTemplate(pillar2layout)

  val viewGroupTerritories: GroupTerritoriesView =
    new GroupTerritoriesView(pillar2layout, formWithCSRF, govukErrorSummary, govukRadios, govukButton)

  val viewBusinessActivityUK: BusinessActivityUKView =
    new BusinessActivityUKView(pillar2layout, formWithCSRF, govukErrorSummary, govukRadios, govukButton)
  val viewRegisteringNfmForThisGroup: RegisteringNfmForThisGroupView =
    new RegisteringNfmForThisGroupView(pillar2layout, formWithCSRF, govukErrorSummary, govukRadios, govukButton)

  val viewTurnOverEligibility: TurnOverEligibilityView =
    new TurnOverEligibilityView(pillar2layout, formWithCSRF, govukErrorSummary, govukRadios, govukButton)

  val viewKbUKIneligible: KbUKIneligibleView =
    new KbUKIneligibleView(pillar2layout, formWithCSRF, govukButton)
  val viewKBMneIneligible: KbMnIneligibleView =
    new KbMnIneligibleView(pillar2layout, formWithCSRF, govukButton)
  val viewKb750Ineligible: Kb750IneligibleView =
    new Kb750IneligibleView(pillar2layout, formWithCSRF, govukButton)

  val viewEligibilityConfirmation: EligibilityConfirmationView =
    new EligibilityConfirmationView(pillar2layout, formWithCSRF, govukButton)

  val viewUPERegisteredInUKConfirmation: UPERegisteredInUKConfirmationView =
    new UPERegisteredInUKConfirmationView(pillar2layout, formWithCSRF, govukErrorSummary, govukRadios, govukButton)
  val viewNominateFilingMemberYesNo: NominateFilingMemberYesNoView =
    new NominateFilingMemberYesNoView(pillar2layout, formWithCSRF, govukErrorSummary, govukRadios, govukButton, govUkInsetText)

  val viewStartPageRegistration: StartPageRegistrationView =
    new StartPageRegistrationView(pillar2layout, formWithCSRF, govukButton)

  val viewUPENameRegistration: UpeNameRegistrationView =
    new UpeNameRegistrationView(pillar2layout, formWithCSRF, govukErrorSummary, govukInput, govukButton)
  val viewUpeContactName: UpeContactNameView =
    new UpeContactNameView(pillar2layout, formWithCSRF, govukErrorSummary, govukInput, govukButton)
  val viewUpeContactEmail: UpeContactEmailView =
    new UpeContactEmailView(pillar2layout, formWithCSRF, govukErrorSummary, govukInput, govukButton)
  val viewUpeRegisteredAddress: UpeRegisteredAddressView =
    new UpeRegisteredAddressView(pillar2layout, formWithCSRF, govukErrorSummary, govukInput, govukButton)

  val viewContactUPEByTelephoneView: ContactUPEByTelephoneView =
    new ContactUPEByTelephoneView(pillar2layout, formWithCSRF, govukErrorSummary, govukRadios, govukButton)

  val viewCaptureTelephoneDetailsView: CaptureTelephoneDetailsView =
    new CaptureTelephoneDetailsView(pillar2layout, formWithCSRF, govukErrorSummary, govukInput, govukButton)
  val viewCheckYourAnswersUPE: UpeCheckYourAnswersView = new UpeCheckYourAnswersView(pillar2layout, govukSummaryList, govukButton)

}
