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

package helpers

import models.{EnrolmentInfo, UserAnswers}
import pages._
import utils.RowStatus

trait ReplaceFilingMemberHelpers {

  self: UserAnswers =>

  def rfmContactDetailStatus: Boolean = {
    val p1 = get(RfmPrimaryContactNamePage).isDefined
    val p2 = get(RfmPrimaryContactEmailPage).isDefined
    val p3 = get(RfmContactByTelephonePage).getOrElse(false)
    val p4 = get(RfmCapturePrimaryTelephonePage).isDefined
    val s1 = get(RfmAddSecondaryContactPage).getOrElse(false)
    val s2 = get(RfmSecondaryContactNamePage).isDefined
    val s3 = get(RfmSecondaryEmailPage).isDefined
    val s4 = get(RfmSecondaryPhonePreferencePage).getOrElse(false)
    val s5 = get(RfmSecondaryCapturePhonePage).isDefined
    val a1 = get(RfmContactAddressPage).isDefined
    (p1, p2, p3, p4, s1, s2, s3, s4, s5, a1) match {
      case (true, true, true, true, true, true, true, true, true, true)      => true
      case (true, true, false, false, true, true, true, true, true, true)    => true
      case (true, true, false, false, true, true, true, false, false, true)  => true
      case (true, true, true, true, false, false, false, false, false, true) => true
      case _                                                                 => false
    }
  }

}
