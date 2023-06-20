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

package helpers

import controllers.actions.{AuthenticatedIdentifierAction, DataRequiredActionImpl, DataRetrievalActionImpl}
import forms._
import models.UserAnswers
import models.requests.{DataRequest, IdentifierRequest, OptionalDataRequest}
import play.api.http.{HttpProtocol, MimeTypes}
import play.api.mvc._
import play.api.test._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.Future

trait ControllerBaseSpec
    extends BaseSpec
    with Results
    with MimeTypes
    with HttpProtocol
    with Writeables
    with EssentialActionCaller
    with RouteInvokers {

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

  def getTradingBusinessConfirmationFormProvider:   TradingBusinessConfirmationFormProvider   = new TradingBusinessConfirmationFormProvider()
  def getUpeNameRegistrationFormProvider:           UpeNameRegistrationFormProvider           = new UpeNameRegistrationFormProvider()
  def getBusinessActivityUKFormProvider:            BusinessActivityUKFormProvider            = new BusinessActivityUKFormProvider()
  def getTurnOverEligibilityProvider:               TurnOverEligibilityFormProvider           = new TurnOverEligibilityFormProvider()
  def getGroupTerritoriesFormProvider:              GroupTerritoriesFormProvider              = new GroupTerritoriesFormProvider()
  def getUPERegisteredInUKConfirmationFormProvider: UPERegisteredInUKConfirmationFormProvider = new UPERegisteredInUKConfirmationFormProvider()
  def getContactUPEByTelephoneFormProvider:         ContactUPEByTelephoneFormProvider         = new ContactUPEByTelephoneFormProvider()

}
