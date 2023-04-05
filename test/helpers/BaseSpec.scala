/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package helpers

import akka.actor.ActorSystem
import akka.stream.Materializer
import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.{HeaderNames, Status}
import play.api.i18n.DefaultLangs
import play.api.test.{DefaultAwaitTimeout, FutureAwaits, ResultExtractors}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.language.LanguageUtils

import scala.concurrent.ExecutionContext

trait BaseSpec
    extends AnyWordSpec
    with OptionValues
    with Inside
    with Matchers
    with EitherValues
    with BeforeAndAfterEach
    with DefaultAwaitTimeout
    with FutureAwaits
    with Status
    with HeaderNames
    with ResultExtractors
    with AllMocks
    with ViewInstances {

  implicit lazy val ec:           ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit lazy val hc:           HeaderCarrier    = HeaderCarrier()
  implicit lazy val system:       ActorSystem      = ActorSystem()
  implicit lazy val materializer: Materializer     = Materializer(system)

  val languageUtil = new LanguageUtils(new DefaultLangs(), configuration)

  def countOccurrences(src: String, tgt: String): Int =
    src.sliding(tgt.length).count(window => window == tgt)
}
