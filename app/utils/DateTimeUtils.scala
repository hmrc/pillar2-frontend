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

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneId, ZonedDateTime}

object DateTimeUtils {

  private val ukZoneId:  ZoneId = ZoneId.of("Europe/London")
  private val gmtZoneId: ZoneId = ZoneId.of("GMT")
  private val utcZoneId: ZoneId = ZoneId.of("UTC")

  // Patterns
  private lazy val defaultDatePattern:     String = "d MMMM yyyy"
  private lazy val defaultDateTimePattern: String = "d MMMM yyyy, h:mma (zzz)"
  private lazy val defaultTimePattern:     String = "hh:mma (zzz)"

  // TODO:
  def formattedCurrentDate: String = LocalDate.now().format(defaultDateFormatter)

  // 3 December 2011
  lazy val defaultDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(defaultDatePattern)

  // 3 December 2011, 10:15am (GMT)
  lazy val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(defaultDateTimePattern)

  // 10:15am (GMT)
  lazy val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(defaultTimePattern)

  // '2011-12-03T10:15:30', '2011-12-03T10:15:30+01:00' or '2011-12-03T10:15:30+01:00[Europe/London]'
  lazy val isoDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

  // 2011-12-03
  lazy val isoLocalDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

  // 2011-12-03T10:15:30
  lazy val isoLocalDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  // 3 December 2011, 10:15am (GMT)
  def getCurrentTimestampGMT: String =
    ZonedDateTime
      .now(gmtZoneId)
      .format(dateTimeFormatter)

  // 10:15am (GMT)
  def getCurrentTimeGMT: String =
    ZonedDateTime
      .now(gmtZoneId)
      .format(timeFormatter)

}
