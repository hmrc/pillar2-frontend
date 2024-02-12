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

import connectors.{EnrolmentConnector, EnrolmentStoreProxyConnector, RegistrationConnector, SubscriptionConnector}
import models.fm.JourneyType
import models.subscription.SubscriptionRequestParameters
import models.{InternalServerError, UserAnswers}
import pages.NominateFilingMemberPage
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionService @Inject() (
  registrationConnector:        RegistrationConnector,
  subscriptionConnector:        SubscriptionConnector,
  enrolmentConnector:           EnrolmentConnector,
  enrolmentStoreProxyConnector: EnrolmentStoreProxyConnector
)(implicit ec:                  ExecutionContext) {

  def createSubscription(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[String] =
    for {
      upeSafeId       <- registerUpe(userAnswers)
      fmnSafeId       <- registerNfm(userAnswers)
      plrRef          <- subscriptionConnector.subscribe(SubscriptionRequestParameters(userAnswers.id, upeSafeId, fmnSafeId))
      enrolmentExists <- enrolmentStoreProxyConnector.enrolmentExists(plrRef)
      _               <- if (!enrolmentExists) Future.unit else Future.failed(InternalServerError)
      enrolmentInfo = userAnswers.createEnrolmentInfo(plrRef)
      _ <- enrolmentConnector.createEnrolment(enrolmentInfo)
    } yield plrRef

  private def registerUpe(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[String] =
    userAnswers.getUpeSafeID
      .map(Future.successful)
      .getOrElse(registrationConnector.register(userAnswers.id, JourneyType.UltimateParent))

  private def registerNfm(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Option[String]] =
    userAnswers.getFmSafeID
      .map(safeId => Future.successful(Some(safeId)))
      .getOrElse {
        if (userAnswers.get(NominateFilingMemberPage).contains(true)) {
          registrationConnector.register(userAnswers.id, JourneyType.FilingMember).map(Some(_))
        } else {
          Future.successful(None)
        }
      }
}
