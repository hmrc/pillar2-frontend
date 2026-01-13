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

import base.SpecBase

class TransactionDescriptionSpec extends SpecBase {

  "TransactionDescription.toUiDescription" should {
    "return correct UI description for UKTR charges" in {
      TransactionDescription.UkTaxReturnDtt.toUiDescription mustBe "UKTR - DTT"
      TransactionDescription.UkTaxReturnMttIir.toUiDescription mustBe "UKTR - MTT (IIR)"
      TransactionDescription.UkTaxReturnMttUtpr.toUiDescription mustBe "UKTR - MTT (UTPR)"
    }

    "return correct UI description for UKTR interest" in {
      TransactionDescription.UkTaxReturnInterestDtt.toUiDescription mustBe "Late UKTR payment interest - DTT"
      TransactionDescription.UkTaxReturnInterestMttIir.toUiDescription mustBe "Late UKTR payment interest - MTT (IIR)"
      TransactionDescription.UkTaxReturnInterestMttUtpr.toUiDescription mustBe "Late UKTR payment interest - MTT (UTPR)"
    }

    "return correct UI description for Determination" in {
      TransactionDescription.DeterminationDtt.toUiDescription mustBe "Determination - DTT"
      TransactionDescription.DeterminationMttIir.toUiDescription mustBe "Determination - MTT (IIR)"
      TransactionDescription.DeterminationMttUtpr.toUiDescription mustBe "Determination - MTT (UTPR)"
    }

    "return correct UI description for Discovery Assessment" in {
      TransactionDescription.DiscoveryAssessmentDtt.toUiDescription mustBe "Discovery Assessment - DTT"
      TransactionDescription.DiscoveryAssessmentMttIir.toUiDescription mustBe "Discovery Assessment - MTT (IIR)"
      TransactionDescription.DiscoveryAssessmentMttUtpr.toUiDescription mustBe "Discovery Assessment - MTT (UTPR)"
    }

    "return correct UI description for Overpaid Claim Assessment" in {
      TransactionDescription.OverpaidClaimAssessmentDtt.toUiDescription mustBe "Overpaid Claim Assessment - DTT"
      TransactionDescription.OverpaidClaimAssessmentMttIir.toUiDescription mustBe "Overpaid Claim Assessment - MTT (IIR)"
      TransactionDescription.OverpaidClaimAssessmentMttUtpr.toUiDescription mustBe "Overpaid Claim Assessment - MTT (UTPR)"
    }

    "return correct UI description for UKTR DTT Late Filing Penalties" in {
      TransactionDescription.UktrDttLfp.toUiDescription mustBe "Late UKTR submission penalty - DTT"
      TransactionDescription.UktrDttLfp3Mth.toUiDescription mustBe "Late UKTR submission penalty - DTT"
      TransactionDescription.UktrDttLfp6Mth.toUiDescription mustBe "Late UKTR submission penalty - DTT"
      TransactionDescription.UktrDttLfp12Mth.toUiDescription mustBe "Late UKTR submission penalty - DTT"
    }

    "return correct UI description for UKTR MTT Late Filing Penalties" in {
      TransactionDescription.UktrMttLfp.toUiDescription mustBe "Late UKTR submission penalty - MTT"
      TransactionDescription.UktrMttLfp3Mth.toUiDescription mustBe "Late UKTR submission penalty - MTT"
      TransactionDescription.UktrMttLfp6Mth.toUiDescription mustBe "Late UKTR submission penalty - MTT"
      TransactionDescription.UktrMttLfp12Mth.toUiDescription mustBe "Late UKTR submission penalty - MTT"
    }

    "return correct UI description for ORN/GIR DTT Late Filing Penalties" in {
      TransactionDescription.OrnGirDttLfp.toUiDescription mustBe "Late ORN/GIR submission penalty - DTT"
      TransactionDescription.OrnGirDttLfp3Mth.toUiDescription mustBe "Late ORN/GIR submission penalty - DTT"
      TransactionDescription.OrnGirDttLfp6Mth.toUiDescription mustBe "Late ORN/GIR submission penalty - DTT"
    }

    "return correct UI description for ORN/GIR MTT Late Filing Penalties" in {
      TransactionDescription.OrnGirMttLfp.toUiDescription mustBe "Late ORN/GIR submission penalty - MTT"
      TransactionDescription.OrnGirMttLfp3Mth.toUiDescription mustBe "Late ORN/GIR submission penalty - MTT"
      TransactionDescription.OrnGirMttLfp6Mth.toUiDescription mustBe "Late ORN/GIR submission penalty - MTT"
    }

    "return correct UI description for other penalties" in {
      TransactionDescription.PotentialLostRevenuePenalty.toUiDescription mustBe "Potential lost revenue penalty"
      TransactionDescription.Sch36Penalty.toUiDescription mustBe "Schedule 36 information notice"
      TransactionDescription.RecordKeepingPenalty.toUiDescription mustBe "Failure to keep accurate records penalty"
    }

    "return original value for unmapped descriptions" in {
      TransactionDescription.PaymentOnAccount.toUiDescription mustBe "On Account Pillar 2 (Payment on Account)"
      TransactionDescription.RepaymentInterest.toUiDescription mustBe "Pillar 2 UKTR RPI Pillar 2 OECD RPI"
    }
  }
}
