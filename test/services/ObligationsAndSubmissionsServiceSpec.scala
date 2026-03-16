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

package services

import base.SpecBase
import connectors.ObligationsAndSubmissionsConnector
import models.DueAndOverdueReturnBannerScenario
import models.obligationsandsubmissions.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import play.api.Application
import play.api.inject.bind
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{LocalDate, ZonedDateTime}
import scala.concurrent.{ExecutionContext, Future}

class ObligationsAndSubmissionsServiceSpec extends SpecBase {

  val application: Application = applicationBuilder()
    .overrides(bind[ObligationsAndSubmissionsConnector].toInstance(mockObligationsAndSubmissionsConnector))
    .build()

  val service:   ObligationsAndSubmissionsService = application.injector.instanceOf[ObligationsAndSubmissionsService]
  val pillar2Id: String                           = PlrReference

  private val fromDate = LocalDate.now().minusMonths(12)
  private val toDate   = LocalDate.now()

  private def obligation(
    obligationType: ObligationType,
    status:         ObligationStatus,
    submissions:    Seq[Submission] = Seq.empty
  ): Obligation =
    Obligation(
      obligationType = obligationType,
      status = status,
      canAmend = false,
      submissions = submissions
    )

  private def submission(
    submissionType: SubmissionType,
    receivedDate:   ZonedDateTime
  ): Submission =
    Submission(
      submissionType = submissionType,
      receivedDate = receivedDate,
      country = None
    )

  private def period(
    dueDate:      LocalDate,
    obligations:  Seq[Obligation],
    endDate:      LocalDate = toDate,
    underEnquiry: Boolean = false
  ): AccountingPeriodDetails =
    AccountingPeriodDetails(
      startDate = fromDate,
      endDate = endDate,
      dueDate = dueDate,
      underEnquiry = underEnquiry,
      obligations = obligations
    )

  private def data(periods: Seq[AccountingPeriodDetails]): ObligationsAndSubmissionsSuccess =
    ObligationsAndSubmissionsSuccess(
      processingDate = ZonedDateTime.now(),
      accountingPeriodDetails = periods
    )

  private def setupMockConnector(response: Future[ObligationsAndSubmissionsSuccess]): OngoingStubbing[Future[ObligationsAndSubmissionsSuccess]] =
    when(mockObligationsAndSubmissionsConnector.getData(any(), any(), any())(using any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(response)

  "handleData" must {
    "return obligations and submissions when the connector returns valid data" in {
      val successResponse = obligationsAndSubmissionsSuccessResponse().success

      running(application) {
        setupMockConnector(Future.successful(successResponse))

        val result = service.handleData(pillar2Id, fromDate, toDate).futureValue

        result mustBe successResponse
      }
    }

    "throw exception when connector fails" in
      running(application) {
        setupMockConnector(Future.failed(new RuntimeException))

        whenReady(service.handleData(pillar2Id, fromDate, toDate).failed)(exception => exception mustBe a[RuntimeException])
      }

    "return empty response when connector returns empty obligations and submissions" in {
      val emptyResponse = ObligationsAndSubmissionsSuccess(
        processingDate = ZonedDateTime.now(),
        accountingPeriodDetails = Seq.empty
      )

      running(application) {
        setupMockConnector(Future.successful(emptyResponse))

        val result = service.handleData(pillar2Id, fromDate, toDate).futureValue

        result mustBe emptyResponse
        result.processingDate mustBe a[ZonedDateTime]
        result.accountingPeriodDetails mustBe Seq.empty
      }
    }
  }

  "ObligationsAndSubmissionsService.getDueOrOverdueReturnsStatus" must {
    "return Due when UKTR is open and due date has not passed" in {
      val result = service.getDueOrOverdueReturnsStatus(
        data(
          Seq(
            period(
              dueDate = LocalDate.now().plusDays(1),
              obligations = Seq(
                obligation(ObligationType.UKTR, ObligationStatus.Open)
              )
            )
          )
        )
      )

      result mustBe Some(DueAndOverdueReturnBannerScenario.Due)
    }

    "return Overdue when UKTR is open and due date has passed" in {
      val result = service.getDueOrOverdueReturnsStatus(
        data(
          Seq(
            period(
              dueDate = LocalDate.now().minusDays(1),
              obligations = Seq(
                obligation(ObligationType.UKTR, ObligationStatus.Open)
              )
            )
          )
        )
      )

      result mustBe Some(DueAndOverdueReturnBannerScenario.Overdue)
    }

    "return Received when both UKTR and GIR are fulfilled and within the received period" in {
      val recentSubmission = submission(
        SubmissionType.UKTR_CREATE,
        ZonedDateTime.now().minusDays(1)
      )

      val result = service.getDueOrOverdueReturnsStatus(
        data(
          Seq(
            period(
              dueDate = LocalDate.now().minusDays(1),
              obligations = Seq(
                obligation(
                  ObligationType.UKTR,
                  ObligationStatus.Fulfilled,
                  Seq(recentSubmission)
                ),
                obligation(
                  ObligationType.GIR,
                  ObligationStatus.Fulfilled
                )
              )
            )
          )
        )
      )

      result mustBe Some(DueAndOverdueReturnBannerScenario.Received)
    }

    "return None when both UKTR and GIR obligations are fulfilled" in {
      val result = service.getDueOrOverdueReturnsStatus(
        data(
          Seq(
            period(
              dueDate = LocalDate.now().minusDays(7),
              obligations = Seq(
                obligation(ObligationType.UKTR, ObligationStatus.Fulfilled),
                obligation(ObligationType.GIR, ObligationStatus.Fulfilled)
              )
            )
          )
        )
      )

      result mustBe None
    }

    "return Due when both UKTR and GIR obligations are open and due date has not passed" in {
      val result = service.getDueOrOverdueReturnsStatus(
        data(
          Seq(
            period(
              dueDate = LocalDate.now().plusDays(7),
              obligations = Seq(
                obligation(ObligationType.UKTR, ObligationStatus.Open),
                obligation(ObligationType.GIR, ObligationStatus.Open)
              )
            )
          )
        )
      )

      result mustBe Some(DueAndOverdueReturnBannerScenario.Due)
    }

    "return Due when UKTR is open, GIR is fulfilled and due date has not passed" in {
      val result = service.getDueOrOverdueReturnsStatus(
        data(
          Seq(
            period(
              dueDate = LocalDate.now().plusDays(7),
              obligations = Seq(
                obligation(ObligationType.UKTR, ObligationStatus.Open),
                obligation(ObligationType.GIR, ObligationStatus.Fulfilled)
              )
            )
          )
        )
      )

      result mustBe Some(DueAndOverdueReturnBannerScenario.Due)
    }

    "return Due when UKTR is fulfilled, GIR is open and due date has not passed" in {
      val result = service.getDueOrOverdueReturnsStatus(
        data(
          Seq(
            period(
              dueDate = LocalDate.now().plusDays(7),
              obligations = Seq(
                obligation(ObligationType.UKTR, ObligationStatus.Fulfilled),
                obligation(ObligationType.GIR, ObligationStatus.Open)
              )
            )
          )
        )
      )

      result mustBe Some(DueAndOverdueReturnBannerScenario.Due)
    }

    "return Overdue when both UKTR and GIR obligations are open and due date has passed" in {
      val result = service.getDueOrOverdueReturnsStatus(
        data(
          Seq(
            period(
              dueDate = LocalDate.now().minusDays(7),
              obligations = Seq(
                obligation(ObligationType.UKTR, ObligationStatus.Open),
                obligation(ObligationType.GIR, ObligationStatus.Open)
              )
            )
          )
        )
      )

      result mustBe Some(DueAndOverdueReturnBannerScenario.Overdue)
    }

    "return Incomplete when UKTR is open, GIR is fulfilled and due date has passed" in {
      val result = service.getDueOrOverdueReturnsStatus(
        data(
          Seq(
            period(
              dueDate = LocalDate.now().minusDays(7),
              obligations = Seq(
                obligation(ObligationType.UKTR, ObligationStatus.Open),
                obligation(ObligationType.GIR, ObligationStatus.Fulfilled)
              )
            )
          )
        )
      )

      result mustBe Some(DueAndOverdueReturnBannerScenario.Incomplete)
    }

    "return Incomplete when UKTR is fulfilled, GIR is open and due date has passed" in {
      val result = service.getDueOrOverdueReturnsStatus(
        data(
          Seq(
            period(
              dueDate = LocalDate.now().minusDays(7),
              obligations = Seq(
                obligation(ObligationType.UKTR, ObligationStatus.Fulfilled),
                obligation(ObligationType.GIR, ObligationStatus.Open)
              )
            )
          )
        )
      )

      result mustBe Some(DueAndOverdueReturnBannerScenario.Incomplete)
    }

    "return None when only UKTR obligation is fulfilled" in {
      val result = service.getDueOrOverdueReturnsStatus(
        data(
          Seq(
            period(
              dueDate = LocalDate.now().minusDays(7),
              obligations = Seq(
                obligation(ObligationType.UKTR, ObligationStatus.Fulfilled)
              )
            )
          )
        )
      )

      result mustBe None
    }

    "return Due when only GIR obligation is open and due date has not passed" in {
      val result = service.getDueOrOverdueReturnsStatus(
        data(
          Seq(
            period(
              dueDate = LocalDate.now().plusDays(7),
              obligations = Seq(
                obligation(ObligationType.GIR, ObligationStatus.Open)
              )
            )
          )
        )
      )

      result mustBe Some(DueAndOverdueReturnBannerScenario.Due)
    }

    "return Overdue when only GIR obligation is open and due date has passed" in {
      val result = service.getDueOrOverdueReturnsStatus(
        data(
          Seq(
            period(
              dueDate = LocalDate.now().minusDays(7),
              obligations = Seq(
                obligation(ObligationType.GIR, ObligationStatus.Open)
              )
            )
          )
        )
      )

      result mustBe Some(DueAndOverdueReturnBannerScenario.Overdue)
    }

    "return None when only GIR obligation is fulfilled" in {
      val result = service.getDueOrOverdueReturnsStatus(
        data(
          Seq(
            period(
              dueDate = LocalDate.now().minusDays(7),
              obligations = Seq(
                obligation(ObligationType.GIR, ObligationStatus.Fulfilled)
              )
            )
          )
        )
      )

      result mustBe None
    }

    "return None when no accounting periods exist" in {
      val result = service.getDueOrOverdueReturnsStatus(
        data(Seq.empty)
      )

      result mustBe None
    }

    "return the earliest period status when multiple accounting periods exist" in {
      val firstPeriod = period(
        dueDate = LocalDate.now().plusDays(7),
        endDate = LocalDate.now().minusDays(10),
        obligations = Seq(
          obligation(ObligationType.UKTR, ObligationStatus.Open)
        )
      )

      val secondPeriod = period(
        dueDate = LocalDate.now().minusDays(7),
        endDate = LocalDate.now().minusDays(5),
        obligations = Seq(
          obligation(ObligationType.UKTR, ObligationStatus.Open)
        )
      )

      val result = service.getDueOrOverdueReturnsStatus(
        data(Seq(secondPeriod, firstPeriod))
      )

      result mustBe Some(DueAndOverdueReturnBannerScenario.Overdue)
    }

    "return None when UKTR and GIR obligations are both fulfilled and outside 60 day period" in {
      val oldSubmissionDate = ZonedDateTime.now().minusDays(70)

      val result = service.getDueOrOverdueReturnsStatus(
        data(
          Seq(
            period(
              dueDate = LocalDate.now().minusDays(7),
              obligations = Seq(
                obligation(
                  ObligationType.UKTR,
                  ObligationStatus.Fulfilled,
                  Seq(submission(SubmissionType.UKTR_CREATE, oldSubmissionDate))
                ),
                obligation(
                  ObligationType.GIR,
                  ObligationStatus.Fulfilled,
                  Seq(submission(SubmissionType.GIR, oldSubmissionDate))
                )
              )
            )
          )
        )
      )

      result mustBe None
    }

    "return None when only UKTR is fulfilled and within 60 day period" in {
      val recentSubmissionDate = ZonedDateTime.now().minusDays(30)

      val result = service.getDueOrOverdueReturnsStatus(
        data(
          Seq(
            period(
              dueDate = LocalDate.now().minusDays(7),
              obligations = Seq(
                obligation(
                  ObligationType.UKTR,
                  ObligationStatus.Fulfilled,
                  Seq(submission(SubmissionType.UKTR_CREATE, recentSubmissionDate))
                )
              )
            )
          )
        )
      )

      result mustBe None
    }

    "return None when only GIR is fulfilled and within 60 day period" in {
      val recentSubmissionDate = ZonedDateTime.now().minusDays(30)

      val result = service.getDueOrOverdueReturnsStatus(
        data(
          Seq(
            period(
              dueDate = LocalDate.now().minusDays(7),
              obligations = Seq(
                obligation(
                  ObligationType.GIR,
                  ObligationStatus.Fulfilled,
                  Seq(submission(SubmissionType.GIR, recentSubmissionDate))
                )
              )
            )
          )
        )
      )

      result mustBe None
    }

    "return Received when multiple submissions exist and most recent for both obligations is within 60 days" in {
      val oldSubmissionDate    = ZonedDateTime.now().minusDays(70)
      val recentSubmissionDate = ZonedDateTime.now().minusDays(5)

      val result = service.getDueOrOverdueReturnsStatus(
        data(
          Seq(
            period(
              dueDate = LocalDate.now().minusDays(7),
              obligations = Seq(
                obligation(
                  ObligationType.UKTR,
                  ObligationStatus.Fulfilled,
                  Seq(submission(SubmissionType.UKTR_CREATE, oldSubmissionDate))
                ),
                obligation(
                  ObligationType.GIR,
                  ObligationStatus.Fulfilled,
                  Seq(submission(SubmissionType.GIR, recentSubmissionDate))
                )
              )
            )
          )
        )
      )

      result mustBe Some(DueAndOverdueReturnBannerScenario.Received)
    }
  }
}
