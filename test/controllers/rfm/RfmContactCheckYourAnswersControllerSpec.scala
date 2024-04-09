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
import connectors.{EnrolmentConnector, UserAnswersConnectors}
import models.grs.{EntityType, GrsRegistrationResult, RegistrationStatus}
import models.registration._
import models.subscription.AccountingPeriod
import models.{DuplicateSubmissionError, InternalIssueError, MneOrDomestic, NonUKAddress, UKAddress, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages._
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.http.HttpResponse
import utils.RowStatus
import viewmodels.govuk.SummaryListFluency
import models.rfm.CorporatePosition

import java.time.LocalDate
import scala.concurrent.Future

class RfmContactCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  private val date = LocalDate.now()

  private val nonUkAddress = NonUKAddress(
    addressLine1 = "1 drive",
    addressLine2 = None,
    addressLine3 = "la la land",
    addressLine4 = None,
    postalCode = None,
    countryCode = "AB"
  )

  private val grsResponse = GrsResponse(
    Some(
      IncorporatedEntityRegistrationData(
        companyProfile = CompanyProfile(
          companyName = "ABC Limited",
          companyNumber = "1234",
          dateOfIncorporation = date,
          unsanitisedCHROAddress = IncorporatedEntityAddress(address_line_1 = Some("line 1"), None, None, None, None, None, None, None)
        ),
        ctutr = "1234567890",
        identifiersMatch = true,
        businessVerification = None,
        registration = GrsRegistrationResult(
          registrationStatus = RegistrationStatus.Registered,
          registeredBusinessPartnerId = Some("XB0000000000001"),
          failures = None
        )
      )
    )
  )

  private val rfmCorpPosition = emptyUserAnswers
    .setOrException(RfmCorporatePositionPage, CorporatePosition.Upe)

  private val rfmNoID = emptyUserAnswers
    .setOrException(RfmCorporatePositionPage, CorporatePosition.NewNfm)
    .setOrException(RfmUkBasedPage, false)
    .setOrException(RfmNameRegistrationPage, "name")
    .setOrException(RfmRegisteredAddressPage, nonUkAddress)

  private val rfmID = emptyUserAnswers
    .setOrException(RfmCorporatePositionPage, CorporatePosition.NewNfm)
    .setOrException(RfmUkBasedPage, true)
    .setOrException(RfmEntityTypePage, EntityType.UkLimitedCompany)
    .setOrException(RfmGRSResponsePage, grsResponse)

  "Check Your Answers Controller" must {
    "on page load method " should {

      "return OK and the correct view if an answer is provided to every New RFM-UPE journey question" in {

        val application = applicationBuilder(userAnswers = Some(rfmCorpPosition))
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()
        running(application) {
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
          val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value
          status(result) mustEqual OK

          contentAsString(result) must include("Filing member details")
          contentAsString(result) must include("Ultimate parent entity (UPE)")
        }
      }

      "return OK and the correct view if an answer is provided to every New RFM ID journey questions" in {

        val application = applicationBuilder(userAnswers = Some(rfmID))
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()
        running(application) {
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
          val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value
          status(result) mustEqual OK

          contentAsString(result) must include("Filing member details")
          contentAsString(result) must include("New nominated filing member")
          contentAsString(result) must include(
            "Company"
          )
          contentAsString(result) must include("ABC Limited")
          contentAsString(result) must include(
            "Company Registration Number"
          )
          contentAsString(result) must include("1234")
          contentAsString(result) must include(
            "Unique Taxpayer Reference"
          )
          contentAsString(result) must include("1234567890")
        }
      }

      "return OK and the correct view if an answer is provided to every New RFM No ID journey questions" in {

        val application = applicationBuilder(userAnswers = Some(rfmNoID))
          .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
          .build()
        running(application) {
          when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
          val request = FakeRequest(GET, controllers.rfm.routes.RfmContactCheckYourAnswersController.onPageLoad.url)
          val result  = route(application, request).value
          status(result) mustEqual OK

          contentAsString(result) must include("Filing member details")
          contentAsString(result) must include("New nominated filing member")
          contentAsString(result) must include("name")
        }
      }

    }
  }
}
