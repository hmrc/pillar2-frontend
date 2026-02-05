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

/** Type-safe representation of all known transaction descriptions from the Account Activity API. Each value has its exact string value
  */
enum TransactionDescription(val value: String) {

  // Payment on Account
  case PaymentOnAccount extends TransactionDescription("On Account Pillar 2 (Payment on Account)")

  // UK Tax Return charges
  case UkTaxReturnDtt extends TransactionDescription("UKTR - DTT")
  case UkTaxReturnMttIir extends TransactionDescription("UKTR - MTT (IIR)")
  case UkTaxReturnMttUtpr extends TransactionDescription("UKTR - MTT (UTPR)")

  // UK Tax Return interest
  case UkTaxReturnInterestDtt extends TransactionDescription("Late UKTR pay int - DTT")
  case UkTaxReturnInterestMttIir extends TransactionDescription("Late UKTR pay int - MTT(IIR)")
  case UkTaxReturnInterestMttUtpr extends TransactionDescription("Late UKTR pay int - MTT(UTPR)")

  // Repayment interest
  case RepaymentInterest extends TransactionDescription("Repayment interest - UKTR")

  // Schedule 36 Penalty
  case Sch36Penalty extends TransactionDescription("Schedule 36 information notice")

  // UKTR DTT Late Filing Penalties
  case UktrDttLfp extends TransactionDescription("Late UKTR sub pen - DTT")
  case UktrDttLfp3Mth extends TransactionDescription("Late UKTR sub pen - DTT 3mnth")
  case UktrDttLfp6Mth extends TransactionDescription("Late UKTR sub pen - DTT 6mnth")
  case UktrDttLfp12Mth extends TransactionDescription("Late UKTR sub pen - DTT 12mnth")

  // UKTR MTT Late Filing Penalties
  case UktrMttLfp extends TransactionDescription("Late UKTR sub pen - MTT")
  case UktrMttLfp3Mth extends TransactionDescription("Late UKTR sub pen - MTT 3mnth")
  case UktrMttLfp6Mth extends TransactionDescription("Late UKTR sub pen - MTT 6mnth")
  case UktrMttLfp12Mth extends TransactionDescription("Late UKTR sub pen - MTT 12mnth")

  // ORN/GIR DTT Late Filing Penalties
  case OrnGirDttLfp extends TransactionDescription("Late ORN/GIR sub pen - DTT")
  case OrnGirDttLfp3Mth extends TransactionDescription("Late ORN/GIR sub pen -DTT3mnth")
  case OrnGirDttLfp6Mth extends TransactionDescription("Late ORN/GIR sub pen -DTT6mnth")

  // ORN/GIR MTT Late Filing Penalties
  case OrnGirMttLfp extends TransactionDescription("Late ORN/GIR sub pen - MTT")
  case OrnGirMttLfp3Mth extends TransactionDescription("Late ORN/GIR sub pen-MTT 3mnth")
  case OrnGirMttLfp6Mth extends TransactionDescription("Late ORN/GIR sub pen-MTT 6mnth")

  // Other Penalties
  case PotentialLostRevenuePenalty extends TransactionDescription("Schedule 24 inaccurate return")
  case RecordKeepingPenalty extends TransactionDescription("Accurate records failure pen")
  case GaarPenalty extends TransactionDescription("General Anti Abuse Rule pen")

  // Determinations
  case DeterminationDtt extends TransactionDescription("Determination - DTT")
  case DeterminationMttIir extends TransactionDescription("Determination - MTT (IIR)")
  case DeterminationMttUtpr extends TransactionDescription("Determination - MTT (UTPR)")

  // Determination Interest
  case DeterminationInterestDtt extends TransactionDescription("Determination interest - DTT")
  case DeterminationInterestMttIir extends TransactionDescription("Determination int- MTT (IIR)")
  case DeterminationInterestMttUtpr extends TransactionDescription("Determination int - MTT (UTPR)")

  // Discovery Assessments
  case DiscoveryAssessmentDtt extends TransactionDescription("Discovery Assessment - DTT")
  case DiscoveryAssessmentMttIir extends TransactionDescription("Discovery Assessment-MTT(IIR)")
  case DiscoveryAssessmentMttUtpr extends TransactionDescription("Discovery Assessment-MTT(UTPR)")

  // Discovery Assessment Interest
  case DiscoveryAssessmentInterestDtt extends TransactionDescription("Discovery Assessment int - DTT")
  case DiscoveryAssessmentInterestMttIir extends TransactionDescription("Discovery Assmnt int-MTT(IIR)")
  case DiscoveryAssessmentInterestMttUtpr extends TransactionDescription("Discovery Assmnt int-MTT(UTPR)")

  // Overpaid Claim Assessments
  case OverpaidClaimAssessmentDtt extends TransactionDescription("Overpaid claim assmnt - DTT")
  case OverpaidClaimAssessmentMttIir extends TransactionDescription("O/paid claim assmnt-MTT (IIR)")
  case OverpaidClaimAssessmentMttUtpr extends TransactionDescription("O/paid claim assmnt-MTT (UTPR)")

  // Overpaid Claim Assessment Interest
  case OverpaidClaimAssessmentInterestDtt extends TransactionDescription("O/paid claim assmnt int - DTT")
  case OverpaidClaimAssessmentInterestMttIir extends TransactionDescription("O/p claim assmnt int-MTT(IIR)")
  case OverpaidClaimAssessmentInterestMttUtpr extends TransactionDescription("O/p claim assmnt int-MTT(UTPR)")

  /** Returns the UI description for display in Outstanding Payments page */
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
    // Determination Interest
    case DeterminationInterestDtt     => "Determination interest - DTT"
    case DeterminationInterestMttIir  => "Determination interest - MTT (IIR)"
    case DeterminationInterestMttUtpr => "Determination interest - MTT (UTPR)"
    // Discovery Assessment
    case DiscoveryAssessmentDtt     => "Discovery Assessment - DTT"
    case DiscoveryAssessmentMttIir  => "Discovery Assessment - MTT (IIR)"
    case DiscoveryAssessmentMttUtpr => "Discovery Assessment - MTT (UTPR)"
    // Discovery Assessment Interest
    case DiscoveryAssessmentInterestDtt     => "Discovery Assessment interest - DTT"
    case DiscoveryAssessmentInterestMttIir  => "Discovery Assessment interest - MTT (IIR)"
    case DiscoveryAssessmentInterestMttUtpr => "Discovery Assessment interest - MTT (UTPR)"
    // Overpaid Claim Assessment
    case OverpaidClaimAssessmentDtt     => "Overpaid Claim Assessment - DTT"
    case OverpaidClaimAssessmentMttIir  => "Overpaid Claim Assessment - MTT (IIR)"
    case OverpaidClaimAssessmentMttUtpr => "Overpaid Claim Assessment - MTT (UTPR)"
    // Overpaid Claim Assessment Interest
    case OverpaidClaimAssessmentInterestDtt     => "Overpaid Claim Assessment interest - DTT"
    case OverpaidClaimAssessmentInterestMttIir  => "Overpaid Claim Assessment interest - MTT (IIR)"
    case OverpaidClaimAssessmentInterestMttUtpr => "Overpaid Claim Assessment interest - MTT (UTPR)"
    // UKTR DTT Late Filing Penalties
    case UktrDttLfp | UktrDttLfp3Mth | UktrDttLfp6Mth | UktrDttLfp12Mth => "Late UKTR submission penalty - DTT"
    // UKTR MTT Late Filing Penalties
    case UktrMttLfp | UktrMttLfp3Mth | UktrMttLfp6Mth | UktrMttLfp12Mth => "Late UKTR submission penalty - MTT"
    // ORN/GIR DTT Late Filing Penalties
    case OrnGirDttLfp | OrnGirDttLfp3Mth | OrnGirDttLfp6Mth => "Late ORN/GIR submission penalty - DTT"
    // ORN/GIR MTT Late Filing Penalties
    case OrnGirMttLfp | OrnGirMttLfp3Mth | OrnGirMttLfp6Mth => "Late ORN/GIR submission penalty - MTT"
    // Other Penalties
    case PotentialLostRevenuePenalty => "Potential lost revenue penalty"
    case Sch36Penalty                => "Schedule 36 information notice"
    case RecordKeepingPenalty        => "Failure to keep accurate records penalty"
    case GaarPenalty                 => "General Anti Abuse Rule penalty"
    // Payment on Account and Repayment Interest
    case PaymentOnAccount  => "Payment on Account"
    case RepaymentInterest => "Repayment interest"
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
