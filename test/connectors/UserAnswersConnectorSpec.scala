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

import org.apache.pekko.Done
import base.SpecBase
import models.InternalIssueError
import org.scalacheck.Gen
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpException

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
  private val errorCodes: Gen[Int] = Gen.oneOf(Seq(400, 403, 500, 501, 502, 503, 504))

  "UserAnswersConnectors" when {

    "save should be successful" in {
      stubResponse(s"$apiUrl/user-cache/registration-subscription/id", OK, testData.toString())
      val result = connector.save("id", testData)
      result.futureValue mustBe testData
    }

    "save should handle error responses" in {
      stubResponse(s"$apiUrl/user-cache/registration-subscription/id", INTERNAL_SERVER_ERROR, "Error message")
      val result = connector.save("id", testData)
      result.failed.futureValue mustBe an[HttpException]
    }

    "get should be successful" in {
      stubGet(s"$apiUrl/user-cache/registration-subscription/id", OK, testData.toString())
      val result = connector.get("id")
      result.futureValue mustBe Some(testData)
    }

    "get should return None if not found" in {
      stubGet(s"$apiUrl/user-cache/registration-subscription/id", NOT_FOUND, "")
      val result = connector.get("id")
      result.futureValue mustBe None
    }

    "get should throw HttpException on unexpected status" in {
      stubGet(s"$apiUrl/user-cache/registration-subscription/id", INTERNAL_SERVER_ERROR, "Error")
      val result = connector.get("id")
      result.failed.futureValue mustBe an[HttpException]
    }

    "getUserAnswers" should {

      "return none if no record is found" in {
        stubGet(s"$apiUrl/user-cache/registration-subscription/id", NOT_FOUND, "")
        val result = connector.getUserAnswer("id")
        result.futureValue mustBe None
      }

      "return a future failed error in case of any response else than 200 or 404" in {
        stubGet(s"$apiUrl/user-cache/registration-subscription/id", errorCodes.sample.value, testData.toString())
        val result = connector.getUserAnswer("id")
        result.failed.futureValue mustBe models.InternalIssueError
      }

      "return a UserAnswers object when a record is found" in {
        stubGet(s"$apiUrl/user-cache/registration-subscription/id", OK, testData.toString())
        val result = connector.getUserAnswer("id")
        result.futureValue.value.data mustEqual testData
      }
    }

    "remove" should {
      "return Done in case of a 200 response" in {
        stubDelete(s"$apiUrl/user-cache/registration-subscription/id", OK, "")
        val result = connector.remove("id")
        result.futureValue mustEqual Done
      }

      "return failure in case of a non-200 response" in {
        stubDelete(s"$apiUrl/user-cache/registration-subscription/id", errorCodes.sample.value, "")
        val result = connector.remove("id")
        result.failed.futureValue mustEqual InternalIssueError
      }
    }

  }
}
