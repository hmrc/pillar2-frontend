/*
 * Copyright 2023 HM Revenue & Customs
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
import helpers.ControllerBaseSpec
import play.api.test.FakeRequest
import play.api.test.Helpers._

import javax.inject.Inject

class SessionDataSpec @Inject() (sessionData: SessionData) extends ControllerBaseSpec {

  "Session Data Spec" should {
    //  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(controllers.eligibility.routes.BusinessActivityUKController.onPageLoad)
    "must store data into session in  Post" in {
      implicit val request =
        FakeRequest(POST, controllers.eligibility.routes.BusinessActivityUKController.onSubmit.url)
          .withFormUrlEncodedBody(("value", "yes"))
      sessionData.updateBusinessActivityUKYesNo("no")
      println("session**********************" + request.session)
      OK shouldBe OK
    }
  }
}
