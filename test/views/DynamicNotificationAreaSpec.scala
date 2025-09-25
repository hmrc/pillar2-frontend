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
  lazy val anyNotificationArea: Gen[
    (
      Option[DueAndOverdueReturnBannerScenario],
      Option[OutstandingPaymentBannerScenario]
    ) => Document
  ] =
    Gen.oneOf(organisationNotificationArea, agentNotificationArea)

  lazy val firstSectionBreakId = "notifications-break-begin"
  lazy val lastSectionBreakId  = "notifications-break-end"
  lazy val subheadingId        = "notifications-subheading"
  lazy val messageId           = "notifications-body"
  lazy val trSubmissionLinkId  = "submission-link"

  "Dynamic notification area" should {
    "not render anything" when {
      val anyPaymentScenario: Gen[Option[OutstandingPaymentBannerScenario]] = Gen.option(Outstanding)
      val doNotRenderScenarios = Gen.option(Received)

      "return is nonexistent or already received" in forAll(anyNotificationArea, doNotRenderScenarios, anyPaymentScenario) {
        (template, uktr, payment) =>
          val page = template(uktr, payment)
          Option(page.getElementById(firstSectionBreakId)) must not be defined
          Option(page.getElementById(lastSectionBreakId))  must not be defined
          Option(page.getElementById(subheadingId))        must not be defined
          Option(page.getElementById(messageId))           must not be defined
          Option(page.getElementById(trSubmissionLinkId))  must not be defined
      }
    }

    "render a notification" when {
      "payment is not outstanding" which {
        val notOutstandingPayment = Option.empty[OutstandingPaymentBannerScenario]

        val scenariosWhichRenderNotification = Gen.oneOf(
          DueAndOverdueReturnBannerScenario.values.filter(_ != Received).map(Some.apply)
        )

        "includes the section breaks" in forAll(anyNotificationArea, scenariosWhichRenderNotification) { (template, uktrScenario) =>
          val page = template(uktrScenario, notOutstandingPayment)
          Option(page.getElementById(firstSectionBreakId)) mustBe defined
          Option(page.getElementById(lastSectionBreakId)) mustBe defined
        }

        "has the proper link" in forAll(anyNotificationArea, scenariosWhichRenderNotification) { (template, uktrScenario) =>
          val page = template(uktrScenario, notOutstandingPayment)
          val link = page.getElementById(trSubmissionLinkId)
          link.text() mustBe "View all due and overdue returns"
          link.attr("href") mustBe controllers.dueandoverduereturns.routes.DueAndOverdueReturnsController.onPageLoad.url
        }

        "has the proper heading and message" when {

          val orgExpectations = Table(
            ("Return scenario", "subhead", "message"),
            (Due, "You have one or more returns due", "Submit your returns before the due date to avoid penalties."),
            (Overdue, "You have overdue or incomplete returns", "You must submit or complete these returns as soon as possible."),
            (Incomplete, "You have overdue or incomplete returns", "You must submit or complete these returns as soon as possible.")
          )

          "user is an organisation" in forAll(orgExpectations) { case (scenario, expSubhead, expMessage) =>
            val page = organisationNotificationArea(Some(scenario), notOutstandingPayment)
            behave like hasHeadingAndMessage(page, expSubhead, expMessage)
          }

          val agentExpectations = Table(
            ("Return scenario", "subhead", "message"),
            (Due, "One or more returns are now due", "Submit returns before the due date to avoid penalties."),
            (Overdue, "Overdue or incomplete returns", "Submit or complete these returns as soon as possible."),
            (Incomplete, "Overdue or incomplete returns", "Submit or complete these returns as soon as possible.")
          )

          "user is an agent" in forAll(agentExpectations) { case (scenario, expSubhead, expMessage) =>
            val page = agentNotificationArea(Some(scenario), notOutstandingPayment)
            behave like hasHeadingAndMessage(page, expSubhead, expMessage)
          }
        }
      }
    }

    def hasHeadingAndMessage(page: Document, expectedHeading: String, expectedMessage: String): Assertion = {
      val subhead = page.getElementById(subheadingId)
      val message = page.getElementById(messageId)

      subhead.text() mustBe expectedHeading
      message.text() mustBe expectedMessage
    }

  }
}
