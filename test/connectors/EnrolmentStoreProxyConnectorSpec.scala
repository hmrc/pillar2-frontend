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

package connectors

import base.SpecBase
import models.GroupIds
import org.scalacheck.Gen
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

class EnrolmentStoreProxyConnectorSpec extends SpecBase {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.enrolment-store-proxy.port" -> server.port()
    )
    .build()
  lazy val connector: EnrolmentStoreProxyConnector = app.injector.instanceOf[EnrolmentStoreProxyConnector]
  private val enrolmentStoreProxy200Url = "/enrolment-store-proxy/enrolment-store/enrolments/HMRC-PILLAR2-ORG~PLRID~200/groups"
  private val enrolmentStoreProxy204Url = "/enrolment-store-proxy/enrolment-store/enrolments/HMRC-PILLAR2-ORG~PLRID~900/groups"

  val groupIds     = GroupIds(principalGroupIds = "ABCEDEFGI1234567", delegatedGroupIds = Seq("ABCEDEFGI1234568"))
  val jsonGroupIds = Json.toJson(groupIds).toString()

  private val errorCodes: Gen[Int] =
    Gen.oneOf(Seq(BAD_REQUEST, FORBIDDEN, NOT_FOUND, INTERNAL_SERVER_ERROR, BAD_GATEWAY, GATEWAY_TIMEOUT, SERVICE_UNAVAILABLE))

  "EnrolmentStoreProxyConnector when calling enrolment store" when {

    "return group IDs associated with an enrolment if 200 response is received" in {

      stubGet(enrolmentStoreProxy200Url, OK, jsonGroupIds)
      val result = connector.getGroupIds("200")
      result.futureValue mustBe Some(groupIds)
    }

    "return None for any non-200 status received for this API" in {
      stubGet(enrolmentStoreProxy204Url, errorCodes.sample.value, "")
      val result = connector.getGroupIds("900")
      result.futureValue mustBe None

    }

  }

}
