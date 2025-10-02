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

import base.SpecBase
import connectors.FinancialDataConnector
import models.{FinancialData, FinancialItem, FinancialTransaction}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future

class FinancialDataServiceSpec extends SpecBase {

  "FinancialDataService" should {

    "retrieveFinancialData" should {
      "return financial data from the connector" in {
        val application = applicationBuilder()
          .overrides(
            bind[FinancialDataConnector].toInstance(mockFinancialDataConnector)
          )
          .build()

        val service = application.injector.instanceOf[FinancialDataService]

        val expectedFinancialData = FinancialData(Seq(
          FinancialTransaction(
            mainTransaction = Some("6500"),
            subTransaction = Some("6233"),
            taxPeriodFrom = Some(LocalDate.now.minusMonths(1)),
            taxPeriodTo = Some(LocalDate.now),
            outstandingAmount = Some(BigDecimal(100)),
            items = Seq(FinancialItem(dueDate = Some(LocalDate.now.plusDays(1)), clearingDate = None))
          )
        ))

        when(mockFinancialDataConnector.retrieveFinancialData(any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(expectedFinancialData))

        val result = service.retrieveFinancialData("test-plr", LocalDate.now.minusYears(1), LocalDate.now)

        whenReady(result) { data =>
          data mustBe expectedFinancialData
        }
      }

      "propagate errors from the connector" in {
        val application = applicationBuilder()
          .overrides(
            bind[FinancialDataConnector].toInstance(mockFinancialDataConnector)
          )
          .build()

        val service = application.injector.instanceOf[FinancialDataService]

        val exception = new RuntimeException("Connector error")

        when(mockFinancialDataConnector.retrieveFinancialData(any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.failed(exception))

        val result = service.retrieveFinancialData("test-plr", LocalDate.now.minusYears(1), LocalDate.now)

        whenReady(result.failed) { error =>
          error mustBe exception
        }
      }
    }
  }
}
