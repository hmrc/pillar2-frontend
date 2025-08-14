/*
 * Copyright 2025 HM Revenue & Customs
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

package utils

import com.ibm.icu.text.SimpleDateFormat
import com.ibm.icu.util.{TimeZone, ULocale}
import play.api.i18n.Messages

import java.time.{LocalDate, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.util.Date

object DateTimeUtils {
  lazy val datePattern:   String            = "d MMMM yyyy"
  lazy val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(datePattern)

  // FIXME: refactor the following code which was moved over from ViewHelpers
  // - remove ibm date/time library and us

  //only for date like Sunday 25 January 2015
  def formatDateGDS(date: LocalDate)(implicit messages: Messages): String =
    dateFormat.format(Date.from(date.atStartOfDay(ZoneId.systemDefault).toInstant))
  def getDateTimeGMT: String = {
    val gmtDateTime:       ZonedDateTime     = ZonedDateTime.now(ZoneId.of("GMT"))
    val formatter:         DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, h:mma")
    val formattedDateTime: String            = gmtDateTime.format(formatter)
    formattedDateTime + " (GMT)"
  }

  private def dateFormat(implicit messages: Messages): SimpleDateFormat = createDateFormatForPattern(datePattern)
  private val defaultTimeZone:                         TimeZone         = TimeZone.getTimeZone("Europe/London")
  private def createDateFormatForPattern(pattern: String)(implicit messages: Messages): SimpleDateFormat = {
    val uLocale:   ULocale          = new ULocale(messages.lang.code)
    val validLang: Boolean          = ULocale.getAvailableLocales.contains(uLocale)
    val locale:    ULocale          = if (validLang) uLocale else ULocale.getDefault
    val sdf:       SimpleDateFormat = new SimpleDateFormat(pattern, locale)
    sdf.setTimeZone(defaultTimeZone)
    sdf
  }

}
