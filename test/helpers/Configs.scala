/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package helpers

import com.typesafe.config.ConfigFactory
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

trait Configs {

  def configuration: Configuration = Configuration(ConfigFactory.parseResources("application.conf"))

  def environment: Environment = Environment.simple()

  def servicesConfig = new ServicesConfig(configuration)

  implicit def applicationConfig: AppConfig = new AppConfig(configuration, servicesConfig)
}
