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

package services

import connectors.ReadSubscriptionConnector
import models.InternalIssueError
import models.subscription.ReadSubscriptionRequestParameters
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReadSubscriptionService @Inject() (
  readSubscriptionConnector: ReadSubscriptionConnector,
  implicit val ec:           ExecutionContext
) {
  def readSubscription(parameters: ReadSubscriptionRequestParameters)(implicit hc: HeaderCarrier): Future[JsValue] =
    readSubscriptionConnector.readSubscription(parameters).flatMap {
      case Some(jsValue) =>
        Future.successful(jsValue)
      case _ =>
        Future.failed(InternalIssueError)
    }
}
