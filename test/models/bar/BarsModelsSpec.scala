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

package models.bars

import base.SpecBase
import play.api.libs.json.{JsString, Json}

class BarsModelsSpec extends SpecBase {

  "AccountNumberIsWellFormatted" should {
    "correctly parse 'yes' to Yes" in {
      JsString("yes").validate[AccountNumberIsWellFormatted].asOpt.value mustEqual AccountNumberIsWellFormatted.Yes
    }

    "correctly parse 'no' to No" in {
      JsString("no").validate[AccountNumberIsWellFormatted].asOpt.value mustEqual AccountNumberIsWellFormatted.No
    }

    "correctly parse 'indeterminate' to Indeterminate" in {
      JsString("indeterminate").validate[AccountNumberIsWellFormatted].asOpt.value mustEqual AccountNumberIsWellFormatted.Indeterminate
    }

    "correctly write Yes as 'yes'" in {
      // Test as the trait type instead of the singleton type
      val value: AccountNumberIsWellFormatted = AccountNumberIsWellFormatted.Yes
      Json.toJson(value) mustEqual JsString("yes")
    }

    "correctly write No as 'no'" in {
      val value: AccountNumberIsWellFormatted = AccountNumberIsWellFormatted.No
      Json.toJson(value) mustEqual JsString("no")
    }

    "correctly write Indeterminate as 'indeterminate'" in {
      val value: AccountNumberIsWellFormatted = AccountNumberIsWellFormatted.Indeterminate
      Json.toJson(value) mustEqual JsString("indeterminate")
    }

  }

  "SortCodeIsPresentOnEISCD" should {
    "correctly parse 'yes' to Yes" in {
      JsString("yes").validate[SortCodeIsPresentOnEISCD].asOpt.value mustEqual SortCodeIsPresentOnEISCD.Yes
    }

    "correctly parse 'no' to No" in {
      JsString("no").validate[SortCodeIsPresentOnEISCD].asOpt.value mustEqual SortCodeIsPresentOnEISCD.No
    }

    "correctly parse 'error' to Error" in {
      JsString("error").validate[SortCodeIsPresentOnEISCD].asOpt.value mustEqual SortCodeIsPresentOnEISCD.Error
    }

    "correctly write Yes as 'yes'" in {
      val value: SortCodeIsPresentOnEISCD = SortCodeIsPresentOnEISCD.Yes
      Json.toJson(value) mustEqual JsString("yes")
    }

    "correctly write No as 'no'" in {
      val value: SortCodeIsPresentOnEISCD = SortCodeIsPresentOnEISCD.No
      Json.toJson(value) mustEqual JsString("no")
    }

    "correctly write Error as 'error'" in {
      val value: SortCodeIsPresentOnEISCD = SortCodeIsPresentOnEISCD.Error
      Json.toJson(value) mustEqual JsString("error")
    }

  }

  "NonStandardAccountDetailsRequiredForBacs" should {
    "correctly parse 'yes' to Yes" in {
      JsString("yes").validate[NonStandardAccountDetailsRequiredForBacs].asOpt.value mustEqual NonStandardAccountDetailsRequiredForBacs.Yes
    }

    "correctly parse 'no' to No" in {
      JsString("no").validate[NonStandardAccountDetailsRequiredForBacs].asOpt.value mustEqual NonStandardAccountDetailsRequiredForBacs.No
    }

    "correctly parse 'inapplicable' to Inapplicable" in {
      JsString("inapplicable")
        .validate[NonStandardAccountDetailsRequiredForBacs]
        .asOpt
        .value mustEqual NonStandardAccountDetailsRequiredForBacs.Inapplicable
    }

    "correctly write Yes as 'yes'" in {
      val value: NonStandardAccountDetailsRequiredForBacs = NonStandardAccountDetailsRequiredForBacs.Yes
      Json.toJson(value) mustEqual JsString("yes")
    }

    "correctly write No as 'no'" in {
      val value: NonStandardAccountDetailsRequiredForBacs = NonStandardAccountDetailsRequiredForBacs.No
      Json.toJson(value) mustEqual JsString("no")
    }

    "correctly write Inapplicable as 'inapplicable'" in {
      val value: NonStandardAccountDetailsRequiredForBacs = NonStandardAccountDetailsRequiredForBacs.Inapplicable
      Json.toJson(value) mustEqual JsString("inapplicable")
    }

  }

  "AccountExists" should {
    "correctly parse 'yes' to Yes" in {
      JsString("yes").validate[AccountExists].asOpt.value mustEqual AccountExists.Yes
    }

    "correctly parse 'no' to No" in {
      JsString("no").validate[AccountExists].asOpt.value mustEqual AccountExists.No
    }

    "correctly parse 'inapplicable' to Inapplicable" in {
      JsString("inapplicable").validate[AccountExists].asOpt.value mustEqual AccountExists.Inapplicable
    }

    "correctly parse 'indeterminate' to Indeterminate" in {
      JsString("indeterminate").validate[AccountExists].asOpt.value mustEqual AccountExists.Indeterminate
    }

    "correctly parse 'error' to Error" in {
      JsString("error").validate[AccountExists].asOpt.value mustEqual AccountExists.Error
    }

    "correctly write Yes as 'yes'" in {
      val value: AccountExists = AccountExists.Yes
      Json.toJson(value) mustEqual JsString("yes")
    }

    "correctly write No as 'no'" in {
      val value: AccountExists = AccountExists.No
      Json.toJson(value) mustEqual JsString("no")
    }

    "correctly write Inapplicable as 'inapplicable'" in {
      val value: AccountExists = AccountExists.Inapplicable
      Json.toJson(value) mustEqual JsString("inapplicable")
    }

    "correctly write Indeterminate as 'indeterminate'" in {
      val value: AccountExists = AccountExists.Indeterminate
      Json.toJson(value) mustEqual JsString("indeterminate")
    }

    "correctly write Error as 'error'" in {
      val value: AccountExists = AccountExists.Error
      Json.toJson(value) mustEqual JsString("error")
    }

  }

  "NameMatches" should {
    "correctly parse 'yes' to Yes" in {
      JsString("yes").validate[NameMatches].asOpt.value mustEqual NameMatches.Yes
    }

    "correctly parse 'partial' to Partial" in {
      JsString("partial").validate[NameMatches].asOpt.value mustEqual NameMatches.Partial
    }

    "correctly parse 'no' to No" in {
      JsString("no").validate[NameMatches].asOpt.value mustEqual NameMatches.No
    }

    "correctly parse 'inapplicable' to Inapplicable" in {
      JsString("inapplicable").validate[NameMatches].asOpt.value mustEqual NameMatches.Inapplicable
    }

    "correctly parse 'indeterminate' to Indeterminate" in {
      JsString("indeterminate").validate[NameMatches].asOpt.value mustEqual NameMatches.Indeterminate
    }

    "correctly parse 'error' to Error" in {
      JsString("error").validate[NameMatches].asOpt.value mustEqual NameMatches.Error
    }

    "correctly write Yes as 'yes'" in {
      val value: NameMatches = NameMatches.Yes
      Json.toJson(value) mustEqual JsString("yes")
    }

    "correctly write Partial as 'partial'" in {
      val value: NameMatches = NameMatches.Partial
      Json.toJson(value) mustEqual JsString("partial")
    }

    "correctly write No as 'no'" in {
      val value: NameMatches = NameMatches.No
      Json.toJson(value) mustEqual JsString("no")
    }

    "correctly write Inapplicable as 'inapplicable'" in {
      val value: NameMatches = NameMatches.Inapplicable
      Json.toJson(value) mustEqual JsString("inapplicable")
    }

    "correctly write Indeterminate as 'indeterminate'" in {
      val value: NameMatches = NameMatches.Indeterminate
      Json.toJson(value) mustEqual JsString("indeterminate")
    }

    "correctly write Error as 'error'" in {
      val value: NameMatches = NameMatches.Error
      Json.toJson(value) mustEqual JsString("error")
    }

  }

  "SortCodeSupportsDirectDebit" should {
    "correctly parse 'yes' to Yes" in {
      JsString("yes").validate[SortCodeSupportsDirectDebit].asOpt.value mustEqual SortCodeSupportsDirectDebit.Yes
    }

    "correctly parse 'no' to No" in {
      JsString("no").validate[SortCodeSupportsDirectDebit].asOpt.value mustEqual SortCodeSupportsDirectDebit.No
    }

    "correctly parse 'error' to Error" in {
      JsString("error").validate[SortCodeSupportsDirectDebit].asOpt.value mustEqual SortCodeSupportsDirectDebit.Error
    }

    "correctly write Yes as 'yes'" in {
      val value: SortCodeSupportsDirectDebit = SortCodeSupportsDirectDebit.Yes
      Json.toJson(value) mustEqual JsString("yes")
    }

    "correctly write No as 'no'" in {
      val value: SortCodeSupportsDirectDebit = SortCodeSupportsDirectDebit.No
      Json.toJson(value) mustEqual JsString("no")
    }

    "correctly write Error as 'error'" in {
      val value: SortCodeSupportsDirectDebit = SortCodeSupportsDirectDebit.Error
      Json.toJson(value) mustEqual JsString("error")
    }

  }

  "SortCodeSupportsDirectCredit" should {
    "correctly parse 'yes' to Yes" in {
      JsString("yes").validate[SortCodeSupportsDirectCredit].asOpt.value mustEqual SortCodeSupportsDirectCredit.Yes
    }

    "correctly parse 'no' to No" in {
      JsString("no").validate[SortCodeSupportsDirectCredit].asOpt.value mustEqual SortCodeSupportsDirectCredit.No
    }

    "correctly parse 'error' to Error" in {
      JsString("error").validate[SortCodeSupportsDirectCredit].asOpt.value mustEqual SortCodeSupportsDirectCredit.Error
    }

    "correctly write Yes as 'yes'" in {
      val value: SortCodeSupportsDirectCredit = SortCodeSupportsDirectCredit.Yes
      Json.toJson(value) mustEqual JsString("yes")
    }

    "correctly write No as 'no'" in {
      val value: SortCodeSupportsDirectCredit = SortCodeSupportsDirectCredit.No
      Json.toJson(value) mustEqual JsString("no")
    }

    "correctly write Error as 'error'" in {
      val value: SortCodeSupportsDirectCredit = SortCodeSupportsDirectCredit.Error
      Json.toJson(value) mustEqual JsString("error")
    }

  }
}
