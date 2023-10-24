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

package connectors

import base.SpecBase
import models.EnrolmentInfo
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

class TaxEnrolmentsConnectorSpec extends SpecBase {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.tax-enrolments.port" -> server.port()
    )
    .build()

  lazy val connector: TaxEnrolmentsConnector = app.injector.instanceOf[TaxEnrolmentsConnector]

  val apiUrl = "/report-pillar2-top-up-taxes"

  "TaxEnrolmentsConnector" when {
    val enrolmentInfo = EnrolmentInfo(crn = Some("crn"), ctUtr = Some("utr"), plrId = "plrId")
    "createEnrolment must return status as 204 for successful Tax Enrolment call" in {

      stubResponseForPutRequest(s"/tax-enrolments/service/HMRC-PILLAR2-ORG/enrolment", NO_CONTENT)
      val result = connector.createEnrolment(enrolmentInfo)
      result.futureValue mustBe Some(NO_CONTENT)
    }

    "must return status as 400 and BadRequest error" in {

      stubResponseForPutRequest(s"/tax-enrolments/service/HMRC-PILLAR2-ORG/enrolment", BAD_REQUEST)
      val result = connector.createEnrolment(enrolmentInfo)
      result.futureValue mustBe None
    }

    "must return status ServiceUnavailable Error" in {

      stubResponseForPutRequest(s"/tax-enrolments/service/HMRC-PILLAR2-ORG/enrolment", INTERNAL_SERVER_ERROR)
      val result = connector.createEnrolment(enrolmentInfo)
      result.futureValue mustBe None
    }

  }
}
