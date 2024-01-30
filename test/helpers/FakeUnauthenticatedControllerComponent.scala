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

package helpers

import akka.stream.testkit.NoMaterializer
import config.FrontendAppConfig
import controllers.actions.{SessionIdentifierAction, UnauthenticatedControllerComponents, UnauthenticatedDataRequiredAction, UnauthenticatedDataRetrievalAction}
import models.UserAnswers
import models.requests.{SessionRequest, UnauthenticatedDataRequest, UnauthenticatedOptionalDataRequest}
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import play.api.http.{DefaultFileMimeTypes, FileMimeTypes, FileMimeTypesConfiguration}
import play.api.i18n.Messages.UrlMessageSource
import play.api.i18n.{DefaultLangs, DefaultMessagesApi, Lang, Langs, Messages, MessagesApi}
import play.api.mvc.{AnyContentAsEmpty, DefaultActionBuilder, DefaultMessagesActionBuilderImpl, MessagesActionBuilder, PlayBodyParsers, Request, Result}
import play.api.test.Helpers.{stubBodyParser, stubPlayBodyParsers}
import repositories.UnauthenticatedDataRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import java.util.Locale
import scala.concurrent.{ExecutionContext, Future}

trait FakeUnauthenticatedControllerComponent
    extends UnauthenticatedControllerComponents
    with DefaultPlayMongoRepositorySupport[UserAnswers]
    with Configs {
  private val instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val mockAppConfig = mock[FrontendAppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1

  protected override val repository = new UnauthenticatedDataRepository(
    mongoComponent = mongoComponent,
    appConfig = mockAppConfig,
    clock = stubClock
  )(ec)

  val fs: Langs = new DefaultLangs(Seq(Lang(new Locale("en"))))

  implicit val m: Map[String, String] =
    Messages
      .parse(UrlMessageSource(this.getClass.getClassLoader.getResource("messages.en")), "")
      .toOption
      .getOrElse(Map.empty[String, String])

  implicit lazy val ma: MessagesApi =
    new DefaultMessagesApi(
      messages = Map("default" -> m),
      langs = fs
    )
  implicit lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  def testUserAnswers:  UserAnswers      = UserAnswers("id")

  val ua = new UnauthenticatedControllerComponents {

    override def actionBuilder: DefaultActionBuilder = DefaultActionBuilder(stubBodyParser(AnyContentAsEmpty))

    override def sessionRepository: UnauthenticatedDataRepository = repository

    override def identify: SessionIdentifierAction = new SessionIdentifierAction(
    ) {
      override def refine[A](request: Request[A]): Future[Either[Result, SessionRequest[A]]] = {

        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        Future.successful(Right(SessionRequest(request, "internalId")))
      }
    }

    override def getData: UnauthenticatedDataRetrievalAction = new UnauthenticatedDataRetrievalAction(repository) {
      override protected def transform[A](request: SessionRequest[A]): Future[UnauthenticatedOptionalDataRequest[A]] = {
        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        Future(UnauthenticatedOptionalDataRequest(request.request, request.userId, Some(testUserAnswers)))(ec)
      }
    }

    override def requireData: UnauthenticatedDataRequiredAction = new UnauthenticatedDataRequiredAction()(ec) {
      override protected def refine[A](request: UnauthenticatedOptionalDataRequest[A]): Future[Either[Result, UnauthenticatedDataRequest[A]]] =
        Future.successful(Right(UnauthenticatedDataRequest(request.request, request.userId, testUserAnswers)))
    }

    override def messagesActionBuilder: MessagesActionBuilder = new DefaultMessagesActionBuilderImpl(stubBodyParser(AnyContentAsEmpty), messagesApi)

    override def parsers: PlayBodyParsers = stubPlayBodyParsers(NoMaterializer)

    override def messagesApi: MessagesApi = ma

    override def langs: Langs = fs

    override def fileMimeTypes: FileMimeTypes = new DefaultFileMimeTypes(FileMimeTypesConfiguration())

    override def executionContext: ExecutionContext = ec
  }
  implicit lazy val appConfig: FrontendAppConfig = new FrontendAppConfig(configuration, servicesConfig)

}
