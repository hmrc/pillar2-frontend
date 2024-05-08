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

  def rfmContactDetailStatus: Boolean =
    (
      get(RfmPrimaryContactNamePage).isDefined,
      get(RfmPrimaryContactEmailPage).isDefined,
      get(RfmContactByTelephonePage),
      get(RfmCapturePrimaryTelephonePage).isDefined,
      get(RfmAddSecondaryContactPage),
      get(RfmSecondaryContactNamePage).isDefined,
      get(RfmSecondaryEmailPage).isDefined,
      get(RfmSecondaryPhonePreferencePage),
      get(RfmSecondaryCapturePhonePage).isDefined,
      get(RfmContactAddressPage).isDefined
    ) match {
      case (true, true, Some(true), true, Some(true), true, true, Some(true), true, true)     => true
      case (true, true, Some(true), true, Some(true), true, true, Some(false), false, true)   => true
      case (true, true, Some(true), true, Some(false), false, false, None, false, true)       => true
      case (true, true, Some(false), false, Some(true), true, true, Some(true), true, true)   => true
      case (true, true, Some(false), false, Some(true), true, true, Some(false), false, true) => true
      case (true, true, Some(false), false, Some(false), false, false, None, false, true)     => true
      case _                                                                                  => false
    }

}
