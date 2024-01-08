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

package filters

import base.SpecBase
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{ActionBuilder, AnyContent, Request, _}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.{ExecutionContext, Future}

class AllowListFilterSpec extends SpecBase {

  val messagesControllerComponentsForView: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  object TestAction extends ActionBuilder[Request, AnyContent] {
    override implicit protected def executionContext: ExecutionContext       = messagesControllerComponentsForView.executionContext
    override def parser:                              BodyParser[AnyContent] = messagesControllerComponentsForView.parsers.defaultBodyParser
    override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = block(request)
  }

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "metrics.jvm"                         -> false,
        "metrics.enabled"                     -> false,
        "bootstrap.filters.allowlist.ips"     -> Seq[String]("127.0.0.2"),
        "bootstrap.filters.allowlist.enabled" -> true
      )
      .routes {
        case (GET, "/report-pillar2-top-up-taxes") => TestAction(Ok("success"))
        case _                                     => TestAction(Ok("err"))
      }
      .build()

  "AllowlistFilter" when {

    "supplied with a non-allowlisted IP" should {
      lazy val fakeRequest = FakeRequest(GET, "/report-pillar2-top-up-taxes").withHeaders(
        "True-Client-IP" -> "127.0.0.3"
      )

      Call(fakeRequest.method, fakeRequest.uri)
      lazy val Some(result) = route(fakeApplication(), fakeRequest)

      "return status of 303" in {
        status(result) mustBe 303
      }

      "redirect to unauthorised page" in {
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/unauthorised")
      }
    }

    "supplied with an allowlisted IP" should {
      lazy val fakeRequest = FakeRequest(GET, "/report-pillar2-top-up-taxes").withHeaders(
        "True-Client-IP" -> "127.0.0.2"
      )

      Call(fakeRequest.method, fakeRequest.uri)
      lazy val Some(result) = route(fakeApplication(), fakeRequest)

      "return status of 200" in {
        status(result) mustBe 200
      }
    }

  }
}
