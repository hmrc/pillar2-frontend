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

package views

import base.ViewSpecBase
import models.{DueAndOverdueReturnBannerScenario, Outstanding, OutstandingPaymentBannerScenario}
import models.DueAndOverdueReturnBannerScenario._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import views.html.DynamicNotificationArea

class DynamicNotificationAreaSpec extends ViewSpecBase with ScalaCheckPropertyChecks {

  lazy val component: DynamicNotificationArea = inject[DynamicNotificationArea]

  lazy val organisationNotificationArea: (
    Option[DueAndOverdueReturnBannerScenario],
    Option[OutstandingPaymentBannerScenario]
  ) => Document = (uktrScenario, paymentScenario) => Jsoup.parse(component(uktrScenario, paymentScenario, isAgent = false).toString())
  lazy val agentNotificationArea: (
    Option[DueAndOverdueReturnBannerScenario],
    Option[OutstandingPaymentBannerScenario]
  ) => Document = (uktrScenario, paymentScenario) => Jsoup.parse(component(uktrScenario, paymentScenario, isAgent = true).toString())
  lazy val agentOrOrgNotificationArea: Gen[
    (
      Option[DueAndOverdueReturnBannerScenario],
      Option[OutstandingPaymentBannerScenario]
    ) => Document
  ] =
    Gen.oneOf(organisationNotificationArea, agentNotificationArea)

  val firstSectionBreakId       = "notifications-break-begin"
  val lastSectionBreakId        = "notifications-break-end"
  val accruingInterestSubheadId = "accruing-interest-notification-subheading"
  val uktrExpectedSubheadId     = "return-expected-notification-subheading"
  val accruingInterestBodyId    = "accruing-interest-notification-body"
  val uktrExpectedBodyId        = "return-expected-notification-body"
  val submitUktrLinkId          = "return-expected-notification-submission-link"
  val outstandingPaymentsLinkId = "accruing-interest-notification-outstanding-payments-link"

  "Dynamic notification area" must {
    "not render anything" when {
      val doNotExpectUktrScenario = Gen.option(Received)
      val anyPaymentScenario: Gen[Option[OutstandingPaymentBannerScenario]] = Gen.option(Outstanding(amountOutstanding = 12345.67))

      "UKTR is nonexistent or already received" in forAll(agentOrOrgNotificationArea, doNotExpectUktrScenario, anyPaymentScenario) {
        (template, uktr, payment) =>
          val page = template(uktr, payment)
          val allIds = Seq(
            firstSectionBreakId,
            lastSectionBreakId,
            accruingInterestSubheadId,
            uktrExpectedSubheadId,
            accruingInterestBodyId,
            uktrExpectedBodyId,
            submitUktrLinkId,
            outstandingPaymentsLinkId
          )

          allIds.flatMap(id => Option(page.getElementById(id))) mustBe empty
      }
    }

    "render a notification" when {
      "an outstanding payment is accruing interest" which {
        val outstandingPayment = Some(Outstanding(amountOutstanding = 12345.67))
        val overdueUktr        = Some(Overdue)

        "includes the section breaks" in forAll(agentOrOrgNotificationArea) { template =>
          val page = template(overdueUktr, outstandingPayment)
          behave like includesSectionBreaks(page)
        }

        "has the proper link" when {
          "user is an organisation" in {
            val page = organisationNotificationArea(overdueUktr, outstandingPayment)
            val link = page.getElementById(outstandingPaymentsLinkId)
            link.text() mustBe "View outstanding payments"
            link.attr("href") mustBe controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url
          }

          "user is an agent" in {
            val page = agentNotificationArea(overdueUktr, outstandingPayment)
            val link = page.getElementById(outstandingPaymentsLinkId)
            link.text() mustBe "View outstanding payments"
            link.attr("href") mustBe controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url
          }
        }

        "has the correct subheading and message" when {
          "user is an organisation" in {
            val page = organisationNotificationArea(overdueUktr, outstandingPayment)
            behave like hasAccruingInterestNotification(
              page,
              expectedHeading = "You owe £12,345.67",
              expectedMessage = "Your overdue payment is now subject to daily interest."
            )
          }

          "user is an agent" in {
            val page = agentNotificationArea(overdueUktr, outstandingPayment)
            behave like hasAccruingInterestNotification(
              page,
              expectedHeading = "Your client owes £12,345.67",
              expectedMessage = "This payment is overdue, and is now subject to daily interest."
            )
          }
        }

      }

      "a UKTR is expected" which {
        val notOutstandingPayment = Option.empty[OutstandingPaymentBannerScenario]

        val uktrExpectedScenarios = Gen.oneOf(
          DueAndOverdueReturnBannerScenario.values.filter(_ != Received).map(Some.apply)
        )

        "includes the section breaks" in forAll(agentOrOrgNotificationArea, uktrExpectedScenarios) { (template, uktrScenario) =>
          val page = template(uktrScenario, notOutstandingPayment)
          behave like includesSectionBreaks(page)
        }

        "has the proper link" in forAll(agentOrOrgNotificationArea, uktrExpectedScenarios) { (template, uktrScenario) =>
          val page = template(uktrScenario, notOutstandingPayment)
          val link = page.getElementById(submitUktrLinkId)
          link.text() mustBe "View all due and overdue returns"
          link.attr("href") mustBe controllers.dueandoverduereturns.routes.DueAndOverdueReturnsController.onPageLoad.url
        }

        "has the proper heading and message" when {

          val orgExpectations = Table(
            ("UKTR scenario", "subhead", "message"),
            (Due, "You have one or more returns due", "Submit your returns before the due date to avoid penalties."),
            (Overdue, "You have overdue or incomplete returns", "You must submit or complete these returns as soon as possible."),
            (Incomplete, "You have overdue or incomplete returns", "You must submit or complete these returns as soon as possible.")
          )

          "user is an organisation" in forAll(orgExpectations) { case (scenario, expSubhead, expMessage) =>
            val page = organisationNotificationArea(Some(scenario), notOutstandingPayment)
            behave like hasUktrExpectedNotification(page, expSubhead, expMessage)
          }

          val agentExpectations = Table(
            ("UKTR scenario", "subhead", "message"),
            (Due, "One or more returns are now due", "Submit returns before the due date to avoid penalties."),
            (Overdue, "Overdue or incomplete returns", "Submit or complete these returns as soon as possible."),
            (Incomplete, "Overdue or incomplete returns", "Submit or complete these returns as soon as possible.")
          )

          "user is an agent" in forAll(agentExpectations) { case (scenario, expSubhead, expMessage) =>
            val page = agentNotificationArea(Some(scenario), notOutstandingPayment)
            behave like hasUktrExpectedNotification(page, expSubhead, expMessage)
          }
        }
      }
    }

    def includesSectionBreaks(page: Document) =
      Seq(firstSectionBreakId, lastSectionBreakId).flatMap(id => Option(page.getElementById(id))) must have(length(2))

    def hasAccruingInterestNotification(page: Document, expectedHeading: String, expectedMessage: String): Assertion = {
      val subhead = page.getElementById(accruingInterestSubheadId)
      val message = page.getElementById(accruingInterestBodyId)

      subhead.text() mustBe expectedHeading
      message.text() mustBe expectedMessage
    }

    def hasUktrExpectedNotification(page: Document, expectedHeading: String, expectedMessage: String): Assertion = {
      val subhead = page.getElementById(uktrExpectedSubheadId)
      val message = page.getElementById(uktrExpectedBodyId)

      subhead.text() mustBe expectedHeading
      message.text() mustBe expectedMessage
    }

  }
}
