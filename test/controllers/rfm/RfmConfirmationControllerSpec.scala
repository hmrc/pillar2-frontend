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

package controllers.rfm

import base.SpecBase
import cats.syntax.option.*
import config.FrontendAppConfig
import controllers.actions.{DataRequiredActionImpl, FakeDataRetrievalAction, FakeIdentifierAction}
import models.UserAnswers
import org.mockito.Mockito.when
import pages.{PlrReferencePage, RfmConfirmationPage}
import play.api.mvc.AnyContentAsEmpty
import play.api.mvc.{MessagesControllerComponents, PlayBodyParsers, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.*
import utils.DateTimeUtils.*
import views.html.rfm.RfmConfirmationView

import java.time.ZonedDateTime
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class RfmConfirmationControllerSpec extends SpecBase {
  val id:              String        = UUID.randomUUID().toString
  val groupId:         String        = UUID.randomUUID().toString
  val providerId:      String        = UUID.randomUUID().toString
  val providerType:    String        = UUID.randomUUID().toString
  val currentDateTime: ZonedDateTime = ZonedDateTime.now()
  val plrId:           String        = "12345678"

  val confirmationView: RfmConfirmationView = app.injector.instanceOf[RfmConfirmationView]

  trait RfmConfirmationTestCase {
    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, controllers.rfm.routes.RfmConfirmationController.onPageLoad().url)

    def userAnswers: UserAnswers => Option[UserAnswers] = _.some

    private def constructedUserAnswers = userAnswers(UserAnswers(id))

    lazy val mockSessionRepo: SessionRepository = {
      val repo = mock[SessionRepository]
      when(repo.get(id)).thenReturn(Future.successful(constructedUserAnswers))
      repo
    }

    lazy val enrolments: Set[Enrolment] = Set(
      Enrolment(
        key = "HMRC-PILLAR2-ORG",
        identifiers = Seq(
          EnrolmentIdentifier("PLRID", plrId),
          EnrolmentIdentifier("UTR", "ABC12345")
        ),
        state = "activated"
      )
    )

    def controller = new RfmConfirmationController(
      new FakeDataRetrievalAction(constructedUserAnswers),
      new FakeIdentifierAction(app.injector.instanceOf[PlayBodyParsers], Enrolments(enrolments)),
      app.injector.instanceOf[DataRequiredActionImpl],
      mockSessionRepo,
      app.injector.instanceOf[MessagesControllerComponents],
      confirmationView
    )(app.injector.instanceOf[ExecutionContext], app.injector.instanceOf[FrontendAppConfig])
  }

  "RfmConfirmation Controller" when {
    "must return OK and the correct view with content" in new RfmConfirmationTestCase {
      override val userAnswers: UserAnswers => Option[UserAnswers] = _.setOrException(RfmConfirmationPage, currentDateTime).some

      val result: Future[Result] = controller.onPageLoad()(request)

      status(result) mustEqual OK
      contentAsString(result) mustEqual confirmationView(plrId, currentDateTime)(
        request,
        applicationConfig,
        messages(app)
      ).toString

      contentAsString(result) must include("Replace filing member successful")
      contentAsString(result) must include("Your group’s filing member was replaced on")
      contentAsString(result) must include("As the new filing member, you have taken over the obligations to:")
      contentAsString(result) must include("act as HMRC’s primary contact in relation to the group’s Pillar 2 Top-up Taxes compliance")
      contentAsString(result) must include("submit your group’s Pillar 2 Top-up Taxes returns")
      contentAsString(result) must include("ensure your group’s Pillar 2 Top-up Taxes account accurately reflects their records.")
      contentAsString(result) must include("If you fail to meet your obligations as a filing member, you may be liable for penalties.")
      contentAsString(result) must include("What happens next")
      contentAsString(result) must include("You can now")
      contentAsString(result) must include("report and manage your group's Pillar 2 Top-up Taxes")
      contentAsString(result) must include("on behalf of your group.")
      contentAsString(result) must include("Print this page")
      contentAsString(result) must include(currentDateTime.toDateTimeGmtFormat)
    }

    "take pillar ID from session repo if missing from enrolments" in new RfmConfirmationTestCase {
      override lazy val enrolments: Set[Enrolment]                     = Set.empty
      override val userAnswers:     UserAnswers => Option[UserAnswers] =
        _.setOrException(PlrReferencePage, plrId).setOrException(RfmConfirmationPage, currentDateTime).some

      val result: Future[Result] = controller.onPageLoad()(request)
      status(result) mustEqual OK
      contentAsString(result) must include(plrId)
    }

    // FIXME - PIL-2670
    "redirect to journey recovery if no data found in session repository" ignore new RfmConfirmationTestCase {
      override val userAnswers: UserAnswers => Option[UserAnswers] = _ => Option.empty[UserAnswers]

      val result: Future[Result] = controller.onPageLoad()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
    }

    "redirect to journey recovery if there's no pillar 2 reference in session repository" in new RfmConfirmationTestCase {
      override lazy val enrolments: Set[Enrolment]                     = Set.empty
      override val userAnswers:     UserAnswers => Option[UserAnswers] = _ => UserAnswers(id).some

      val result: Future[Result] = controller.onPageLoad()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
    }

    "redirect to journey recovery if there's no confirmation page" in new RfmConfirmationTestCase {
      override val userAnswers: UserAnswers => Option[UserAnswers] = _ => UserAnswers(id).some

      val result: Future[Result] = controller.onPageLoad()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
    }
  }
}
