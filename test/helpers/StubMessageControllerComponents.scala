/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package helpers

import akka.stream.testkit.NoMaterializer
import play.api.http.{DefaultFileMimeTypes, FileMimeTypesConfiguration, HttpConfiguration}
import play.api.i18n.Messages.UrlMessageSource
import play.api.i18n._
import play.api.mvc._
import play.api.test.Helpers._

import java.util.Locale
import scala.concurrent.ExecutionContext

trait StubMessageControllerComponents extends Configs {

  val lang = Lang(new Locale("en"))

  val langs: Langs = new DefaultLangs(Seq(lang))

  val httpConfiguration = new HttpConfiguration()

  implicit val messages: Map[String, String] =
    Messages
      .parse(UrlMessageSource(this.getClass.getClassLoader.getResource("messages")), "")
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
