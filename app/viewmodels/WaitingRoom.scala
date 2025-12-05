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

import models.longrunningsubmissions.LongRunningSubmission
import play.api.i18n.Messages

case class WaitingRoom(
  pageTitle:            String,
  h1Message:            String,
  h2Message:            String,
  afterHeadingsContent: Option[String]
)

object WaitingRoom {
  def fromLongRunningSubmission(using messages: Messages): LongRunningSubmission => WaitingRoom = {
    case LongRunningSubmission.BTN =>
      WaitingRoom(
        pageTitle = messages("btn.waitingRoom.title"),
        h1Message = messages("btn.waitingRoom.submitting"),
        h2Message = messages("btn.waitingRoom.dontLeave"),
        afterHeadingsContent = Some(messages("btn.waitingRoom.redirect"))
      )
    case LongRunningSubmission.ManageContactDetails =>
      WaitingRoom(
        pageTitle = messages("manageContactDetails.title"),
        h1Message = messages("manageContactDetails.h1"),
        h2Message = messages("manageContactDetails.h2"),
        afterHeadingsContent = None
      )
    case LongRunningSubmission.ManageGroupDetails =>
      WaitingRoom(
        pageTitle = messages("manageGroupDetails.title"),
        h1Message = messages("manageGroupDetails.h1"),
        h2Message = messages("manageGroupDetails.h2"),
        afterHeadingsContent = None
      )
    case LongRunningSubmission.Registration =>
      WaitingRoom(
        pageTitle = messages("registrationWaitingRoom.title"),
        h1Message = messages("registrationWaitingRoom.h1"),
        h2Message = messages("registrationWaitingRoom.h2"),
        afterHeadingsContent = None
      )
    case LongRunningSubmission.Repayments =>
      WaitingRoom(
        pageTitle = messages("repaymentsWaitingRoom.title"),
        h1Message = messages("repaymentsWaitingRoom.h1"),
        h2Message = messages("repaymentsWaitingRoom.h2"),
        afterHeadingsContent = None
      )
    case LongRunningSubmission.RFM =>
      WaitingRoom(
        pageTitle = messages("rfmWaitingRoom.title"),
        h1Message = messages("rfmWaitingRoom.h1"),
        h2Message = messages("rfmWaitingRoom.h2"),
        afterHeadingsContent = None
      )
  }
}
