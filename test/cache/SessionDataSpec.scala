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

package cache
import base.SpecBase
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.Pillar2SessionKeys

class SessionDataSpec extends SpecBase {
  val sessionData = new SessionData();
  "SessionDataSpec" when {
    implicit val requestBusinessActivityUK: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(controllers.eligibility.routes.BusinessActivityUKController.onPageLoad)
    "must store data into session in Post" in {
      implicit val updatedRequest =
        FakeRequest(POST, controllers.eligibility.routes.BusinessActivityUKController.onSubmit.url)
          .withFormUrlEncodedBody(("value", "no"))
          .withSession((Pillar2SessionKeys.businessActivityUKPageYesNo, "no"))
      sessionData.updateBusinessActivityUKYesNo("no")
      updatedRequest.session.get(Pillar2SessionKeys.businessActivityUKPageYesNo) shouldEqual Some("no")
    }

    implicit val requestMneOrDomestic: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(controllers.eligibility.routes.MneOrDomesticController.onPageLoad)
    "must store MNE or Domestic value into session in Post" in {
      implicit val updatedRequest =
        FakeRequest(POST, controllers.eligibility.routes.MneOrDomesticController.onSubmit.url)
          .withFormUrlEncodedBody(("value", "Domestic Top-up Tax"))
          .withSession((Pillar2SessionKeys.updateMneOrDomestic, "Domestic Top-up Tax"))
      sessionData.updateMneOrDomestic("Domestic Top-up Tax")
      updatedRequest.session.get(Pillar2SessionKeys.updateMneOrDomestic) shouldEqual Some("Domestic Top-up Tax")
    }
  }
}
