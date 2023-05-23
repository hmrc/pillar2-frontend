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

package controllers.actions

import base.SpecBase
import connectors.UserAnswersConnectors
import models.UserAnswers
import models.requests.{IdentifierRequest, OptionalDataRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito._
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeRequest
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with MockitoSugar {

  val mockUserAnswersConnectors = mock[UserAnswersConnectors]
  class Harness(userAnswersConnectors: UserAnswersConnectors) extends DataRetrievalActionImpl(userAnswersConnectors) {
    def callTransform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
  }

  "Data Retrieval Action" - {

    "when there is no data in the cache" - {

      "must set userAnswers to 'None' in the request" in {

        when(mockUserAnswersConnectors.get(any())(any())) thenReturn Future(None)
        val action = new Harness(mockUserAnswersConnectors)

        val result = action.callTransform(IdentifierRequest(FakeRequest(), "id")).futureValue

        result.userAnswers.get.data shouldBe Json.obj()
      }
    }

    "when there is data in the cache" - {

      "must build a userAnswers object and add it to the request" in {

        when(mockUserAnswersConnectors.get(any())(any())) thenReturn Future(Some(Json.obj("abc" -> "def")))
        val action = new Harness(mockUserAnswersConnectors)

        val result = action.callTransform(new IdentifierRequest(FakeRequest(), "id")).futureValue

        result.userAnswers mustBe defined
      }
    }
  }
}
