/*
 * Copyright 2025 HM Revenue & Customs
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
import connectors.ObligationsAndSubmissionsConnector
import models.obligationsandsubmissions.ObligationsAndSubmissionsSuccess
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import play.api.Application
import play.api.inject.bind
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ObligationsAndSubmissionsServiceSpec extends SpecBase {

  val application: Application = applicationBuilder()
    .overrides(bind[ObligationsAndSubmissionsConnector].toInstance(mockObligationsAndSubmissionsConnector))
    .build()

  val service:   ObligationsAndSubmissionsService = application.injector.instanceOf[ObligationsAndSubmissionsService]
  val pillar2Id: String                           = PlrReference

  private def setupMockConnector(response: Future[ObligationsAndSubmissionsSuccess]): OngoingStubbing[Future[ObligationsAndSubmissionsSuccess]] =
    when(mockObligationsAndSubmissionsConnector.getData(any(), any(), any())(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(response)

  "handleData" must {
    "return obligations and submissions when the connector returns valid data" in {
      val successResponse = obligationsAndSubmissionsSuccessResponse().success

      running(application) {
        setupMockConnector(Future.successful(successResponse))

        val result = service.handleData(pillar2Id, fromDate, toDate).futureValue

        result mustBe successResponse
      }
    }

    "throw exception when connector fails" in {
      running(application) {
        setupMockConnector(Future.failed(new RuntimeException))

        whenReady(service.handleData(pillar2Id, fromDate, toDate).failed)(exception => exception mustBe a[RuntimeException])
      }
    }
  }
}
