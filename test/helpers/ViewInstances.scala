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
import uk.gov.hmrc.hmrcfrontend.views.html.components._
import uk.gov.hmrc.hmrcfrontend.views.html.helpers.{HmrcScripts, HmrcStandardHeader, HmrcTrackingConsentSnippet}
import uk.gov.hmrc.play.language.LanguageUtils
import views.html.templates.Layout

trait ViewInstances extends Configs with StubMessageControllerComponents {

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

  val hmrcNewTabLink = new HmrcNewTabLink

  val assetsConfig = new AssetsConfig()

  val hmrcScripts        = new HmrcScripts(assetsConfig)
  val hmrcTimeoutDilogue = new HmrcTimeoutDialog

  val languageUtils = new LanguageUtils(new DefaultLangs(), configuration)

  val govukHint         = new GovukHint
  val govukRadios       = new GovukRadios(new GovukErrorMessage, new GovukFieldset, new GovukHint, new GovukLabel)
  val govukInput        = new GovukInput(new GovukErrorMessage, new GovukHint, new GovukLabel)
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

  /*  val mainTemplate =
    new Layout(
      govukLayout,
      new GovukBackLink,
      new head(hmrcTrackingConsent),
      new uk.gov.hmrc.goodsmovementsystemfrontend.views.html.main.partials.LanguageSelect(new HmrcLanguageSelect()),
      scripts,
      new MultiLanguageSelect(new GovukSelect(new GovukErrorMessage, new GovukHint, new GovukLabel))
    )*/

}
