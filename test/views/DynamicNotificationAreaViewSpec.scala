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
import models.DynamicNotificationAreaState.*
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import views.behaviours.ViewScenario
import views.html.DynamicNotificationAreaView

class DynamicNotificationAreaViewSpec extends ViewSpecBase with ScalaCheckPropertyChecks {

  lazy val component: DynamicNotificationAreaView = inject[DynamicNotificationAreaView]

  lazy val organisationNotificationArea: DynamicNotificationAreaState => Document = notificationState =>
    Jsoup.parse(component(notificationState, isAgent = false).toString())
  lazy val agentNotificationArea: DynamicNotificationAreaState => Document = notificationState =>
    Jsoup.parse(component(notificationState, isAgent = true).toString())
  lazy val agentOrOrgNotificationArea: Gen[DynamicNotificationAreaState => Document] = Gen.oneOf(organisationNotificationArea, agentNotificationArea)

  val firstSectionBreakId             = "notifications-break-begin"
  val lastSectionBreakId              = "notifications-break-end"
  val accruingInterestSubheadId       = "accruing-interest-notification-subheading"
  val accruingInterestBodyId          = "accruing-interest-notification-body"
  val accruingInterestLinkId          = "accruing-interest-notification-link"
  val outstandingPaymentsBtnSubheadId = "outstanding-payments-btn-notification-subheading"
  val outstandingPaymentsBtnBodyId    = "outstanding-payments-btn-notification-body"
  val outstandingPaymentsBtnLinkId    = "outstanding-payments-btn-notification-link"
  val outstandingPaymentsSubheadId    = "outstanding-payments-notification-subheading"
  val outstandingPaymentsLinkId       = "outstanding-payments-notification-link"
  val uktrExpectedSubheadId           = "return-expected-notification-subheading"
  val uktrExpectedBodyId              = "return-expected-notification-body"
  val submitUktrLinkId                = "return-expected-notification-submission-link"

  "Dynamic notification area" must {
    "not render anything" in forAll(agentOrOrgNotificationArea) { template =>
      val page   = template(NoNotification)
      val allIds = Seq(
        firstSectionBreakId,
        lastSectionBreakId,
        accruingInterestSubheadId,
        accruingInterestBodyId,
        accruingInterestLinkId,
        outstandingPaymentsBtnSubheadId,
        outstandingPaymentsBtnBodyId,
        outstandingPaymentsBtnLinkId,
        outstandingPaymentsSubheadId,
        outstandingPaymentsLinkId,
        uktrExpectedSubheadId,
        uktrExpectedBodyId,
        submitUktrLinkId
      )

      allIds.flatMap(id => Option(page.getElementById(id))) mustBe empty
    }

    "render a notification" when {
      "an outstanding charge is accruing interest but there's no BTN submitted" which {
        "includes the section breaks" in forAll(agentOrOrgNotificationArea, arbitrary[BigDecimal]) { (template, owed) =>
          val page = template(AccruingInterest(owed))
          behave like includesSectionBreaks(page)
        }

        "has the proper link" when {
          val expectedHref = controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url

          "user is an organisation" in forAll(arbitrary[BigDecimal]) { owed =>
            val page = organisationNotificationArea(AccruingInterest(owed))
            val link = page.getElementById(accruingInterestLinkId)
            behave like isALinkWithTextAndHref("View outstanding payments", expectedHref, link)
          }

          "user is an agent" in forAll(arbitrary[BigDecimal]) { owed =>
            val page = agentNotificationArea(AccruingInterest(owed))
            val link = page.getElementById(accruingInterestLinkId)
            behave like isALinkWithTextAndHref("View outstanding payments", expectedHref, link)
          }
        }

        "has the correct subheading and message" when {
          "user is an organisation" in forAll(arbitrary[BigDecimal]) { owed =>
            val page    = organisationNotificationArea(AccruingInterest(owed))
            val subhead = page.getElementById(accruingInterestSubheadId)
            val message = page.getElementById(accruingInterestBodyId)

            behave like isASubheadingWithText(s"You owe £${ViewUtils.formatAmount(owed)}", subhead)
            behave like isABodyTextParagraph("Your overdue payment is now subject to daily interest.", message)
          }

          "user is an agent" in forAll(arbitrary[BigDecimal]) { owed =>
            val page    = agentNotificationArea(AccruingInterest(owed))
            val subhead = page.getElementById(accruingInterestSubheadId)
            val message = page.getElementById(accruingInterestBodyId)

            behave like isASubheadingWithText(s"Your client owes £${ViewUtils.formatAmount(owed)}", subhead)
            behave like isABodyTextParagraph("This payment is overdue, and is now subject to daily interest.", message)
          }
        }

      }

      "there is an outstanding charge and a BTN was submitted" which {
        "includes the section breaks" in forAll(agentOrOrgNotificationArea, arbitrary[BigDecimal]) { (template, owed) =>
          val page = template(OutstandingPaymentsWithBtn(owed))
          behave like includesSectionBreaks(page)
        }

        "has the proper link" when {
          val expectedHref = controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url

          "user is an organisation" in forAll(arbitrary[BigDecimal]) { owed =>
            val page = organisationNotificationArea(OutstandingPaymentsWithBtn(owed))
            val link = page.getElementById(outstandingPaymentsBtnLinkId)
            behave like isALinkWithTextAndHref("View outstanding payments", expectedHref, link)
          }

          "user is an agent" in forAll(arbitrary[BigDecimal]) { owed =>
            val page = agentNotificationArea(OutstandingPaymentsWithBtn(owed))
            val link = page.getElementById(outstandingPaymentsBtnLinkId)
            behave like isALinkWithTextAndHref("View outstanding payments", expectedHref, link)
          }
        }

        "has the correct subheading and message" when {
          "user is an organisation" in forAll(arbitrary[BigDecimal]) { owed =>
            val page    = organisationNotificationArea(OutstandingPaymentsWithBtn(owed))
            val subhead = page.getElementById(outstandingPaymentsBtnSubheadId)
            val message = page.getElementById(outstandingPaymentsBtnBodyId)

            behave like isASubheadingWithText(s"You owe £${ViewUtils.formatAmount(owed)}", subhead)
            behave like isABodyTextParagraph("You have submitted a Below-Threshold Notification but your group has outstanding payments.", message)
          }

          "user is an agent" in forAll(arbitrary[BigDecimal]) { owed =>
            val page    = agentNotificationArea(OutstandingPaymentsWithBtn(owed))
            val subhead = page.getElementById(outstandingPaymentsBtnSubheadId)
            val message = page.getElementById(outstandingPaymentsBtnBodyId)

            behave like isASubheadingWithText(s"Your client owes £${ViewUtils.formatAmount(owed)}", subhead)
            behave like isABodyTextParagraph("Your client has submitted a Below-Threshold Notification but has outstanding payments.", message)
          }
        }

      }

      "there is an outstanding charge and no BTN submitted" which {
        "includes the section breaks" in forAll(agentOrOrgNotificationArea, arbitrary[BigDecimal]) { (template, owed) =>
          val page = template(OutstandingPayments(owed))
          behave like includesSectionBreaks(page)
        }

        "has the proper link" when {
          val expectedHref = controllers.payments.routes.OutstandingPaymentsController.onPageLoad.url

          "user is an organisation" in forAll(arbitrary[BigDecimal]) { owed =>
            val page = organisationNotificationArea(OutstandingPayments(owed))
            val link = page.getElementById(outstandingPaymentsLinkId)
            behave like isALinkWithTextAndHref("View outstanding payments", expectedHref, link)
          }

          "user is an agent" in forAll(arbitrary[BigDecimal]) { owed =>
            val page = agentNotificationArea(OutstandingPayments(owed))
            val link = page.getElementById(outstandingPaymentsLinkId)
            behave like isALinkWithTextAndHref("View outstanding payments", expectedHref, link)
          }
        }

        "has the correct subheading" when {
          "user is an organisation" in forAll(arbitrary[BigDecimal]) { owed =>
            val page    = organisationNotificationArea(OutstandingPayments(owed))
            val subhead = page.getElementById(outstandingPaymentsSubheadId)

            behave like isASubheadingWithText(s"You owe £${ViewUtils.formatAmount(owed)}", subhead)
          }

          "user is an agent" in forAll(arbitrary[BigDecimal]) { owed =>
            val page    = agentNotificationArea(OutstandingPayments(owed))
            val subhead = page.getElementById(outstandingPaymentsSubheadId)

            behave like isASubheadingWithText(s"Your client owes £${ViewUtils.formatAmount(owed)}", subhead)
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
          val page         = template(returnExpected)
          val link         = page.getElementById(submitUktrLinkId)
          val expectedHref = controllers.dueandoverduereturns.routes.DueAndOverdueReturnsController.onPageLoad().url
          isALinkWithTextAndHref("View all due and overdue returns", expectedHref, link)
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
            val page    = organisationNotificationArea(notification)
            val subhead = page.getElementById(uktrExpectedSubheadId)
            val message = page.getElementById(uktrExpectedBodyId)

            behave like isASubheadingWithText(expSubhead, subhead)
            behave like isABodyTextParagraph(expMessage, message)
          }

          val agentExpectations = Table(
            ("Notification", "subhead", "message"),
            (ReturnExpectedNotification.Due, "One or more returns are now due", "Submit returns before the due date to avoid penalties."),
            (ReturnExpectedNotification.Overdue, "Overdue or incomplete returns", "Submit or complete these returns as soon as possible."),
            (ReturnExpectedNotification.Incomplete, "Overdue or incomplete returns", "Submit or complete these returns as soon as possible.")
          )

          "user is an agent" in forAll(agentExpectations) { case (notification, expSubhead, expMessage) =>
            val page    = agentNotificationArea(notification)
            val subhead = page.getElementById(uktrExpectedSubheadId)
            val message = page.getElementById(uktrExpectedBodyId)

            behave like isASubheadingWithText(expSubhead, subhead)
            behave like isABodyTextParagraph(expMessage, message)
          }
        }
      }

      val viewScenarios: Seq[ViewScenario] =
        Seq(
          ViewScenario("dueView", organisationNotificationArea(ReturnExpectedNotification.Due)),
          ViewScenario("overdueView", organisationNotificationArea(ReturnExpectedNotification.Overdue)),
          ViewScenario("incompleteView", organisationNotificationArea(ReturnExpectedNotification.Incomplete)),
          ViewScenario("accruingInterestView", organisationNotificationArea(AccruingInterest(100.00))),
          ViewScenario("outstandingPaymentsWithBtnView", organisationNotificationArea(OutstandingPaymentsWithBtn(100.00))),
          ViewScenario("outstandingPaymentsView", organisationNotificationArea(OutstandingPayments(100.00))),
          ViewScenario("dueAgentView", agentNotificationArea(ReturnExpectedNotification.Due)),
          ViewScenario("overdueAgentView", agentNotificationArea(ReturnExpectedNotification.Overdue)),
          ViewScenario("incompleteAgentView", agentNotificationArea(ReturnExpectedNotification.Incomplete)),
          ViewScenario("accruingInterestAgentView", agentNotificationArea(AccruingInterest(100.00))),
          ViewScenario("outstandingPaymentsWithBtnAgentView", agentNotificationArea(OutstandingPaymentsWithBtn(100.00))),
          ViewScenario("outstandingPaymentsAgentView", agentNotificationArea(OutstandingPayments(100.00)))
        )

      behaveLikeAccessiblePage(viewScenarios, requireTitleAndH1Tests = false)
    }

    def includesSectionBreaks(page: Document) =
      Seq(firstSectionBreakId, lastSectionBreakId).flatMap(id => Option(page.getElementById(id))) must have(length(2))

    def isASubheadingWithText(expectedHeadingText: String, element: Element): Assertion = {
      element.tagName() mustBe "h2"
      element.classNames() must contain("govuk-heading-s")
      element.text() mustBe expectedHeadingText
    }

    def isABodyTextParagraph(expectedBodyText: String, element: Element): Assertion = {
      element.tagName() mustBe "p"
      element.classNames() must contain("govuk-body")
      element.text() mustBe expectedBodyText
    }

    def isALinkWithTextAndHref(expectedText: String, href: String, element: Element): Assertion = {
      element.tagName() mustBe "a"
      element.classNames() must contain("govuk-link")
      element.text() mustBe expectedText
      Option(element.attr("href")).value mustBe href
    }
  }
}
