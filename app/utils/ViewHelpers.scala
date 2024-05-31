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

package utils
import com.ibm.icu.text.SimpleDateFormat
import com.ibm.icu.util.{TimeZone, ULocale}
import play.api.Logging
import play.api.i18n.Messages
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime, ZoneId}
import java.util.Date
import javax.inject.{Inject, Singleton}

@Singleton
class ViewHelpers @Inject() extends Logging {
  //only for date like Sunday 25 January 2015
  def formatDateGDS(date: LocalDate)(implicit messages: Messages): String =
    dateFormat.format(Date.from(date.atStartOfDay(ZoneId.systemDefault).toInstant))
  private def dateFormat(implicit messages: Messages) = createDateFormatForPattern("d MMMM y")
  private val defaultTimeZone: TimeZone = TimeZone.getTimeZone("Europe/London")
  private def createDateFormatForPattern(pattern: String)(implicit messages: Messages): SimpleDateFormat = {
    val uLocale = new ULocale(messages.lang.code)
    val validLang: Boolean = ULocale.getAvailableLocales.contains(uLocale)
    val locale:    ULocale = if (validLang) uLocale else ULocale.getDefault
    val sdf = new SimpleDateFormat(pattern, locale)
    sdf.setTimeZone(defaultTimeZone)
    sdf
  }

}
