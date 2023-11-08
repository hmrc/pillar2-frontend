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

package services

import connectors.ReadSubscriptionConnector
import models.subscription.ReadSubscriptionRequestParameters
import models.{ApiError, SubscriptionCreateError, UserAnswers}
import uk.gov.hmrc.http.HeaderCarrier
import utils.SubscriptionTransformer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReadSubscriptionService @Inject() (readSubscriptionConnector: ReadSubscriptionConnector) {
  def readSubscription(id: String, plrReference: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ApiError, UserAnswers]] =
    readSubscriptionConnector
      .readSubscription(ReadSubscriptionRequestParameters(id, plrReference))
      .map {
        case Some(jsValue) =>
          SubscriptionTransformer.jsValueToSubscription(jsValue) match {
            case Right(userAnswers) => Right(userAnswers)
            case Left(error)        => Left(error)
          }
        case None => Left(SubscriptionCreateError)
      }

}
