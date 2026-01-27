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

import connectors.ObligationsAndSubmissionsConnector
import models.DueAndOverdueReturnBannerScenario
import models.DueAndOverdueReturnBannerScenario.*
import models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess
import models.obligationsandsubmissions.{AccountingPeriodDetails, Obligation, ObligationStatus}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ObligationsAndSubmissionsService @Inject() (
  obligationAndSubmissionsConnector: ObligationsAndSubmissionsConnector
)(using ec: ExecutionContext) {

  def handleData(pillar2Id: String, fromDate: LocalDate, toDate: LocalDate)(using hc: HeaderCarrier): Future[ObligationsAndSubmissionsSuccess] =
    obligationAndSubmissionsConnector.getData(pillar2Id, fromDate, toDate).flatMap(Future.successful)
}

object ObligationsAndSubmissionsService {

  def getDueOrOverdueReturnsStatus(obligationsAndSubmissions: ObligationsAndSubmissionsSuccess): Option[DueAndOverdueReturnBannerScenario] = {

    def periodStatus(period: AccountingPeriodDetails): Option[DueAndOverdueReturnBannerScenario] =
      if period.obligations.isEmpty then {
        None
      } else {
        val uktrObligation:       Option[Obligation] = period.uktrObligation
        val girObligation:        Option[Obligation] = period.girObligation
        val dueDatePassed:        Boolean            = period.dueDatePassed
        val hasAnyOpenObligation: Boolean            = period.hasAnyOpenObligation
        val isInReceivedPeriod:   Boolean            = period.isInReceivedPeriod

        (uktrObligation, girObligation) match {
          case (Some(uktr), Some(gir)) =>
            (uktr.status, gir.status, dueDatePassed, isInReceivedPeriod) match {
              case (ObligationStatus.Open, ObligationStatus.Open, false, _)          => Some(Due)
              case (ObligationStatus.Open, ObligationStatus.Fulfilled, false, _)     => Some(Due)
              case (ObligationStatus.Fulfilled, ObligationStatus.Open, false, _)     => Some(Due)
              case (ObligationStatus.Open, ObligationStatus.Open, true, _)           => Some(Overdue)
              case (ObligationStatus.Open, ObligationStatus.Fulfilled, true, _)      => Some(Incomplete)
              case (ObligationStatus.Fulfilled, ObligationStatus.Open, true, _)      => Some(Incomplete)
              case (ObligationStatus.Fulfilled, ObligationStatus.Fulfilled, _, true) => Some(Received)
              case _ if hasAnyOpenObligation && !dueDatePassed                       => Some(Due)
              case _ if hasAnyOpenObligation && dueDatePassed                        => Some(Overdue)
              case _                                                                 => None
            }
          case (Some(uktr), None) =>
            (uktr.status, dueDatePassed) match {
              case (ObligationStatus.Open, false) => Some(Due)
              case (ObligationStatus.Open, true)  => Some(Overdue)
              case _                              => None
            }
          case (None, Some(gir)) =>
            (gir.status, dueDatePassed) match {
              case (ObligationStatus.Open, false) => Some(Due)
              case (ObligationStatus.Open, true)  => Some(Overdue)
              case _                              => None
            }
          case _ if hasAnyOpenObligation && !dueDatePassed => Some(Due)
          case _ if hasAnyOpenObligation && dueDatePassed  => Some(Overdue)
          case _                                           => None
        }
      }

    obligationsAndSubmissions.accountingPeriodDetails
      .flatMap(periodStatus)
      .maxOption

  }
}
