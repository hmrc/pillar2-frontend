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

package controllers.actions

import play.api.Configuration
import play.api.mvc.Results.Redirect
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FeatureFlagActionFactory @Inject() (configuration: Configuration, controllerComponents: MessagesControllerComponents)(implicit
  ec:                                                    ExecutionContext
) {

  def asaAccessAction:        ActionBuilder[MessagesRequest, AnyContent] = actionStart andThen whenEnabled("asaAccessEnabled")
  def repaymentsAccessAction: ActionBuilder[MessagesRequest, AnyContent] = actionStart andThen whenEnabled("repaymentsAccessEnabled")
  def rfmAccessAction:        ActionBuilder[MessagesRequest, AnyContent] = actionStart andThen whenEnabled("rfmAccessEnabled")

  private def actionStart: ActionBuilder[MessagesRequest, AnyContent] =
    controllerComponents.messagesActionBuilder.compose(controllerComponents.actionBuilder)

  private def whenEnabled(
    featureFlag: String
  ): FeatureFlagAction =
    new FeatureFlagAction {
      override def invokeBlock[A](
        request: MessagesRequest[A],
        block:   MessagesRequest[A] => Future[Result]
      ): Future[Result] = {
        val isFeatureEnabled =
          configuration.getOptional[Boolean](s"features.$featureFlag").getOrElse(false)

        if (isFeatureEnabled) {
          block(request)
        } else {
          Future.successful(Redirect(controllers.routes.ErrorController.pageNotFoundLoad))
        }

      }

      override protected def executionContext: ExecutionContext = ec
    }
}
trait FeatureFlagAction extends ActionFunction[MessagesRequest, MessagesRequest]
