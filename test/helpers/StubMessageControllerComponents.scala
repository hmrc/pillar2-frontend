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

package helpers

import akka.stream.testkit.NoMaterializer
import controllers.actions.DefaultUnauthenticatedControllerComponents
import play.api.http.{DefaultFileMimeTypes, FileMimeTypesConfiguration, HttpConfiguration}
import play.api.i18n.Messages.UrlMessageSource
import play.api.i18n._
import play.api.mvc._
import play.api.test.Helpers._
import repositories.UnauthenticatedDataRepository

import java.util.Locale
import scala.concurrent.ExecutionContext

trait StubMessageControllerComponents extends Configs {

  val lang = Lang(new Locale("en"))

  val langs: Langs = new DefaultLangs(Seq(lang))

  val httpConfiguration = new HttpConfiguration()

  implicit val messages: Map[String, String] =
    Messages
      .parse(UrlMessageSource(this.getClass.getClassLoader.getResource("messages.en")), "")
      .toOption
      .getOrElse(Map.empty[String, String])

  implicit lazy val messagesApi: MessagesApi =
    new DefaultMessagesApi(
      messages = Map("default" -> messages),
      langs = langs
    )

  implicit val messagesImpl: MessagesImpl = MessagesImpl(lang, messagesApi)

  def stubMessagesControllerComponents()(implicit
    executionContext: ExecutionContext
  ): MessagesControllerComponents =
    DefaultMessagesControllerComponents(
      new DefaultMessagesActionBuilderImpl(stubBodyParser(AnyContentAsEmpty), messagesApi),
      DefaultActionBuilder(stubBodyParser(AnyContentAsEmpty)),
      stubPlayBodyParsers(NoMaterializer),
      messagesApi,
      langs,
      new DefaultFileMimeTypes(FileMimeTypesConfiguration()),
      executionContext
    )

}
