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

import com.google.inject.name.{Named, Names}
import com.google.inject.{AbstractModule, Provides}
import connectors.{IncorporatedEntityIdentificationFrontendConnector, IncorporatedEntityIdentificationFrontendConnectorImpl, PartnershipIdentificationFrontendConnector, PartnershipIdentificationFrontendConnectorImpl}
import controllers.actions._
import play.api.{Configuration, Environment}
import stubsonly.connectors.stubs.{StubIncorporatedEntityIdentificationFrontendConnector, StubPartnershipEntityIdentificationFrontendConnector}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.{Clock, ZoneOffset}
import javax.inject.Singleton

class GuiceModule(environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[DataRetrievalAction]).to(classOf[DataRetrievalActionImpl]).asEagerSingleton()
    bind(classOf[DataRequiredAction]).to(classOf[DataRequiredActionImpl]).asEagerSingleton()
    bind(classOf[SubscriptionDataRetrievalAction]).to(classOf[SubscriptionDataRetrievalActionImpl]).asEagerSingleton()
    bind(classOf[SubscriptionDataRequiredAction]).to(classOf[SubscriptionDataRequiredActionImpl]).asEagerSingleton()
    bind(classOf[SessionDataRetrievalAction]).to(classOf[SessionDataRetrievalActionImpl]).asEagerSingleton()
    bind(classOf[SessionDataRequiredAction]).to(classOf[SessionDataRequiredActionImpl]).asEagerSingleton()

    // For session based storage instead of cred based, change to SessionIdentifierAction
    bind(classOf[IdentifierAction]).to(classOf[AuthenticatedIdentifierAction]).asEagerSingleton()
    bind(classOf[IdentifierAction]).annotatedWith(Names.named("RfmIdentifier")).to(classOf[RfmIdentifierAction]).asEagerSingleton()
    bind(classOf[IdentifierAction]).annotatedWith(Names.named("EnrolmentIdentifier")).to(classOf[EnrolmentIdentifierAction]).asEagerSingleton()
    bind(classOf[IdentifierAction]).annotatedWith(Names.named("ASAEnrolmentIdentifier")).to(classOf[ASAEnrolmentIdentifierAction]).asEagerSingleton()

    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone.withZone(ZoneOffset.UTC))
    val grsStubEnabled = configuration.get[Boolean]("features.grsStubEnabled")
    if (grsStubEnabled) {
      bind(classOf[IncorporatedEntityIdentificationFrontendConnector])
        .to(classOf[StubIncorporatedEntityIdentificationFrontendConnector])
        .asEagerSingleton()

      bind(classOf[PartnershipIdentificationFrontendConnector])
        .to(classOf[StubPartnershipEntityIdentificationFrontendConnector])
        .asEagerSingleton()

    } else {
      bind(classOf[IncorporatedEntityIdentificationFrontendConnector])
        .to(classOf[IncorporatedEntityIdentificationFrontendConnectorImpl])
        .asEagerSingleton()

      bind(classOf[PartnershipIdentificationFrontendConnector])
        .to(classOf[PartnershipIdentificationFrontendConnectorImpl])
        .asEagerSingleton()
    }
  }

  @Provides
  @Named("pillar2Url")
  @Singleton
  def registerPillar2UrlProvider(servicesConfig: ServicesConfig): String =
    servicesConfig.baseUrl("pillar2")
  /*
  @Provides
  @Named("pillar2StubUrl")
  @Singleton
  def registerPillar2StubUrlProvider(servicesConfig: ServicesConfig): String =
    servicesConfig.baseUrl("pillar2-stubs")

  @Provides
  @Named("exitSurveyFeedbackUrl")
  @Singleton
  def exitSurveyFeedbackUrlProvider(servicesConfig: ServicesConfig): String =
    s"${servicesConfig.getString("feedback-frontend.baseUrl")}/feedback/${servicesConfig.getString("appName")}"

  @Provides
  @Named("platformAnalyticsUrl")
  @Singleton
  def platformAnalyticsUrlProvider(servicesConfig: ServicesConfig): String =
    servicesConfig.baseUrl("platform-analytics")

  @Provides
  @Named("platformAnalyticsTrackingId")
  @Singleton
  protected def platformAnalyticsTrackingIdProvider(configuration: Configuration): String =
    configuration.get[String](s"google-analytics.trackingId")*/

}
