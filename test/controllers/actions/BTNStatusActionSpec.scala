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

package controllers.actions

import cats.syntax.option._
import controllers.btn.routes
import helpers.{SubscriptionLocalDataFixture, UserAnswersFixture}
import models.btn.BTNStatus
import models.requests.SubscriptionDataRequest
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.TryValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import pages.EntitiesInsideOutsideUKPage
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import repositories.SessionRepository
import services.audit.AuditService
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BTNStatusActionSpec
    extends AnyWordSpec
    with MockitoSugar
    with ArgumentMatchersSugar
    with must.Matchers
    with UserAnswersFixture
    with SubscriptionLocalDataFixture
    with ScalaFutures
    with TryValues
    with ScalaCheckDrivenPropertyChecks {

  private val userId = "some-test-user-id"
  private val fakeRequest = SubscriptionDataRequest(
    FakeRequest("GET", "/some-example"),
    userId,
    emptySubscriptionLocalData,
    Set.empty[Enrolment],
    isAgent = false
  )
  private val successResult = Results.Ok("")
  private val successBlock: SubscriptionDataRequest[_] => Future[Result] = _ => Future.successful(successResult)

  trait BTNStatusActionTestCase {
    val mockSessionRepository: SessionRepository = mock[SessionRepository]
    val mockAuditService:      AuditService      = mock[AuditService]
    val statusAction:          BTNStatusAction   = new BTNStatusAction(mockSessionRepository, mockAuditService)
  }

  "subscription request action" when {
    "user has no session" should {
      "run the block" in new BTNStatusActionTestCase {
        when(mockSessionRepository.get(userId)).thenReturn(Future.successful(None))

        statusAction.subscriptionRequest.invokeBlock(fakeRequest, successBlock).futureValue mustBe successResult
      }
    }

    "user's BTN status is not submitted or processing" should {
      "run the block" in forAll(
        arbitrary[Option[String]].retryUntil(_.toSeq.intersect(Seq(BTNStatus.submitted, BTNStatus.processing)).isEmpty)
      ) { btnStatus =>
        new BTNStatusActionTestCase {
          when(mockSessionRepository.get(userId)).thenReturn(
            Future.successful {
              btnStatus.fold(emptyUserAnswers)(emptyUserAnswers.set(BTNStatus, _).success.value).some
            }
          )

          val result = statusAction.subscriptionRequest.invokeBlock(fakeRequest, successBlock).futureValue

          result mustBe successResult
        }
      }
    }

    "BTN status is processing" should {
      "redirect to the waiting room" in new BTNStatusActionTestCase {
        when(mockSessionRepository.get(userId)).thenReturn(Future.successful {
          emptyUserAnswers.set(BTNStatus, BTNStatus.processing).success.value.some
        })

        val result = statusAction.subscriptionRequest.invokeBlock(fakeRequest, successBlock).futureValue

        result mustBe Results.Redirect(routes.BTNWaitingRoomController.onPageLoad)
      }
    }

    "BTN status is submitted" should {
      "audit and redirect to knockback page" when {
        "entitiesInsideOutsideUk is true" in new BTNStatusActionTestCase {
          when(mockSessionRepository.get(userId)).thenReturn(Future.successful {
            emptyUserAnswers
              .set(BTNStatus, BTNStatus.submitted)
              .success
              .value
              .set(EntitiesInsideOutsideUKPage, true)
              .success
              .value
              .some
          })

          when(
            mockAuditService.auditBtnAlreadySubmitted(
              eqTo(emptySubscriptionLocalData.plrReference),
              eqTo(emptySubscriptionLocalData.subAccountingPeriod),
              entitiesInsideOutsideUk = eqTo(true)
            )(any[HeaderCarrier])
          ).thenReturn(Future.successful(AuditResult.Success))

          val result = statusAction.subscriptionRequest.invokeBlock(fakeRequest, successBlock).futureValue

          result mustBe Results.Redirect(routes.CheckYourAnswersController.cannotReturnKnockback)

          verify(mockAuditService).auditBtnAlreadySubmitted(
            eqTo(emptySubscriptionLocalData.plrReference),
            eqTo(emptySubscriptionLocalData.subAccountingPeriod),
            entitiesInsideOutsideUk = eqTo(true)
          )(any[HeaderCarrier])
        }

        "entitiesInsideOutsideUk is empty or false" in forAll(Gen.option(false)) { entitiesInsideOutsideUk =>
          new BTNStatusActionTestCase {
            when(mockSessionRepository.get(userId)).thenReturn(Future.successful {
              entitiesInsideOutsideUk
                .foldLeft(
                  emptyUserAnswers
                    .set(BTNStatus, BTNStatus.submitted)
                    .success
                    .value
                ) { (answers, insideOutsideUk) =>
                  answers
                    .set(EntitiesInsideOutsideUKPage, insideOutsideUk)
                    .success
                    .value
                }
                .some

            })

            when(
              mockAuditService.auditBtnAlreadySubmitted(
                eqTo(emptySubscriptionLocalData.plrReference),
                eqTo(emptySubscriptionLocalData.subAccountingPeriod),
                entitiesInsideOutsideUk = eqTo(false)
              )(any[HeaderCarrier])
            ).thenReturn(Future.successful(AuditResult.Success))

            val result = statusAction.subscriptionRequest.invokeBlock(fakeRequest, successBlock).futureValue

            result mustBe Results.Redirect(routes.CheckYourAnswersController.cannotReturnKnockback)

            verify(mockAuditService).auditBtnAlreadySubmitted(
              eqTo(emptySubscriptionLocalData.plrReference),
              eqTo(emptySubscriptionLocalData.subAccountingPeriod),
              entitiesInsideOutsideUk = eqTo(false)
            )(any[HeaderCarrier])
          }
        }
      }
    }
  }
}
