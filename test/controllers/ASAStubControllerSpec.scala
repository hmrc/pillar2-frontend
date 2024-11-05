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

package controllers

import base.SpecBase
import controllers.actions.TestAuthRetrievals.Ops
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import views.html.ASAStubView

import java.util.UUID
import scala.concurrent.Future

class ASAStubControllerSpec extends SpecBase {
  val agentEnrolments: Enrolments = Enrolments(
    Set(Enrolment("HMRC-AS-AGENT", List(EnrolmentIdentifier("AgentReference", "1234")), "Activated", None))
  )
  val id:           String = UUID.randomUUID().toString
  val providerId:   String = UUID.randomUUID().toString
  val providerType: String = UUID.randomUUID().toString
  type RetrievalsType = Option[String] ~ Enrolments ~ Option[AffinityGroup] ~ Option[CredentialRole] ~ Option[Credentials]

  "Stub ASA Controller" must {

    "must return the correct view if the feature flag is true and user is agent" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()
      when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(GET, routes.ASAStubController.onPageLoad.url)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[ASAStubView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, applicationConfig, messages(application)).toString
      }
    }

    "must redirect to error page if the feature flag is false" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), additionalData = Map("features.asaAccessEnabled" -> false))
        .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
        .build()
      when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(
            Some(id) ~ pillar2AgentEnrolment ~ Some(Agent) ~ Some(User) ~ Some(Credentials(providerId, providerType))
          )
        )

      running(application) {
        val request = FakeRequest(GET, routes.ASAStubController.onPageLoad.url)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some("/report-pillar2-top-up-taxes/error/page-not-found")
      }
    }
  }
}
