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

package controllers

import base.SpecBase
import models.MneOrDomestic
import models.subscription.{Subscription, UpeDetails}
import models.{ApiError, SubscriptionCreateError}
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ReadSubscriptionService
import uk.gov.hmrc.hmrcfrontend.controllers.routes
import uk.gov.hmrc.http.HeaderCarrier
import utils.RowStatus
import views.html.DashboardView

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}

class DashboardControllerSpec extends SpecBase {

  "Dashboard Controller" when {

    "must return OK and the correct view for a GET" in {

      val mockSubscription = Subscription(
        domesticOrMne = MneOrDomestic.Uk,
        groupDetailStatus = RowStatus.Completed,
        contactDetailsStatus = RowStatus.Completed,
        upeDetails = Some(
          UpeDetails(
            organisationName = "Test Org",
            registrationDate = LocalDate.now(),
            safeId = Some("SafeID12345"),
            customerIdentification1 = Some("CustID1"),
            customerIdentification2 = Some("CustID2"),
            domesticOnly = true,
            filingMember = true
          )
        ),
        formBundleNumber = Some("XMPLR0123456789")
      )

      when(mockReadSubscriptionService.readSubscription(anyString, anyString)(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Right(mockSubscription)))

      val application = applicationBuilder()
        .overrides(bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[DashboardView]

        val organisationName = "Test Org"
        val registrationDate = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
        val plrRef           = "XMPLR0123456789"

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(organisationName, registrationDate, plrRef)(
          request,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return InternalServerError when there's an error fetching subscription" in {
      when(mockReadSubscriptionService.readSubscription(anyString, anyString)(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(Left(SubscriptionCreateError)))

      val application = applicationBuilder()
        .overrides(bind[ReadSubscriptionService].toInstance(mockReadSubscriptionService))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.routes.DashboardController.onPageLoad.url)
        val result  = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        contentAsString(result) mustEqual "Failed to fetch subscription details"
      }
    }

  }

}
