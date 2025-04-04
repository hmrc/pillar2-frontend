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

package base

import config.FrontendAppConfig
import helpers.{AllMocks, UserAnswersFixture}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.{FakeRequest, Injecting}

trait ViewSpecBase
    extends PlaySpec
    with GuiceOneAppPerSuite
    with Injecting
    with Matchers
    with UserAnswersFixture
    with BeforeAndAfterEach
    with AllMocks {

  val request:                        Request[AnyContent] = FakeRequest().withCSRFToken
  protected lazy val realMessagesApi: MessagesApi         = inject[MessagesApi]
  val appConfig:                      FrontendAppConfig   = inject[FrontendAppConfig]

  implicit def messages: Messages =
    realMessagesApi.preferred(request)

}
