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

package controllers.actions

import models.requests.IdentifierRequest
import play.api.mvc._
import uk.gov.hmrc.auth.core.Enrolments

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FakeAmendIdentifierAction @Inject()(bodyParsers: PlayBodyParsers, enrolments: Enrolments) extends AmendIdentifierAction {

  override def refine[A](request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] =
    Future.successful(Right(IdentifierRequest(request, "id", Some("groupID"), enrolments.enrolments, userIdForEnrolment = "userId")))

  override def parser: BodyParser[AnyContent] =
    bodyParsers.default

  override protected def executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}
