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
import models.DynamicNotificationAreaState
import models.DynamicNotificationAreaState._
import models.DynamicNotificationAreaState.ReturnExpectedNotification._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalacheck.Gen
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import views.html.DynamicNotificationAreaView

class DynamicNotificationAreaViewSpec extends ViewSpecBase with ScalaCheckPropertyChecks {

  lazy val component: DynamicNotificationAreaView = inject[DynamicNotificationAreaView]

  lazy val organisationNotificationArea: DynamicNotificationAreaState => Document = notificationState =>
    Jsoup.parse(component(notificationState, isAgent = false).toString())
  lazy val agentNotificationArea: DynamicNotificationAreaState => Document = notificationState =>
    Jsoup.parse(component(notificationState, isAgent = true).toString())
  lazy val agentOrOrgNotificationArea: Gen[DynamicNotificationAreaState => Document] = Gen.oneOf(organisationNotificationArea, agentNotificationArea)

  val firstSectionBreakId       = "notifications-break-begin"
  val lastSectionBreakId        = "notifications-break-end"
  val accruingInterestSubheadId = "accruing-interest-notification-subheading"
  val uktrExpectedSubheadId     = "return-expected-notification-subheading"
  val accruingInterestBodyId    = "accruing-interest-notification-body"
  val uktrExpectedBodyId        = "return-expected-notification-body"
  val submitUktrLinkId          = "return-expected-notification-submission-link"
  val outstandingPaymentsLinkId = "accruing-interest-notification-outstanding-payments-link"

  "Dynamic notification area" must {
    "not render anything" in forAll(agentOrOrgNotificationArea) { template =>
      val page = template(NoNotification)
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

    "render a notification" when {
      "an outstanding payment is accruing interest" which {
        "includes the section breaks" in forAll(agentOrOrgNotificationArea, arbitrary[BigDecimal]) { (template, owed) =>
          val page = template(AccruingInterestNotification(owed))
          behave like includesSectionBreaks(page)
        }

        "has the proper link" when {
          "user is an organisation" in forAll(arbitrary[BigDecimal]) { owed =>
            val page = organisationNotificationArea(AccruingInterestNotification(owed))
            val link = page.getElementById(outstandingPaymentsLinkId)
            link.text() mustBe "View outstanding payments"
            link.attr("href") mustBe controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url
          }

          "user is an agent" in forAll(arbitrary[BigDecimal]) { owed =>
            val page = agentNotificationArea(AccruingInterestNotification(owed))
            val link = page.getElementById(outstandingPaymentsLinkId)
            link.text() mustBe "View outstanding payments"
            link.attr("href") mustBe controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url
          }
        }

        "has the correct subheading and message" when {
          "user is an organisation" in forAll(arbitrary[BigDecimal]) { owed =>
            val page = organisationNotificationArea(AccruingInterestNotification(owed))
            behave like hasAccruingInterestNotification(
              page,
              expectedHeading = s"You owe £${ViewUtils.formatAmount(owed)}",
              expectedMessage = "Your overdue payment is now subject to daily interest."
            )
          }

          "user is an agent" in forAll(arbitrary[BigDecimal]) { owed =>
            val page = agentNotificationArea(AccruingInterestNotification(owed))
            behave like hasAccruingInterestNotification(
              page,
              expectedHeading = s"Your client owes £${ViewUtils.formatAmount(owed)}",
              expectedMessage = "This payment is overdue, and is now subject to daily interest."
            )
          }
        }

      }

      "a UKTR is expected and there is no outstanding payment" which {

        val returnExpected: Gen[ReturnExpectedNotification] = Gen.oneOf(ReturnExpectedNotification.values)

        "includes the section breaks" in forAll(agentOrOrgNotificationArea, returnExpected) { (template, returnExpected) =>
          val page = template(returnExpected)
          behave like includesSectionBreaks(page)
        }

        "has the proper link" in forAll(agentOrOrgNotificationArea, returnExpected) { (template, returnExpected) =>
          val page = template(returnExpected)
          val link = page.getElementById(submitUktrLinkId)
          link.text() mustBe "View all due and overdue returns"
          link.attr("href") mustBe controllers.dueandoverduereturns.routes.DueAndOverdueReturnsController.onPageLoad.url
        }

        "has the proper heading and message" when {

          val orgExpectations = Table(
            ("Notification", "subhead", "message"),
            (ReturnExpectedNotification.Due, "You have one or more returns due", "Submit your returns before the due date to avoid penalties."),
            (
              ReturnExpectedNotification.Overdue,
              "You have overdue or incomplete returns",
              "You must submit or complete these returns as soon as possible."
            ),
            (
              ReturnExpectedNotification.Incomplete,
              "You have overdue or incomplete returns",
              "You must submit or complete these returns as soon as possible."
            )
          )

          "user is an organisation" in forAll(orgExpectations) { case (notification, expSubhead, expMessage) =>
            val page = organisationNotificationArea(notification)
            behave like hasUktrExpectedNotification(page, expSubhead, expMessage)
          }

          val agentExpectations = Table(
            ("Notification", "subhead", "message"),
            (ReturnExpectedNotification.Due, "One or more returns are now due", "Submit returns before the due date to avoid penalties."),
            (ReturnExpectedNotification.Overdue, "Overdue or incomplete returns", "Submit or complete these returns as soon as possible."),
            (ReturnExpectedNotification.Incomplete, "Overdue or incomplete returns", "Submit or complete these returns as soon as possible.")
          )

          "user is an agent" in forAll(agentExpectations) { case (notification, expSubhead, expMessage) =>
            val page = agentNotificationArea(notification)
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
