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

package views

import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.Html

object ViewUtils {

  def title(form: Form[_], title: String, section: Option[String] = None)(implicit messages: Messages): String =
    titleNoForm(
      title = s"${errorPrefix(form)} ${messages(title)}",
      section = section
    )

  def titleNoForm(title: String, section: Option[String] = None)(implicit messages: Messages): String =
    s"${messages(title)} - ${section.fold("")(messages(_) + " - ")}${messages("service.name")} - ${messages("site.govuk")}"

  def errorPrefix(form: Form[_])(implicit messages: Messages): String =
    if (form.hasErrors || form.hasGlobalErrors) messages("error.browser.title.prefix") else ""

  def errorKey(form: Form[_], fieldKey: String): String = {
    val errorMessageKeys = form.errors.map(x => x.message).find(x => x.contains(fieldKey))
    val emptyErrorFields = form.errors.filter(x => x.key == fieldKey).flatMap(x => x.args.map(_.toString)).headOption

    def extractErrorKey(error: Option[String], errorField: Option[String], key: String): String =
      error match {
        case Some(msg) if msg.contains("all")      => s"$key.day"
        case Some(msg) if msg.contains("required") => s"$key.${errorField.getOrElse("day")}"
        case Some(msg) if msg.contains("year")     => s"$key.year"
        case Some(msg) if msg.contains("month")    => s"$key.month"
        case _                                     => s"$key.day"
      }

    extractErrorKey(errorMessageKeys, emptyErrorFields, fieldKey)
  }

  def hideForScreenReader(visualKey: String, screenReaderKey: Option[String]): Html =
    screenReaderKey.fold(
      Html(s"<span aria-hidden='true'>$visualKey</span>")
    )(screenReaderAlt => Html(s"<span aria-hidden='true'>$visualKey</span> <span class='govuk-visually-hidden'>$screenReaderAlt</span>"))

}
