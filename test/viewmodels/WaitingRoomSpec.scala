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

package viewmodels
import models.longrunningsubmissions.LongRunningSubmission.*
import org.scalatest.matchers.must
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.Messages
import play.api.i18n.{Lang, MessagesApi}

class WaitingRoomSpec extends AnyWordSpec with GuiceOneAppPerSuite with must.Matchers with TableDrivenPropertyChecks {

  given messages: Messages = app.injector.instanceOf[MessagesApi].preferred(Seq(Lang("EN")))

  "fromLongRunningSubmission" must {
    "produce the correct page titles" in forAll(
      Table(
        "submission"         -> "expected title",
        BTN                  -> "Submitting your Below-Threshold Notification",
        ManageContactDetails -> "Submitting your contact details",
        ManageGroupDetails   -> "Submitting your group details",
        Registration         -> "Submitting your registration",
        Repayments           -> "Submitting your repayment request",
        RFM                  -> "Submitting..."
      )
    ) { (submission, expectedTitle) =>
      viewmodels.WaitingRoom.fromLongRunningSubmission.apply(submission).pageTitle mustBe expectedTitle
    }

    "produce the correct page h1" in forAll(
      Table(
        "submission"         -> "expected h1",
        BTN                  -> "Submitting your Below-Threshold Notification",
        ManageContactDetails -> "Submitting your contact details",
        ManageGroupDetails   -> "Submitting your group details",
        Registration         -> "Submitting your registration",
        Repayments           -> "Submitting your repayment request",
        RFM                  -> "Submitting..."
      )
    ) { (submission, expectedH1) =>
      viewmodels.WaitingRoom.fromLongRunningSubmission.apply(submission).h1Message mustBe expectedH1
    }

    "produce the correct page h2" in forAll(
      Table(
        "submission"         -> "expected h2",
        BTN                  -> "Don’t leave this page",
        ManageContactDetails -> "Do not press back in your browser or leave this page. It may take up to a minute to process this change.",
        ManageGroupDetails   -> "Do not press back in your browser or leave this page. It may take up to a minute to process this change.",
        Registration         -> "Do not leave this page.",
        Repayments           -> "Do not leave this page.",
        RFM                  -> "Do not leave this page."
      )
    ) { (submission, expectedH2) =>
      viewmodels.WaitingRoom.fromLongRunningSubmission.apply(submission).h2Message mustBe expectedH2
    }

    "produce the correct post-headings content" in forAll(
      Table(
        "submission"         -> "expected post-headings content",
        BTN                  -> Some("You will be redirected automatically when the submission is complete."),
        ManageContactDetails -> None,
        ManageGroupDetails   -> None,
        Registration         -> None,
        Repayments           -> None,
        RFM                  -> None
      )
    ) { (submission, expectedPostHeadingsContent) =>
      viewmodels.WaitingRoom.fromLongRunningSubmission.apply(submission).afterHeadingsContent mustBe expectedPostHeadingsContent
    }
  }

}
