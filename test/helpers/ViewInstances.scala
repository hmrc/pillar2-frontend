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

package helpers

import base.SpecBase
import play.api.i18n.DefaultLangs
import uk.gov.hmrc.govukfrontend.views.html.components._
import uk.gov.hmrc.govukfrontend.views.html.helpers.{GovukFormGroup, GovukHintAndErrorMessage, GovukLogo}
import uk.gov.hmrc.hmrcfrontend.config._
import uk.gov.hmrc.hmrcfrontend.views.config.{HmrcFooterItems, StandardBetaBanner}
import uk.gov.hmrc.hmrcfrontend.views.html.components._
import uk.gov.hmrc.hmrcfrontend.views.html.helpers._
import uk.gov.hmrc.play.language.LanguageUtils
import views.html._
import views.html.components.gds._
import views.html.eligibilityview.EligibilityConfirmationView
import views.html.fmview._
import views.html.registrationview._
import views.html.repayments.RequestRefundAmountView
import views.html.rfm.RfmEntityTypeView
import views.html.subscriptionview._
import views.html.templates._

trait ViewInstances extends StubMessageControllerComponents {
  this: SpecBase =>

  lazy val configuration = app.configuration

  val hmrcTrackingConsent = new HmrcTrackingConsentSnippet(new TrackingConsentConfig(configuration))

  val accessibilityConfiguration = new AccessibilityStatementConfig(configuration)

  lazy val tudorCrownConfig: TudorCrownConfig = TudorCrownConfig(configuration)

  lazy val rebrandConfig: RebrandConfig = RebrandConfig(configuration)

  lazy val govukLogo = new GovukLogo()

  val govukHeader = new GovukHeader(tudorCrownConfig, rebrandConfig, govukLogo)

  val govukTemplate =
    new GovukTemplate(govukHeader, new GovukFooter(rebrandConfig, govukLogo), new GovukSkipLink, new FixedWidthPageLayout, rebrandConfig)

  val hmrcStandardHeader = new HmrcStandardHeader(
    hmrcHeader = new HmrcHeader(
      hmrcBanner = new HmrcBanner(tudorCrownConfig),
      hmrcUserResearchBanner = new HmrcUserResearchBanner(),
      govukPhaseBanner = new GovukPhaseBanner(govukTag = new GovukTag()),
      tudorCrownConfig = tudorCrownConfig,
      rebrandConfig = rebrandConfig,
      govukLogo = govukLogo
    )
  )
  val hmrcStandardFooter = new HmrcStandardFooter(
    new HmrcFooter(new GovukFooter(rebrandConfig, govukLogo)),
    new HmrcFooterItems(new AccessibilityStatementConfig(configuration))
  )

  val hmrcNewTabLink = new HmrcNewTabLink

  val assetsConfig = new AssetsConfig()

  val hmrcScripts        = new HmrcScripts(assetsConfig)
  val hmrcTimeoutDilogue = new HmrcTimeoutDialog

  private val govukHintAndErrorMessage: GovukHintAndErrorMessage =
    new GovukHintAndErrorMessage(new GovukHint(), new GovukErrorMessage())

  val languageUtils            = new LanguageUtils(new DefaultLangs(), configuration)
  val govukHint                = new GovukHint
  val govukRadios              = new GovukRadios(new GovukFieldset, new GovukHint, new GovukLabel, new GovukFormGroup, govukHintAndErrorMessage)
  val govukInput               = new GovukInput(new GovukLabel, new GovukFormGroup, govukHintAndErrorMessage)
  val govukDateInput           = new GovukDateInput(new GovukFieldset, govukInput, new GovukFormGroup, govukHintAndErrorMessage)
  val govukCheckboxes          = new GovukCheckboxes(new GovukFieldset, new GovukHint, new GovukLabel, new GovukFormGroup, govukHintAndErrorMessage)
  val govukLabel               = new GovukLabel()
  val govukDetails             = new GovukDetails
  val govukPanel               = new GovukPanel
  val govukTable               = new GovukTable
  val govukButton              = new GovukButton
  val govukFieldSet            = new GovukFieldset
  val govukErrorSummary        = new GovukErrorSummary
  val govukErrorMessage        = new GovukErrorMessage
  val govukSummaryList         = new GovukSummaryList
  val govukSelect              = new GovukSelect(new GovukLabel, new GovukFormGroup, govukHintAndErrorMessage)
  val govukBackLink            = new GovukBackLink
  val govukWarningText         = new GovukWarningText
  val formWithCSRF             = new FormWithCSRF
  val heading                  = new heading
  val warningText              = new warningText(govukWarningText)
  val h2                       = new HeadingH2
  val paragraphBody            = new paragraphBody
  val paragraphBodyLink        = new paragraphBodyLink
  val span                     = new Span
  val paragraphMessageWithLink = new ParagraphMessageWithLink
  val sectionHeader            = new sectionHeader
  val sectionBreak             = new SectionBreak
  val inactiveStatusBanner     = new InactiveStatusBanner
  val bulletList               = new bulletList

  val hmrcPageHeading = new HmrcPageHeading
  val govUkInsetText  = new GovukInsetText

  val govukNotificationBanner = new GovukNotificationBanner()

  val govukLayout = new GovukLayout(
    govukTemplate = govukTemplate,
    govukHeader = govukHeader,
    govukFooter = new GovukFooter(rebrandConfig, govukLogo),
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
    new HmrcAccessibleAutocompleteJavascript(assetsConfig),
    hmrcScripts,
    new StandardBetaBanner,
    new Stylesheets(new HmrcAccessibleAutocompleteCss(assetsConfig)),
    new TwoThirdsMainContent()
  )

  val viewGroupTerritories: GroupTerritoriesView =
    new GroupTerritoriesView(pillar2layout, formWithCSRF, sectionHeader, govukErrorSummary, govukRadios, govukButton)

  val viewBusinessActivityUK: BusinessActivityUKView =
    new BusinessActivityUKView(pillar2layout, formWithCSRF, sectionHeader, govukErrorSummary, govukRadios, govukButton, heading)
  val viewRegisteringNfmForThisGroup: RegisteringNfmForThisGroupView =
    new RegisteringNfmForThisGroupView(pillar2layout, formWithCSRF, sectionHeader, govukErrorSummary, govukRadios, govukButton)

  val viewTurnOverEligibility: TurnOverEligibilityView =
    new TurnOverEligibilityView(pillar2layout, formWithCSRF, sectionHeader, govukErrorSummary, govukRadios, govukButton)

  val viewKbUKIneligible: KbUKIneligibleView =
    new KbUKIneligibleView(pillar2layout, formWithCSRF, heading, paragraphBody, paragraphBodyLink, govukButton)
  val viewKBMneIneligible: KbMnIneligibleView =
    new KbMnIneligibleView(pillar2layout, formWithCSRF, heading, paragraphBody, paragraphBodyLink, govukButton)
  val viewKb750Ineligible: Kb750IneligibleView =
    new Kb750IneligibleView(pillar2layout, formWithCSRF, heading, paragraphBody, paragraphBodyLink, govukButton)

  val viewEligibilityConfirmation: EligibilityConfirmationView =
    new EligibilityConfirmationView(pillar2layout, formWithCSRF, govukButton, govUkInsetText)

  val viewUPERegisteredInUKConfirmation: UPERegisteredInUKConfirmationView =
    new UPERegisteredInUKConfirmationView(pillar2layout, formWithCSRF, sectionHeader, govukErrorSummary, govukRadios, govukButton)

  val viewNominateFilingMemberYesNo: NominateFilingMemberYesNoView =
    new NominateFilingMemberYesNoView(
      pillar2layout,
      formWithCSRF,
      sectionHeader,
      govukErrorSummary,
      govukRadios,
      govukButton,
      govUkInsetText,
      paragraphBody,
      heading,
      h2,
      span
    )

  val viewStartPageRegistration: StartPageRegistrationView =
    new StartPageRegistrationView(pillar2layout, sectionHeader, heading, paragraphBody, formWithCSRF, govukButton)

  val viewRfmEntityTypeController: RfmEntityTypeView =
    new RfmEntityTypeView(pillar2layout, formWithCSRF, govukErrorSummary, govukRadios, govukButton, sectionHeader)
  val viewUPENameRegistration: UpeNameRegistrationView =
    new UpeNameRegistrationView(pillar2layout, formWithCSRF, sectionHeader, govukErrorSummary, govukInput, govukButton)
  val viewUpeContactName: UpeContactNameView =
    new UpeContactNameView(pillar2layout, formWithCSRF, govukErrorSummary, sectionHeader, govukInput, govukButton)
  val viewUpeContactEmail: UpeContactEmailView =
    new UpeContactEmailView(pillar2layout, formWithCSRF, sectionHeader, govukErrorSummary, govukInput, govukButton)
  val viewUpeRegisteredAddress: UpeRegisteredAddressView =
    new UpeRegisteredAddressView(
      pillar2layout,
      formWithCSRF,
      sectionHeader,
      govukWarningText,
      heading,
      govukErrorSummary,
      govukInput,
      govukButton,
      govukSelect
    )
  val viewNfmRegisteredAddress: NfmRegisteredAddressView =
    new NfmRegisteredAddressView(
      pillar2layout,
      formWithCSRF,
      sectionHeader,
      heading,
      govukWarningText,
      govukErrorSummary,
      govukInput,
      govukButton,
      govukSelect
    )

  val viewContactUPEByTelephoneView: ContactUPEByTelephoneView =
    new ContactUPEByTelephoneView(pillar2layout, formWithCSRF, sectionHeader, govukErrorSummary, govukRadios, govukButton)
  val viewContactByTelephoneView: ContactByTelephoneView =
    new ContactByTelephoneView(pillar2layout, formWithCSRF, sectionHeader, govukErrorSummary, govukRadios, govukButton)

  val viewCaptureTelephoneDetailsView: CaptureTelephoneDetailsView =
    new CaptureTelephoneDetailsView(pillar2layout, formWithCSRF, sectionHeader, govukErrorSummary, govukInput, govukButton)
  val viewCheckYourAnswersUPE: UpeCheckYourAnswersView =
    new UpeCheckYourAnswersView(pillar2layout, sectionHeader, heading, govukSummaryList, govukButton)
  val viewCheckYourAnswersFilingMember: FilingMemberCheckYourAnswersView =
    new FilingMemberCheckYourAnswersView(pillar2layout, sectionHeader, heading, govukSummaryList, govukButton)
  val viewCheckYourAnswersSub: SubCheckYourAnswersView =
    new SubCheckYourAnswersView(pillar2layout, sectionHeader, heading, govukSummaryList, govukButton)

  val viewDashboardView: DashboardView =
    new DashboardView(
      pillar2layout,
      govukButton,
      heading,
      h2,
      paragraphBody,
      bulletList,
      paragraphMessageWithLink,
      paragraphBodyLink,
      sectionBreak,
      inactiveStatusBanner
    )

  val viewRequestRefundAmount: RequestRefundAmountView =
    new RequestRefundAmountView(pillar2layout, formWithCSRF, sectionHeader, govukErrorSummary, govukInput, govukButton)

}
