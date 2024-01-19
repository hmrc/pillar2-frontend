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
import connectors.{EnrolmentStoreProxyConnector, TaxEnrolmentsConnector}
import models.{EnrolmentCreationError, EnrolmentExistsError, EnrolmentInfo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future

class TaxEnrolmentServiceSpec extends SpecBase {

  val service: TaxEnrolmentService = app.injector.instanceOf[TaxEnrolmentService]

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
    )
    .overrides(
      bind[TaxEnrolmentsConnector].toInstance(mockTaxEnrolmentsConnector)
    )
    .build()

  "TaxEnrolmentService" when {
    val enrolmentInfo = EnrolmentInfo(crn = Some("crn"), ctUtr = Some("utr"), plrId = "plrId")
    "must create a Enrolment and call the taxEnrolmentsConnector returning with a Successful NO_CONTENT" in {

      when(mockEnrolmentStoreProxyConnector.enrolmentExists(any())(any(), any())).thenReturn(Future.successful(Right(false)))
      when(mockTaxEnrolmentsConnector.createEnrolment(any())(any(), any())).thenReturn(Future.successful(Some(1)))
      service.checkAndCreateEnrolment(enrolmentInfo).map { res =>
        res mustBe (Right(1))
      }
    }

    "must return none when any other Status  is received from taxEnrolments" in {

      when(mockEnrolmentStoreProxyConnector.enrolmentExists(any())(any(), any())).thenReturn(Future.successful(Right(false)))
      when(mockTaxEnrolmentsConnector.createEnrolment(any())(any(), any())).thenReturn(Future.successful(None))
      service.checkAndCreateEnrolment(enrolmentInfo).map { res =>
        res mustBe Left(EnrolmentCreationError)
      }
    }

    "must return EnrolmentExistsError when there is already an enrolment" in {
      when(mockEnrolmentStoreProxyConnector.enrolmentExists(any())(any(), any()))
        .thenReturn(Future.successful(Right(true)))

      when(mockTaxEnrolmentsConnector.createEnrolment(any())(any(), any()))
        .thenReturn(Future.successful(Some(NO_CONTENT)))

      service.checkAndCreateEnrolment(enrolmentInfo).map { res =>
        res mustBe Left(EnrolmentExistsError)
      }
    }

  }
}
