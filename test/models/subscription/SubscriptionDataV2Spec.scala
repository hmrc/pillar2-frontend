/*
 * Copyright 2026 HM Revenue & Customs
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

package models.subscription

import base.SpecBase
import fixtures.SubscriptionFixtures
import play.api.libs.json.*

import java.time.LocalDate

class SubscriptionDataV2Spec extends SpecBase with SubscriptionFixtures {

  "SubscriptionDataV2" must {

    "successfully deserialise" when {

      "given valid V2 payload with one accounting period" in {
        val result = subscriptionDataV2Json.validate[SubscriptionDataV2]

        result mustBe a[JsSuccess[_]]

        val subscriptionDataV2 = result.get

        subscriptionDataV2.formBundleNumber mustBe "119000004323"
        subscriptionDataV2.upeDetails.organisationName mustBe "UK Only Organisation Ltd"
        subscriptionDataV2.upeDetails.domesticOnly mustBe true
        subscriptionDataV2.accountingPeriod mustBe a[Seq[AccountingPeriodV2]]
        subscriptionDataV2.accountingPeriod must have size 1
        subscriptionDataV2.accountingPeriod.head.startDate mustBe LocalDate.of(2024, 1, 6)
        subscriptionDataV2.accountingPeriod.head.endDate mustBe LocalDate.of(2025, 4, 6)
        subscriptionDataV2.accountingPeriod.head.dueDate mustBe Some(LocalDate.of(2025, 4, 6))
        subscriptionDataV2.accountingPeriod.head.canAmendStartDate mustBe true
        subscriptionDataV2.accountingPeriod.head.canAmendEndDate mustBe true
      }

      "given valid V2 payload with an empty accountingPeriod array" in {
        val emptyAccountingPeriodsArrayJson = subscriptionDataV2Json.as[JsObject] ++ Json.obj("accountingPeriod" -> Json.arr())
        val result                          = emptyAccountingPeriodsArrayJson.validate[SubscriptionDataV2]

        result mustBe a[JsSuccess[_]]
        result.get.accountingPeriod mustBe Seq.empty
      }

      "given valid V2 payload with accountingPeriod absent and defaulting to Seq.empty" in {
        val noPeriodsJson = subscriptionDataV2Json.as[JsObject] - "accountingPeriod"
        val result        = noPeriodsJson.validate[SubscriptionDataV2]

        result mustBe a[JsSuccess[_]]
        result.get.accountingPeriod mustBe Seq.empty
        result.get.formBundleNumber mustBe "119000004323"
      }

      "given valid V2 payload with multiple accounting periods" in {
        val testAccountingPeriod          = (subscriptionDataV2Json \ "accountingPeriod").as[JsArray].value.head
        val multipleAccountingPeriodsJson = subscriptionDataV2Json.as[JsObject] ++ Json.obj(
          "accountingPeriod" -> Json.arr(testAccountingPeriod, testAccountingPeriod, testAccountingPeriod)
        )
        val result = multipleAccountingPeriodsJson.validate[SubscriptionDataV2]

        result mustBe a[JsSuccess[_]]
        result.get.accountingPeriod must have size 3
      }

    }

    "fail to deserialise" when {
      "given V2 payload with missing mandatory fields" in {
        val jsonMissingMandatoryField = subscriptionDataV2Json.as[JsObject] - "formBundleNumber"
        val result                    = jsonMissingMandatoryField.validate[SubscriptionDataV2]

        result mustBe a[JsError]
      }

      "given V2 payload with JsObject accountingPeriod (V1 shape)" in {
        val v2JsonWithV1AccountingPeriod = misshapedSubscriptionDataV2Json.as[JsObject]
        val result                       = v2JsonWithV1AccountingPeriod.validate[SubscriptionDataV2]

        result mustBe a[JsError]
      }
    }

    "successfully serialise and deserialise (round-trip)" when {
      "given a valid model instance" in {
        val model: SubscriptionDataV2 = subscriptionDataV2Json.as[SubscriptionDataV2]
        Json.toJson(model).as[SubscriptionDataV2] mustBe model
      }

      "given a model with no accounting periods" in {
        val model = subscriptionDataV2Json
          .as[SubscriptionDataV2]
          .copy(accountingPeriod = Seq.empty)

        Json.toJson(model).as[SubscriptionDataV2] mustBe model
      }
    }
  }

  "SubscriptionDataV2.toSubscriptionData" must {
    "return Some(SubscriptionData)" when {
      "given a V2 model with one accounting period" in {
        val v2: SubscriptionDataV2 = subscriptionDataV2Json.as[SubscriptionDataV2]
        val v1: SubscriptionData   = v2.toSubscriptionData.value

        v1.formBundleNumber mustBe v2.formBundleNumber
        v1.upeDetails mustBe v2.upeDetails
        v1.upeCorrespAddressDetails mustBe v2.upeCorrespAddressDetails
        v1.primaryContactDetails mustBe v2.primaryContactDetails
        v1.secondaryContactDetails mustBe v2.secondaryContactDetails
        v1.filingMemberDetails mustBe v2.filingMemberDetails
        v1.accountStatus mustBe v2.accountStatus
        v1.accountingPeriod mustBe v2.accountingPeriod.head.toAccountingPeriod
        v1.accountingPeriod.startDate mustBe LocalDate.of(2024, 1, 6)
        v1.accountingPeriod.endDate mustBe LocalDate.of(2025, 4, 6)
        v1.accountingPeriod.dueDate mustBe Some(LocalDate.of(2025, 4, 6))
      }

      "given a V2 model with multiple accounting periods (V1 uses only the first one)" in {
        val firstAccountingPeriod: AccountingPeriodV2 = AccountingPeriodV2(
          startDate = LocalDate.of(2024, 1, 1),
          endDate = LocalDate.of(2024, 12, 31),
          dueDate = Some(LocalDate.of(2025, 1, 31)),
          canAmendStartDate = true,
          canAmendEndDate = true
        )

        val secondAccountingPeriod = firstAccountingPeriod.copy(
          startDate = LocalDate.of(2025, 1, 1)
        )

        val v2: SubscriptionDataV2 = subscriptionDataV2Json
          .as[SubscriptionDataV2]
          .copy(accountingPeriod = Seq(firstAccountingPeriod, secondAccountingPeriod))

        val v1: SubscriptionData = v2.toSubscriptionData.value

        v1.accountingPeriod mustBe v2.accountingPeriod.head.toAccountingPeriod
      }
    }

    "return None" when {
      "given a V2 model with an empty accountingPeriod" in {
        val v2: SubscriptionDataV2 = subscriptionDataV2Json.as[SubscriptionDataV2].copy(accountingPeriod = Seq.empty)
        v2.toSubscriptionData mustBe None
      }
    }
  }
}

/*

  "SubscriptionSuccessV2" must {

    "SubscriptionSuccessV2 wrap SubscriptionDataV2" in {
      val wrapped = Json.obj("success" -> subscriptionDataV2Json)
      val result  = wrapped.as[SubscriptionSuccessV2]
      result.success.formBundleNumber mustBe "119000004323"
    }

  }
 */
