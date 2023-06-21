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

package base

import akka.actor.ActorSystem
import akka.stream.Materializer
import config.FrontendAppConfig
import controllers.actions._
import forms.{BusinessActivityUKFormProvider, GroupTerritoriesFormProvider, TradingBusinessConfirmationFormProvider, TurnOverEligibilityFormProvider, UPERegisteredInUKConfirmationFormProvider, UpeNameRegistrationFormProvider, UpeRegisteredAddressFormProvider}
import helpers.{AllMocks, ViewInstances}
import models.UserAnswers
import models.requests.{DataRequest, IdentifierRequest, OptionalDataRequest}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import play.api.Application
import play.api.http.{HeaderNames, HttpProtocol, MimeTypes, Status}
import play.api.i18n.{DefaultLangs, Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.{EssentialActionCaller, FakeRequest, ResultExtractors, Writeables}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.play.language.LanguageUtils

import scala.concurrent.{ExecutionContext, Future}

trait SpecBase
    extends AnyWordSpec
    with TryValues
    with OptionValues
    with ScalaFutures
    with BeforeAndAfterEach
    with Matchers
    with Results
    with HttpProtocol
    with AllMocks
    with ResultExtractors
    with Status
    with MimeTypes
    with Writeables
    with EssentialActionCaller
    with HeaderNames
    with ViewInstances
    with IntegrationPatience {

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  val userAnswersId: String = "id"

  def testUserAnswers:            UserAnswers       = UserAnswers(userAnswersId)
  implicit lazy val ec:           ExecutionContext  = scala.concurrent.ExecutionContext.Implicits.global
  implicit lazy val hc:           HeaderCarrier     = HeaderCarrier()
  implicit lazy val appConfig:    FrontendAppConfig = new FrontendAppConfig(configuration)
  implicit lazy val system:       ActorSystem       = ActorSystem()
  implicit lazy val materializer: Materializer      = Materializer(system)

  def countOccurrences(src: String, tgt: String): Int =
    src.sliding(tgt.length).count(window => window == tgt)
  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())
  val languageUtil = new LanguageUtils(new DefaultLangs(), configuration)
  def appConfig(app: Application): FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  def preAuthenticatedActionBuilders: AuthenticatedIdentifierAction =
    new AuthenticatedIdentifierAction(
      mockAuthConnector,
      mockFrontendAppConfig,
      new BodyParsers.Default
    ) {
      override def refine[A](request: Request[A]): Future[Either[Result, IdentifierRequest[A]]] = {

        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        Future.successful(Right(IdentifierRequest(request, "internalId")))
      }
    }

  def preDataRequiredActionImpl: DataRequiredActionImpl = new DataRequiredActionImpl()(ec) {
    override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] =
      Future.successful(Right(DataRequest(request.request, request.userId, UserAnswers("12345"))))
  }

  def preDataRetrievalActionImpl: DataRetrievalActionImpl = new DataRetrievalActionImpl(mockUserAnswersConnectors)(ec) {
    override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = {
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      Future(OptionalDataRequest(request.request, request.userId, Some(testUserAnswers)))(ec)
    }
  }
  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
      )

}
