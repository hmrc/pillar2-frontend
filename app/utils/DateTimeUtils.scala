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

// FIXME: remove this library
import com.ibm.icu.text.SimpleDateFormat
import com.ibm.icu.util.{TimeZone, ULocale}
import play.api.i18n.Messages

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneId, ZonedDateTime}
import java.util.Date

object DateTimeUtils {
  private lazy val defaultTimeZone:    TimeZone = TimeZone.getTimeZone("Europe/London")
  private lazy val defaultDatePattern: String   = "d MMMM yyyy"

  // FIXME
  // 3 December 2011
  lazy val defaultDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(defaultDatePattern)

  // '2011-12-03T10:15:30', '2011-12-03T10:15:30+01:00' or '2011-12-03T10:15:30+01:00[Europe/London]'
  lazy val isoDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

  // 2011-12-03
  lazy val isoLocalDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

  // 2011-12-03T10:15:30
  lazy val isoLocalDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  // 3 December 2011, 10:15am (GMT)
  def getDateTimeGMT: String = {
    val gmtDateTime:       ZonedDateTime     = ZonedDateTime.now(ZoneId.of("GMT"))
    val formatter:         DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, h:mma")
    val formattedDateTime: String            = gmtDateTime.format(formatter)
    val result = formattedDateTime + " (GMT)"
    println(s"\n\n\nGET DATE TIME GMT: $result\n")
    result
  }

}
