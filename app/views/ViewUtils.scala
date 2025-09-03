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

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneId, ZonedDateTime}

object ViewUtils {

  lazy val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def title(form: Form[_], title: String, section: Option[String] = None)(implicit messages: Messages): String =
    titleNoForm(
      title = s"${errorPrefix(form)} ${messages(title)}",
      section = section
    )

  def titleNoForm(title: String, section: Option[String] = None)(implicit messages: Messages): String =
    s"${messages(title)} - ${section.fold("")(messages(_) + " - ")}${messages("service.name")} - ${messages("site.govuk")}"

  def noTitle(implicit messages: Messages): String = s"${messages("service.name")} - ${messages("site.govuk")}"

  def errorPrefix(form: Form[_])(implicit messages: Messages): String =
    if (form.hasErrors || form.hasGlobalErrors) messages("error.browser.title.prefix") else ""

  def localDateErrorKey(form: Form[_], fieldKey: String): String = {
    val fieldErrors   = form.errors.filter(_.key.startsWith(fieldKey))
    val missingFields = fieldErrors.flatMap(_.args.map(_.toString)).distinct

    val invalidDay   = form(s"$fieldKey.day").value.exists(day => day.toIntOption.exists(d => d < 1 || d > 31))
    val invalidMonth = form(s"$fieldKey.month").value.exists(month => month.toIntOption.exists(m => m < 1 || m > 12))
    val invalidYear  = form(s"$fieldKey.year").value.exists(year => year.toIntOption.exists(y => y < 1000 || y > 9999))

    if (invalidDay || fieldErrors.exists(_.key == s"$fieldKey.day") || missingFields.contains("day")) {
      s"$fieldKey.day"
    } else if (invalidMonth || fieldErrors.exists(_.key == s"$fieldKey.month") || missingFields.contains("month")) {
      s"$fieldKey.month"
    } else if (invalidYear || fieldErrors.exists(_.key == s"$fieldKey.year") || missingFields.contains("year")) {
      s"$fieldKey.year"
    } else {
      s"$fieldKey.day"
    }
  }

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

  def formattedCurrentDate: String = LocalDate.now().format(dateFormatter)

  def currentTimeGMT: String = {
    val zonedTime     = ZonedDateTime.now(ZoneId.of("GMT"))
    val formatter     = DateTimeFormatter.ofPattern("hh:mma (zzz)")
    val formattedTime = zonedTime.format(formatter)
    formattedTime
  }

  def formattedCurrency(amount: BigDecimal): String =
    amount match {
      case n if n.scale <= 0 => f"$n%,.0f"
      case n if n.scale == 1 => f"$n%,.2f"
      case n                 => f"$n%,.2f"
    }

  def userTypeDependentText(groupText: String, agentText: String)(implicit isAgent: Boolean): String =
    if (isAgent) agentText else groupText
}
