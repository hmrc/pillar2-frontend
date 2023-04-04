/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package config

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import controllers.actions.{AuthenticatedIdentifierAction, DataRequiredAction, DataRequiredActionImpl, DataRetrievalAction, DataRetrievalActionImpl, IdentifierAction}
import play.api.i18n.DefaultMessagesApiProvider
import play.api.mvc.MessagesControllerComponents
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.{Clock, ZoneOffset}
import javax.inject.Singleton
import scala.concurrent.ExecutionContext

class GuiceModule(environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[DataRetrievalAction]).to(classOf[DataRetrievalActionImpl]).asEagerSingleton()
    bind(classOf[DataRequiredAction]).to(classOf[DataRequiredActionImpl]).asEagerSingleton()

    // For session based storage instead of cred based, change to SessionIdentifierAction
    bind(classOf[IdentifierAction]).to(classOf[AuthenticatedIdentifierAction]).asEagerSingleton()

    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone.withZone(ZoneOffset.UTC))
  }



  @Provides
  @Named("pillar2Url")
  @Singleton
  def registerGmsUrlProvider(servicesConfig: ServicesConfig): String =
    servicesConfig.baseUrl("pillar2")


  @Provides
  @Named("pillar2StubUrl")
  @Singleton
  def registerGmsStubUrlProvider(servicesConfig: ServicesConfig): String =
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
    configuration.get[String](s"google-analytics.trackingId")

}
