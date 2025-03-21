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

package stubsonly.controllers.actions

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import models.eacd.Pillar2Enrolment
import play.api.mvc.Results._
import play.api.mvc._
import stubsonly.models.requests.TestOnlyAuthorisedRequest
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestOnlyAuthorisedAction @Inject() (
  override val authConnector:    AuthConnector,
  config:                        FrontendAppConfig,
  val parser:                    BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[TestOnlyAuthorisedRequest, AnyContent]
    with FrontendHeaderCarrierProvider
    with ActionFunction[Request, TestOnlyAuthorisedRequest]
    with AuthorisedFunctions {

  override def invokeBlock[A](
    request: Request[A],
    block:   TestOnlyAuthorisedRequest[A] => Future[Result]
  ): Future[Result] =
    authorised().retrieve(internalId and allEnrolments and groupIdentifier) { case optInternalId ~ enrolments ~ optGroupId =>
      val internalId: String = optInternalId.getOrElseFail("Unable to retrieve internalId")
      val groupId:    String = optGroupId.getOrElseFail("Unable to retrieve groupIdentifier")
      val pillar2Reference: Option[String] = enrolments
        .getEnrolment(Pillar2Enrolment.ServiceName)
        .flatMap(_.getIdentifier(Pillar2Enrolment.IdentifierKey).map(_.value))

      block(TestOnlyAuthorisedRequest(request, internalId, groupId, pillar2Reference))
    }(hc(request), executionContext) recover { case _: NoActiveSession =>
      Redirect(config.loginUrl, Map("continue" -> Seq(s"${config.host}${request.uri}")))
    }

  implicit class OptionOps[T](o: Option[T]) {
    def getOrElseFail(failureMessage: String): T = o.getOrElse(throw new IllegalStateException(failureMessage))
  }
}
