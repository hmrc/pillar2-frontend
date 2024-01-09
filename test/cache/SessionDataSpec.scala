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
import models.NormalMode
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.Pillar2SessionKeys

class SessionDataSpec extends SpecBase {
  val sessionData = new SessionData();
  "SessionDataSpec" when {
    "must store data into session for Post on BusinessActivityUK page" in {
      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        FakeRequest(POST, controllers.eligibility.routes.BusinessActivityUKController.onSubmit.url)
          .withFormUrlEncodedBody(("value", "no"))
          .withSession((Pillar2SessionKeys.businessActivityUKPageYesNo, "no"))
      sessionData.updateBusinessActivityUKYesNo("no")
      request.session.get(Pillar2SessionKeys.businessActivityUKPageYesNo) shouldEqual Some("no")
    }
    "must store data into session for Post on TurnOverEligibility page" in {
      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        FakeRequest(POST, controllers.eligibility.routes.TurnOverEligibilityController.onSubmit.url)
          .withFormUrlEncodedBody(("value", "no"))
          .withSession((Pillar2SessionKeys.turnOverEligibilityValue, "no"))
      sessionData.updateTurnOverEligibilitySessionData("no")
      request.session.get(Pillar2SessionKeys.turnOverEligibilityValue) shouldEqual Some("no")
    }
    "must store data into session for Post on GroupTerritories page" in {
      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        FakeRequest(POST, controllers.eligibility.routes.GroupTerritoriesController.onSubmit.url)
          .withFormUrlEncodedBody(("value", "no"))
          .withSession((Pillar2SessionKeys.groupTerritoriesPageYesNo, "no"))
      sessionData.updateGroupTerritoriesYesNo("no")
      request.session.get(Pillar2SessionKeys.groupTerritoriesPageYesNo) shouldEqual Some("no")
    }
    "must store data into session for Post on RegisteringNfmForThisGroup page" in {
      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        FakeRequest(POST, controllers.eligibility.routes.RegisteringNfmForThisGroupController.onSubmit.url)
          .withFormUrlEncodedBody(("value", "no"))
          .withSession((Pillar2SessionKeys.registeringNfmForThisGroup, "no"))
      sessionData.registeringNfmForThisGroup("no")
      request.session.get(Pillar2SessionKeys.registeringNfmForThisGroup) shouldEqual Some("no")
    }
    "must store data into session for Post on MneOrDomestic page" in {
      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] =
        FakeRequest(POST, controllers.subscription.routes.MneOrDomesticController.onSubmit(NormalMode).url)
          .withFormUrlEncodedBody(("value", "no"))
          .withSession((Pillar2SessionKeys.updateMneOrDomestic, "no"))
      sessionData.updateMneOrDomestic("no")
      request.session.get(Pillar2SessionKeys.updateMneOrDomestic) shouldEqual Some("no")
    }
  }
}
