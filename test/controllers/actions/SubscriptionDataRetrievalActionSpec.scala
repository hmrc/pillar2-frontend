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

package controllers.actions

import base.SpecBase
import connectors.SubscriptionConnector
import models.subscription.{AccountingPeriod, SubscriptionLocalData}
import models.{MneOrDomestic, NonUKAddress}
import models.requests.{IdentifierRequest, OptionalSubscriptionDataRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future

class SubscriptionDataRetrievalActionSpec extends SpecBase {

  class Harness(subscriptionConnector: SubscriptionConnector) extends SubscriptionDataRetrievalActionImpl(subscriptionConnector)(ec) {
    def callTransform[A](request: IdentifierRequest[A]): Future[OptionalSubscriptionDataRequest[A]] = transform(request)
  }

  "Subscription Data Retrieval Action" when {

    "there is subscription data" must {

      "build the SubscriptionLocalData and add it to the request" in {

        val subscriptionConnector = mock[SubscriptionConnector]

        val testAddress          = NonUKAddress("line1", None, "line3", None, None, "UK")
        val testAccountingPeriod = AccountingPeriod(startDate = LocalDate.now(), endDate = LocalDate.now().plusDays(30))
        val subscriptionLocalData = SubscriptionLocalData(
          subMneOrDomestic = MneOrDomestic.Uk,
          subAccountingPeriod = testAccountingPeriod,
          subPrimaryContactName = "John Doe",
          subPrimaryEmail = "john.doe@example.com",
          subPrimaryPhonePreference = true,
          subPrimaryCapturePhone = Some("123456789"),
          subAddSecondaryContact = true,
          subSecondaryContactName = Some("Jane Doe"),
          subSecondaryEmail = Some("jane.doe@example.com"),
          subSecondaryCapturePhone = Some("987654321"),
          subSecondaryPhonePreference = Some(true),
          subRegisteredAddress = testAddress
        )

        when(subscriptionConnector.getSubscriptionCache(any())(any(), any())).thenReturn(Future.successful(Some(subscriptionLocalData)))

        val action = new Harness(subscriptionConnector)
        val result = action.callTransform(IdentifierRequest(FakeRequest(), "id", Some("groupID"), userIdForEnrolment = "userId")).futureValue

        result.maybeSubscriptionLocalData mustBe Some(subscriptionLocalData) // Use `maybeSubscriptionLocalData`
      }
    }

    "there is no subscription data" must {

      "set subscriptionData to 'None' in the request" in {

        val subscriptionConnector = mock[SubscriptionConnector]

        when(subscriptionConnector.getSubscriptionCache(any())(any(), any())).thenReturn(Future.successful(None))

        val action = new Harness(subscriptionConnector)
        val result = action.callTransform(IdentifierRequest(FakeRequest(), "id", Some("groupID"), userIdForEnrolment = "userId")).futureValue

        result.maybeSubscriptionLocalData mustBe None // Use `maybeSubscriptionLocalData`
      }
    }
  }
}
