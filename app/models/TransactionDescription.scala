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

package models

import play.api.libs.json.*

/** Type-safe representation of all known transaction descriptions from the Account Activity API. Each value has its exact string value for matching.
  */
enum TransactionDescription(val value: String) {

  // Payment on Account
  case PaymentOnAccount extends TransactionDescription("On Account Pillar 2 (Payment on Account)")

  // UK Tax Return charges
  case UkTaxReturnDtt extends TransactionDescription("Pillar 2 UK Tax Return Pillar 2 DTT")
  case UkTaxReturnMttIir extends TransactionDescription("Pillar 2 UK Tax Return Pillar 2 MTT IIR")
  case UkTaxReturnMttUtpr extends TransactionDescription("Pillar 2 UK Tax Return Pillar 2 MTT UTPR")

  // UK Tax Return interest
  case UkTaxReturnInterestDtt extends TransactionDescription("Pillar 2 UKTR Interest Pillar 2 DTT Int")
  case UkTaxReturnInterestMttIir extends TransactionDescription("Pillar 2 UKTR Interest Pillar 2 MTT IIR Int")
  case UkTaxReturnInterestMttUtpr extends TransactionDescription("Pillar 2 UKTR Interest Pillar 2 MTT UTPR Int")

  // Repayment interest
  case RepaymentInterest extends TransactionDescription("Pillar 2 UKTR RPI Pillar 2 OECD RPI")

  // Schedule 36 Penalty
  case Sch36Penalty extends TransactionDescription("Sch 36 Penalty TG PEN")

  // UKTR DTT Late Filing Penalties
  case UktrDttLfp extends TransactionDescription("Pillar 2 UKTR DTT LFP AUTO PEN")
  case UktrDttLfp3Mth extends TransactionDescription("Pillar 2 UKTR DTT 3 Mth LFP AUTO PEN")
  case UktrDttLfp6Mth extends TransactionDescription("Pillar 2 UKTR DTT 6 Mth LFP AUTO PEN")
  case UktrDttLfp12Mth extends TransactionDescription("Pillar 2 UKTR DTT 12 Mth LFP AUTO PEN")

  // UKTR MTT Late Filing Penalties
  case UktrMttLfp extends TransactionDescription("Pillar 2 UKTR MTT LFP AUTO PEN")
  case UktrMttLfp3Mth extends TransactionDescription("Pillar 2 UKTR MTT 3 Mth LFP AUTO PEN")
  case UktrMttLfp6Mth extends TransactionDescription("Pillar 2 UKTR MTT 6 Mth LFP AUTO PEN")
  case UktrMttLfp12Mth extends TransactionDescription("Pillar 2 UKTR MTT 12 Mth LFP AUTO PEN")

  // ORN/GIR DTT Late Filing Penalties
  case OrnGirDttLfp extends TransactionDescription("Pillar 2 ORN/GIR DTT LFP AUTO PEN")
  case OrnGirDttLfp3Mth extends TransactionDescription("Pillar 2 ORN/GIR DTT 3 Mth LFP AUTO PEN")
  case OrnGirDttLfp6Mth extends TransactionDescription("Pillar 2 ORN/GIR DTT 6 Mth LFP AUTO PEN")

  // ORN/GIR MTT Late Filing Penalties
  case OrnGirMttLfp extends TransactionDescription("Pillar 2 ORN/GIR MTT LFP AUTO PEN")
  case OrnGirMttLfp3Mth extends TransactionDescription("Pillar 2 ORN/GIR MTT 3 Mth LFP AUTO PEN")
  case OrnGirMttLfp6Mth extends TransactionDescription("Pillar 2 ORN/GIR MTT 6 Mth LFP AUTO PEN")

  // Other Penalties
  case PotentialLostRevenuePenalty extends TransactionDescription("Pillar 2 Poten Lost Rev Pen TG PEN")
  case RecordKeepingPenalty extends TransactionDescription("Pillar 2 Record Keeping Pen TG PEN")

  // Determinations
  case DeterminationDtt extends TransactionDescription("Pillar 2 Determination Pillar 2 DTT")
  case DeterminationMttIir extends TransactionDescription("Pillar 2 Determination Pillar 2 MTT IIR")
  case DeterminationMttUtpr extends TransactionDescription("Pillar 2 Determination Pillar 2 MTT UTPR")

  // Determination Interest
  case DeterminationInterestDtt extends TransactionDescription("Pillar 2 Determination Int Pillar 2 DTT Int")
  case DeterminationInterestMttIir extends TransactionDescription("Pillar 2 Determination Int Pillar 2 MTT IIR Int")
  case DeterminationInterestMttUtpr extends TransactionDescription("Pillar 2 Determination Int Pillar 2 MTT UTPR Int")

  // Discovery Assessments
  case DiscoveryAssessmentDtt extends TransactionDescription("Pillar 2 Discovery Assessment Pillar 2 DTT")
  case DiscoveryAssessmentMttIir extends TransactionDescription("Pillar 2 Discovery Assessment Pillar 2 MTT IIR")
  case DiscoveryAssessmentMttUtpr extends TransactionDescription("Pillar 2 Discovery Assessment Pillar 2 MTT UTPR")

  // Discovery Assessment Interest
  case DiscoveryAssessmentInterestDtt extends TransactionDescription("Pillar 2 Discovery Assmt Int Pillar 2 DTT Int")
  case DiscoveryAssessmentInterestMttIir extends TransactionDescription("Pillar 2 Discovery Assmt Int Pillar 2 MTT IIR Int")
  case DiscoveryAssessmentInterestMttUtpr extends TransactionDescription("Pillar 2 Discovery Assmt Int Pillar 2 MTT UTPR Int")

  // Overpaid Claim Assessments
  case OverpaidClaimAssessmentDtt extends TransactionDescription("Pillar 2 Overpaid Claim Assmt Pillar 2 DTT")
  case OverpaidClaimAssessmentMttIir extends TransactionDescription("Pillar 2 Overpaid Claim Assmt Pillar 2 MTT IIR")
  case OverpaidClaimAssessmentMttUtpr extends TransactionDescription("Pillar 2 Overpaid Claim Assmt Pillar 2 MTT UTPR")

  // Overpaid Claim Assessment Interest
  case OverpaidClaimAssessmentInterestDtt extends TransactionDescription("Pillar 2 Opaid Claim Assmt Int Pillar 2 DTT Int")
  case OverpaidClaimAssessmentInterestMttIir extends TransactionDescription("Pillar 2 Opaid Claim Assmt Int Pillar 2 MTT IIR Int")
  case OverpaidClaimAssessmentInterestMttUtpr extends TransactionDescription("Pillar 2 Opaid Claim Assmt Int Pillar 2 MTT UTPR Int")

  /** Returns the Column G UI description for display in Outstanding Payments page */
  def toUiDescription: String = this match {
    // UKTR Charges
    case UkTaxReturnDtt     => "UKTR - DTT"
    case UkTaxReturnMttIir  => "UKTR - MTT (IIR)"
    case UkTaxReturnMttUtpr => "UKTR - MTT (UTPR)"
    // UKTR Interest
    case UkTaxReturnInterestDtt     => "Late UKTR payment interest - DTT"
    case UkTaxReturnInterestMttIir  => "Late UKTR payment interest - MTT (IIR)"
    case UkTaxReturnInterestMttUtpr => "Late UKTR payment interest - MTT (UTPR)"
    // Determination
    case DeterminationDtt     => "Determination - DTT"
    case DeterminationMttIir  => "Determination - MTT (IIR)"
    case DeterminationMttUtpr => "Determination - MTT (UTPR)"
    // Discovery Assessment
    case DiscoveryAssessmentDtt     => "Discovery Assessment - DTT"
    case DiscoveryAssessmentMttIir  => "Discovery Assessment - MTT (IIR)"
    case DiscoveryAssessmentMttUtpr => "Discovery Assessment - MTT (UTPR)"
    // Overpaid Claim Assessment
    case OverpaidClaimAssessmentDtt     => "Overpaid Claim Assessment - DTT"
    case OverpaidClaimAssessmentMttIir  => "Overpaid Claim Assessment - MTT (IIR)"
    case OverpaidClaimAssessmentMttUtpr => "Overpaid Claim Assessment - MTT (UTPR)"
    // UKTR DTT Late Filing Penalties
    case UktrDttLfp | UktrDttLfp3Mth | UktrDttLfp6Mth | UktrDttLfp12Mth => "Late UKTR submission penalty - DTT"
    // UKTR MTT Late Filing Penalties
    case UktrMttLfp | UktrMttLfp3Mth | UktrMttLfp6Mth | UktrMttLfp12Mth => "Late UKTR submission penalty - MTT"
    // ORN/GIR DTT Late Filing Penalties
    case OrnGirDttLfp | OrnGirDttLfp3Mth | OrnGirDttLfp6Mth => "Late ORN/GIR submission penalty - DTT"
    // ORN/GIR MTT Late Filing Penalties
    case OrnGirMttLfp | OrnGirMttLfp3Mth | OrnGirMttLfp6Mth => "Late ORN/GIR submission penalty - MTT"
    // Other Penalties
    case PotentialLostRevenuePenalty => "Potential lost revenue penalty - DTT"
    case Sch36Penalty                => "Schedule 36 information notice"
    case RecordKeepingPenalty        => "Failure to keep accurate records penalty"
    // Payment on Account and other unmapped types - return original value as fallback
    case _ => value
  }
}

object TransactionDescription {

  /** Parse a string to TransactionDescription, returning None for unknown values */
  def fromString(value: String): Option[TransactionDescription] =
    TransactionDescription.values.find(_.value == value)

  /** Helper to check if a transaction description string matches a known type */
  def matches(desc: String, expected: TransactionDescription): Boolean =
    fromString(desc).contains(expected)

  given reads: Reads[TransactionDescription] = Reads { json =>
    json.validate[String].flatMap { value =>
      fromString(value) match {
        case Some(desc) => JsSuccess(desc)
        case None       => JsError(s"Unknown transaction description: $value")
      }
    }
  }

  given writes: Writes[TransactionDescription] = Writes { desc =>
    JsString(desc.value)
  }
}
