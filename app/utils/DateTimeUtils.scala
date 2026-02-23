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

import java.time.*
import java.time.format.DateTimeFormatter

object DateTimeUtils {

  private val gmtZoneId: ZoneId = ZoneId.of("GMT")
  val utcZoneId:         ZoneId = ZoneId.of("UTC")

  // Patterns
  private val datePattern:       String = "d MMMM yyyy"
  private val dateTimePattern:   String = "d MMMM yyyy, h:mma (zzz)"
  private val timePattern:       String = "hh:mma (zzz)"
  private val dateAtTimePattern: String = "d MMMM yyyy 'at' h:mma"

  // 3 December 2011
  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(datePattern)

  // 3 December 2011, 10:15am (GMT)
  private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern)

  // 3 December 2011 at 10:15pm
  private val dateAtTimeFormatter = DateTimeFormatter.ofPattern(dateAtTimePattern)

  // 10:15am (GMT)
  private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(timePattern)

  // '2011-12-03T10:15:30', '2011-12-03T10:15:30+01:00' or '2011-12-03T10:15:30+01:00[Europe/London]'
  val isoDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

  // 2011-12-03
  val isoLocalDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

  // 2011-12-03T10:15:30
  val isoLocalDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  extension (localDate: LocalDate) {
    def toDateFormat: String = localDate.format(dateFormatter)
  }

  extension (zonedDateTime: ZonedDateTime) {
    def toDateFormat:        String = zonedDateTime.withZoneSameLocal(gmtZoneId).format(dateFormatter)
    def toDateTimeGmtFormat: String = zonedDateTime.withZoneSameLocal(gmtZoneId).format(dateTimeFormatter)
    def toTimeGmtFormat:     String = zonedDateTime.withZoneSameLocal(gmtZoneId).format(timeFormatter)
    def toDateAtTimeFormat:  String = zonedDateTime.withZoneSameLocal(gmtZoneId).format(dateAtTimeFormatter)
  }

  private val fixedNow: Instant   = Instant.now()
  given fixedClock:     Clock     = Clock.fixed(fixedNow, utcZoneId)
  val today:            LocalDate = LocalDate.now(fixedClock)

}
