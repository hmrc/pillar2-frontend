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
import config.FrontendAppConfig
import models.UserAnswers
import models.requests.OptionalDataRequest
import models.rfm.RegistrationDate
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import pages.{RfmPillar2ReferencePage, RfmRegistrationDatePage}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, running}
import repositories.SessionRepository
import services.SubscriptionService

import java.time.LocalDate
import scala.concurrent.Future

class RfmSecurityQuestionCheckActionSpec extends SpecBase {

  class Harness(sessionRepository: SessionRepository, subscriptionService: SubscriptionService, appConfig: FrontendAppConfig)
      extends RfmSecurityQuestionCheckActionImpl(sessionRepository, subscriptionService, appConfig) {
    def callFilter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = filter(request)
  }

  val date: LocalDate = LocalDate.of(2024, 1, 31)
  val sessionRepositoryUserAnswers: UserAnswers = UserAnswers("id")
    .setOrException(RfmPillar2ReferencePage, "someID")
    .setOrException(RfmRegistrationDatePage, RegistrationDate(date))
  val userId: String = sessionRepositoryUserAnswers.id

  "Rfm check security questions action" must {
    "redirect the user to the mismatch page" when {
      "there are missing answers in session for the RFM security questions in the session cache" in {
        val application = applicationBuilder().build()

        when(mockSessionRepository.get(any())) thenReturn Future.successful(None)

        running(application) {
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val action    = new Harness(mockSessionRepository, mockSubscriptionService, appConfig)
          val result: Future[Result] = action
            .callFilter(
              OptionalDataRequest(
                FakeRequest(),
                sessionRepositoryUserAnswers.id,
                groupId = None,
                userAnswers = Some(sessionRepositoryUserAnswers)
              )
            )
            .map(res => res.get)

          status(result) mustBe 303
          redirectLocation(result) mustBe Some(controllers.rfm.routes.MismatchedRegistrationDetailsController.onPageLoad.url)

        }
      }
    }

    "redirect the user to the mismatch page" when {
      "when there is a mismatch between what is returned from read subscription and the session repository" in {
        val application = applicationBuilder(Some(sessionRepositoryUserAnswers)).build()

        val testUserAnswers = sessionRepositoryUserAnswers
          .setOrException(RfmRegistrationDatePage, RegistrationDate(LocalDate.of(2024, 2, 28)))

        when(mockSessionRepository.get(sessionRepositoryUserAnswers.id)) thenReturn Future.successful(Some(testUserAnswers))
        when(mockSubscriptionService.readSubscription(any())(any())) thenReturn Future.successful(subscriptionData)

        running(application) {
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val action    = new Harness(mockSessionRepository, mockSubscriptionService, appConfig)
          val result: Future[Result] = action
            .callFilter(
              OptionalDataRequest(
                FakeRequest(),
                sessionRepositoryUserAnswers.id,
                groupId = None,
                Some(testUserAnswers)
              )
            )
            .map(res => res.get)

          status(result) mustBe 303
          redirectLocation(result) mustBe Some(controllers.rfm.routes.MismatchedRegistrationDetailsController.onPageLoad.url)
        }
      }
    }

    "allow the user to continue with the journey" when {
      "when both questions have been answered in session and correspond with the answers stored" in {
        val application = applicationBuilder(Some(sessionRepositoryUserAnswers)).build()
        when(mockSessionRepository.get(sessionRepositoryUserAnswers.id)) thenReturn Future.successful(Some(sessionRepositoryUserAnswers))
        when(mockSubscriptionService.readSubscription(any())(any())) thenReturn Future.successful(subscriptionData)

        running(application) {
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val action    = new Harness(mockSessionRepository, mockSubscriptionService, appConfig)
          val result: Future[Option[Result]] = action.callFilter(
            OptionalDataRequest(
              FakeRequest(),
              sessionRepositoryUserAnswers.id,
              groupId = None,
              Some(sessionRepositoryUserAnswers)
            )
          )

          result.futureValue mustBe None
        }
      }
    }
    "allow the user to continue with the journey" when {
      "when the rfm feature flag is set to false" in {
        val application = applicationBuilder(Some(sessionRepositoryUserAnswers))
          .configure(
            Seq(
              "features.rfmAccessEnabled" -> false
            ): _*
          )
          .build()
        when(mockSessionRepository.get(sessionRepositoryUserAnswers.id)) thenReturn Future.successful(Some(sessionRepositoryUserAnswers))
        when(mockSubscriptionService.readSubscription(any())(any())) thenReturn Future.successful(subscriptionData)

        running(application) {
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val action    = new Harness(mockSessionRepository, mockSubscriptionService, appConfig)
          val result: Future[Option[Result]] = action.callFilter(
            OptionalDataRequest(
              FakeRequest(),
              sessionRepositoryUserAnswers.id,
              groupId = None,
              Some(sessionRepositoryUserAnswers)
            )
          )

          result.futureValue mustBe None
        }
      }
    }
  }
}
