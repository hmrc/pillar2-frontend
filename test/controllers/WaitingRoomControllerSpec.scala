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

package controllers
import cats.syntax.either.*
import config.FrontendAppConfig
import controllers.actions.{FakeIdentifierAction, FakeSubscriptionDataRequiredAction, FakeSubscriptionDataRetrievalAction}
import models.longrunningsubmissions.{LongRunningSubmission, SubmissionLookupError, SubmissionState}
import models.requests.UserIdRequest
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.matchers.must
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.http.{HeaderNames, Status}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.JsPath
import play.api.mvc.{AnyContentAsEmpty, PlayBodyParsers}
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.{FakeRequest, ResultExtractors}
import queries.{Gettable, Settable}
import services.LongRunningSubmissionService
import uk.gov.hmrc.auth.core.Enrolments
import views.html.WaitingRoomView

import scala.concurrent.{ExecutionContext, Future}

class WaitingRoomControllerSpec
    extends AnyWordSpec
    with GuiceOneAppPerSuite
    with must.Matchers
    with MockitoSugar
    with ResultExtractors
    with HeaderNames
    with Status
    with OptionValues
    with ScalaCheckDrivenPropertyChecks {

  given request:  FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  given messages: Messages                            = app.injector.instanceOf[MessagesApi].preferred(Seq(Lang("EN")))
  given config:   FrontendAppConfig                   = app.injector.instanceOf[FrontendAppConfig]

  val waitingRoomView: WaitingRoomView = app.injector.instanceOf[WaitingRoomView]

  private val fakeIdAction = new FakeIdentifierAction(app.injector.instanceOf[PlayBodyParsers], Enrolments(Set.empty))

  private trait WaitingRoomTestCase {

    val submissionService: LongRunningSubmissionService = mock[LongRunningSubmissionService]

    val controller = new WaitingRoomController(
      app.injector.instanceOf[WaitingRoomController].controllerComponents,
      fakeIdAction,
      fakeIdAction,
      new FakeSubscriptionDataRetrievalAction(None),
      new FakeSubscriptionDataRequiredAction,
      submissionService,
      waitingRoomView
    )(app.injector.instanceOf[ExecutionContext], config)
  }

  case object DummyPage extends Gettable[Unit] with Settable[Unit] {
    override def path: JsPath = JsPath
  }

  val anySubmissionLookupError: Gen[SubmissionLookupError] = Gen.oneOf(
    SubmissionLookupError.UserAnswersNotFound("some-pillar-ref"),
    SubmissionLookupError.SpecificAnswerNotFound(DummyPage)
  )
  val anySubmissionError: Gen[SubmissionState.Error] = Gen.oneOf(
    SubmissionState.values.collect { case e: SubmissionState.Error => e }
  )
  val anyError: Gen[LongRunningSubmission.AnyError] =
    Gen.oneOf(anySubmissionLookupError.map(_.asLeft), anySubmissionError.map(_.asRight))

  "onPageLoad" must {
    "redirect to the configured submission page on completion" in forAll(Gen.oneOf(LongRunningSubmission.values)) { submission =>
      new WaitingRoomTestCase {
        when(submissionService.getCurrentState(eqTo(submission))(using any[UserIdRequest[_]]))
          .thenReturn(Future.successful(SubmissionState.Submitted.asRight))

        private val result = controller.onPageLoad(submission)(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe submission.submittedPage.url
      }
    }

    "return the waiting room view and instruct a refresh after the configured interval" in forAll(Gen.oneOf(LongRunningSubmission.values)) {
      submission =>
        new WaitingRoomTestCase {
          when(submissionService.getCurrentState(eqTo(submission))(using any[UserIdRequest[_]])).thenReturn(
            Future.successful(SubmissionState.Processing.asRight)
          )

          private val result = controller.onPageLoad(submission)(request)

          status(result) mustBe OK
          contentAsString(result) mustBe waitingRoomView(viewmodels.WaitingRoom.fromLongRunningSubmission.apply(submission)).toString()

          val expectedRefreshHeader: String =
            s"${config.longRunningSubmissionConfig(submission).pollingIntervalSeconds}, url=${routes.WaitingRoomController.onPageLoad(submission)}"

          headers(result).get("Refresh").value mustBe expectedRefreshHeader
        }
    }

    "redirect to the error route in the model on any modelled failure" in forAll(Gen.oneOf(LongRunningSubmission.values), anyError) {
      (submission, error) =>
        new WaitingRoomTestCase {
          when(submissionService.getCurrentState(eqTo(submission))(using any[UserIdRequest[_]])).thenReturn(
            Future.successful(error)
          )

          private val result = controller.onPageLoad(submission)(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe submission.errorPage(error).url
        }
    }
  }

}
