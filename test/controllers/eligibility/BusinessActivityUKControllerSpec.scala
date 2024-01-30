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

import akka.stream.testkit.NoMaterializer
import base.SpecBase
import config.FrontendAppConfig
import controllers.actions.{SessionIdentifierAction, UnauthenticatedControllerComponents, UnauthenticatedDataRequiredAction, UnauthenticatedDataRetrievalAction}
import forms.BusinessActivityUKFormProvider
import helpers.{Configs, ViewInstances}
import models.UserAnswers
import models.requests.{SessionRequest, UnauthenticatedDataRequest, UnauthenticatedOptionalDataRequest}
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.matchers.should.Matchers.not.include
import play.api.http.{DefaultFileMimeTypes, FileMimeTypes, FileMimeTypesConfiguration}
import play.api.i18n.Messages.UrlMessageSource
import play.api.i18n._
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UnauthenticatedDataRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import uk.gov.hmrc.play.bootstrap.controller
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import java.util.Locale
import scala.concurrent.{ExecutionContext, Future}

class BusinessActivityUKControllerSpec extends SpecBase {
  val formProvider = new BusinessActivityUKFormProvider()

  val application = applicationBuilder(None).build()

  "Trading Business Confirmation Controller" when {

    "must return OK and the correct view for a GET" in {

      val request =
        FakeRequest(GET, controllers.eligibility.routes.BusinessActivityUKController.onPageLoad.url).withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustEqual OK
      contentAsString(result) should include(
        "Does the group have business operations in the UK?"
      )
    }

    "must redirect to the next page when valid data is submitted" in {

      val request =
        FakeRequest(POST, controllers.eligibility.routes.BusinessActivityUKController.onSubmit.url)
          .withFormUrlEncodedBody(("value", "yes"))
      val result = route(application, request).value
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.eligibility.routes.TurnOverEligibilityController.onPageLoad.url

    }
    "must redirect to the next page when valid data is submitted with no selected" in {

      val request =
        FakeRequest(POST, controllers.eligibility.routes.BusinessActivityUKController.onSubmit.url)
          .withFormUrlEncodedBody(("value", "no"))
      val result = route(application, request).value
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.eligibility.routes.KbUKIneligibleController.onPageLoad.url

    }
    "must show error page with 400 if no option is selected " in {
      val formData = Map("value" -> "")
      val request =
        FakeRequest(POST, controllers.eligibility.routes.BusinessActivityUKController.onSubmit.url).withFormUrlEncodedBody(formData.toSeq: _*)
      val result = route(application, request).value
      status(result) shouldBe 400
    }
  }
}
