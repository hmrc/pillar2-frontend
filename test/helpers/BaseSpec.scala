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

import akka.actor.ActorSystem
import akka.stream.Materializer
import config.FrontendAppConfig
import models.UserAnswers
import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.{HeaderNames, Status}
import play.api.i18n.{DefaultLangs, DefaultMessagesApi, MessagesApi}
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

  implicit lazy val ec:           ExecutionContext  = scala.concurrent.ExecutionContext.Implicits.global
  implicit lazy val hc:           HeaderCarrier     = HeaderCarrier()
  implicit lazy val appConfig:    FrontendAppConfig = new FrontendAppConfig(configuration)
  implicit lazy val system:       ActorSystem       = ActorSystem()
  implicit lazy val materializer: Materializer      = Materializer(system)

  val userAnswersId: String = "id"

  def testUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  val languageUtil = new LanguageUtils(new DefaultLangs(), configuration)

  def countOccurrences(src: String, tgt: String): Int =
    src.sliding(tgt.length).count(window => window == tgt)

}
