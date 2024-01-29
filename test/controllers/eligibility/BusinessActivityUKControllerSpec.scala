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

package controllers.eligibility

import base.SpecBase
import config.FrontendAppConfig
import controllers.actions.{DataRequiredActionImpl, DataRetrievalActionImpl, DefaultUnauthenticatedControllerComponents, SessionIdentifierAction, UnauthenticatedControllerComponents, UnauthenticatedDataRequiredAction, UnauthenticatedDataRetrievalAction}
import forms.BusinessActivityUKFormProvider
import models.UserAnswers
import models.requests.{DataRequest, IdentifierRequest, OptionalDataRequest, SessionRequest, UnauthenticatedDataRequest, UnauthenticatedOptionalDataRequest}
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.{DefaultFileMimeTypes, FileMimeTypesConfiguration}
import play.api.i18n.{DefaultMessagesApi, MessagesApi}
import play.api.mvc.{AnyContentAsEmpty, DefaultActionBuilder, DefaultMessagesActionBuilderImpl, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UnauthenticatedDataRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.Future

class BusinessActivityUKControllerSpec extends SpecBase with  DefaultPlayMongoRepositorySupport[UserAnswers] with UnauthenticatedControllerComponents {
  val formProvider = new BusinessActivityUKFormProvider()

  private val instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val mockAppConfig = mock[FrontendAppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1

  protected override val repository = new UnauthenticatedDataRepository(
    mongoComponent = mongoComponent,
    appConfig      = mockAppConfig,
    clock          = stubClock
  )(ec)
  implicit lazy val messagesApi: MessagesApi =
    new DefaultMessagesApi(
      messages = Map("default" -> messages),
      langs = langs
    )
  override def identifyAndGetData() =
    DefaultActionBuilder(stubBodyParser(AnyContentAsEmpty)) andThen
    fakeDataRequiredAction andThen
    fakeDataRetrievalAction andThen
    fakeDataRequiredAction

  def fakeIdentifier: SessionIdentifierAction  =
    new SessionIdentifierAction(
    ) {
      override def refine[A](request: Request[A]): Future[Either[Result, SessionRequest[A]]] = {

        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        Future.successful(Right(SessionRequest(request, "internalId")))
      }
    }

  def fakeDataRequiredAction: UnauthenticatedDataRequiredAction = new UnauthenticatedDataRequiredAction()(ec) {
    override protected def refine[A](request: UnauthenticatedOptionalDataRequest[A]): Future[Either[Result, UnauthenticatedDataRequest[A]]] =
      Future.successful(Right(UnauthenticatedDataRequest(request.request, request.userId, testUserAnswers)))
  }

  def fakeDataRetrievalAction: UnauthenticatedDataRetrievalAction = new UnauthenticatedDataRetrievalAction(repository) {
    override protected def transform[A](request: SessionRequest[A]): Future[UnauthenticatedOptionalDataRequest[A]] = {
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      Future(UnauthenticatedOptionalDataRequest(request.request, request.userId, Some(testUserAnswers)))(ec)
    }
  }


  def controller(): BusinessActivityUKController =
    new BusinessActivityUKController(
      formProvider,
      stubMessagesControllerComponents(),
      viewBusinessActivityUK,
      mockSessionData
    )

  "Trading Business Confirmation Controller" must {
    implicit val request = FakeRequest(controllers.eligibility.routes.BusinessActivityUKController.onPageLoad)

    "must return OK and the correct view for a GET" in {

      val request =
        FakeRequest(GET, controllers.eligibility.routes.BusinessActivityUKController.onPageLoad.url).withFormUrlEncodedBody(("value", "no"))

      val result = controller.onPageLoad()()(request)
      status(result) shouldBe OK
      contentAsString(result) should include(
        "Does the group have business operations in the UK?"
      )
    }

    "must redirect to the next page when valid data is submitted" in {

      val request =
        FakeRequest(POST, controllers.eligibility.routes.BusinessActivityUKController.onSubmit.url)
          .withFormUrlEncodedBody(("value", "yes"))
      val result = controller.onSubmit()()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.eligibility.routes.TurnOverEligibilityController.onPageLoad.url

    }
    "must redirect to the next page when valid data is submitted with no selected" in {

      val request =
        FakeRequest(POST, controllers.eligibility.routes.BusinessActivityUKController.onSubmit.url)
          .withFormUrlEncodedBody(("value", "no"))
      val result = controller.onSubmit()()(request)
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.eligibility.routes.KbUKIneligibleController.onPageLoad.url

    }
    "must show error page with 400 if no option is selected " in {
      val formData = Map("value" -> "")
      val request =
        FakeRequest(POST, controllers.eligibility.routes.BusinessActivityUKController.onSubmit.url).withFormUrlEncodedBody(formData.toSeq: _*)
      val result = controller.onSubmit()()(request)
      status(result) shouldBe 400
    }
  }
}
