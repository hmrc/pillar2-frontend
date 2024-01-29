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
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HttpResponse

class EnrolmentStoreProxyConnectorSpec extends SpecBase {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.enrolment-store-proxy.port" -> server.port()
    )
    .build()
  lazy val connector: EnrolmentStoreProxyConnector = app.injector.instanceOf[EnrolmentStoreProxyConnector]
  val enrolmentStoreProxyUrl    = "/enrolment-store-proxy/enrolment-store/enrolments"
  val enrolmentStoreProxy200Url = "/enrolment-store-proxy/enrolment-store/enrolments/HMRC-PILLAR2-ORG~PLRID~xxx200/groups"
  val enrolmentStoreProxy204Url = "/enrolment-store-proxy/enrolment-store/enrolments/HMRC-PILLAR2-ORG~PLRID~xxx204/groups"

  val enrolmentStoreProxyResponseJson: String =
    """{
      |  "principalGroupIds": [
      |    "ABCEDEFGI1234567",
      |    "ABCEDEFGI1234568"
      |  ],
      |  "delegatedGroupIds": [
      |    "ABCEDEFGI1234567",
      |    "ABCEDEFGI1234568"
      |  ]
      |}""".stripMargin

  val enrolmentStoreProxyResponseNoPrincipalIdJson: String =
    """{
      |  "principalGroupIds": []
      |}""".stripMargin

  "EnrolmentStoreProxyConnector when calling enrolmentStatus" when {

    "return 200 and a enrolmentStatus response when already enrolment exists" in {
      val plrRef = "xxx200"
      stubGet(enrolmentStoreProxy200Url, OK, enrolmentStoreProxyResponseJson)
      val result = connector.enrolmentExists(plrRef)
      result.futureValue mustBe Right(true)
    }

    "return 204 and a enrolmentStatus response when no enrolment exists" in {
      val plrRef = "xxx204"
      stubGet(enrolmentStoreProxy204Url, NO_CONTENT, "")

      val result = connector.enrolmentExists(plrRef)
      result.futureValue mustBe Right(false)
    }

    "return 204 enrolmentStatus response when principalGroupId is empty seq" in {
      val plrRef = "xxx204"
      stubGet(enrolmentStoreProxy204Url, OK, enrolmentStoreProxyResponseNoPrincipalIdJson)
      val result = connector.enrolmentExists(plrRef)
      result.futureValue mustBe Right(false)
    }

    "return 404 and a enrolmentStatus response when invalid or malfromed URL" in {
      val plrRef = "xxx404"
      stubGet(enrolmentStoreProxy204Url, NOT_FOUND, "")
      intercept[IllegalStateException](await(connector.enrolmentExists(plrRef)))

    }
  }
}
