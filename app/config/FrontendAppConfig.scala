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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, servicesConfig: ServicesConfig) {

  val host:    String = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  private def loadConfig(key: String): String =
    configuration.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private val contactHost                  = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "pillar2-frontend"

  val pillar2mailbox: String = configuration.get[String]("features.pillar2mailbox")

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${SafeRedirectUrl(host + request.uri).encodedUrl}"

  def supportUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/report-technical-problem?service=$contactFormServiceIdentifier&referrerUrl=${SafeRedirectUrl(request.uri).encodedUrl}"

  val loginUrl:                    String = configuration.get[String]("urls.login")
  val loginContinueUrl:            String = configuration.get[String]("urls.loginContinue")
  val rfmLoginContinueUrl:         String = configuration.get[String]("urls.rfmLoginContinue")
  val rfmSecurityLoginContinueUrl: String = configuration.get[String]("urls.rfmSecurityLoginContinue")
  val signOutUrl:                  String = configuration.get[String]("urls.signOut")
  val plr2RegistrationGuidanceUrl: String = configuration.get[String]("urls.plr2RegistrationGuidance")
  val penaltiesInformationUrl:     String = configuration.get[String]("urls.penaltiesInformation")
  val pillar2TopupGuidance:        String = configuration.get[String]("urls.pillar2TopupGuidance")

  val enrolmentKey:        String = configuration.get[String](s"enrolment.key")
  val enrolmentIdentifier: String = configuration.get[String](s"enrolment.identifier")

  lazy val enrolmentStoreProxyUrl: String =
    s"${configuration.get[Service]("microservice.services.enrolment-store-proxy").baseUrl}${configuration
      .get[String]("microservice.services.enrolment-store-proxy.startUrl")}"

  val taxEnrolmentsUrl1: String = s"${configuration.get[Service]("microservice.services.tax-enrolments").baseUrl}${configuration
    .get[String]("microservice.services.tax-enrolments.url1")}"

  val taxEnrolmentsUrl2: String = s"${configuration.get[String]("microservice.services.tax-enrolments.url2")}"

  val accessibilityStatementServicePath: String = configuration.get[String]("accessibility-statement.service-path")

  val accessibilityStatementPath: String =
    s"/accessibility-statement$accessibilityStatementServicePath"

  private val exitSurveyBaseUrl: String = configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  val exitSurveyUrl:             String = s"$exitSurveyBaseUrl/feedback/pillar2-frontend"

  val timeout:   Int = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Int = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  val pillar2BaseUrl:                                  String = servicesConfig.baseUrl("pillar2")
  val incorporatedEntityIdentificationFrontendBaseUrl: String = servicesConfig.baseUrl("incorporated-entity-identification-frontend")
  val partnershipEntityIdentificationFrontendBaseUrl:  String = servicesConfig.baseUrl("partnership-identification-frontend")
  val barsBaseUrl:                                     String = servicesConfig.baseUrl("bank-account-reputation")

  val grsContinueUrl:              String  = configuration.get[String]("urls.grsContinue")
  val incorporatedEntityBvEnabled: Boolean = configuration.get[Boolean]("features.incorporatedEntityBvEnabled")
  val partnershipBvEnabled:        Boolean = configuration.get[Boolean]("features.partnershipBvEnabled")

  //Enable Disable
  val grsStubEnabled: Boolean = configuration.get[Boolean]("features.grsStubEnabled")

  lazy val locationCanonicalList: String = loadConfig("location.canonical.list.all")

  val btaAccessEnabled:             Boolean = configuration.get[Boolean]("features.btaAccessEnabled")
  val btaHomePageUrl:               String  = configuration.get[String]("urls.btaHomePage")
  val asaHomePageUrl:               String  = configuration.get[String]("urls.asaHomePage")
  val eacdHomePageUrl:              String  = configuration.get[String]("urls.eacdHomePage")
  val howToRegisterPlr2GuidanceUrl: String  = configuration.get[String]("urls.howToRegisterPlr2Guidance")
  val researchUrl:                  String  = configuration.get[String]("urls.researchUrl")
  val howToPayPillar2TaxesUrl:      String  = configuration.get[String]("urls.howToPayPillar2Taxes")

  val opsBaseUrl:             String  = servicesConfig.baseUrl("ops")
  val opsStartUrl:            String  = configuration.get[String]("microservice.services.ops.startUrl")
  val enablePayByBankAccount: Boolean = configuration.get[Boolean]("features.enablePayByBankAccount")

  def transactionHistoryEndDate: LocalDate = {
    val date = configuration.get[String]("features.transactionHistoryEndDate")

    if (date == "now") LocalDate.now() else LocalDate.parse(date)
  }

  val maxDaysAgoToConsiderPaymentAsRecent: Int = configuration.get[Int]("payments.maxDaysAgoToConsiderPaymentAsRecent")

  val subscriptionPollingTimeoutSeconds:  Int = configuration.get[Int]("subscription.pollingTimeoutSeconds")
  val subscriptionPollingIntervalSeconds: Int = configuration.get[Int]("subscription.pollingIntervalSeconds")

  val btnWaitingRoomPollIntervalSeconds: Int = configuration.get[Int]("btn.waitingRoom.pollIntervalSeconds")
}
