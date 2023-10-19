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
import models.SafeId
import org.scalacheck.Gen
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import scala.collection.Seq

class UserAnswersConnectorSpec extends SpecBase {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.pillar2.port" -> server.port()
    )
    .build()

  lazy val connector: UserAnswersConnectors = app.injector.instanceOf[UserAnswersConnectors]

  val apiUrl   = "/report-pillar2-top-up-taxes"
  val testData = Json.parse("""{"test": "data"}""".stripMargin)

  "UserAnswersConnectors" when {
    "save should be successful" in {

      stubResponse(s"$apiUrl/user-cache/registration-subscription/id", OK, testData.toString())
      val result = connector.save("id", testData)
      result.futureValue mustBe testData
    }

    "get should be successful" in {

      stubGet(s"$apiUrl/user-cache/registration-subscription/id", OK, testData.toString())
      val result = connector.get("id")
      result.futureValue mustBe Some(testData)
    }

  }

}
