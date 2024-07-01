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

package models.bars

import play.api.libs.json.{JsPath, Reads, Writes}

sealed trait AccountNumberIsWellFormatted
object AccountNumberIsWellFormatted {
  case object Yes extends AccountNumberIsWellFormatted
  case object No extends AccountNumberIsWellFormatted
  case object Indeterminate extends AccountNumberIsWellFormatted

  val values = List(Yes, No, Indeterminate)

  implicit val reads: Reads[AccountNumberIsWellFormatted] =
    JsPath
      .read[String]
      .map {
        case "yes"           => Yes
        case "no"            => No
        case "indeterminate" => Indeterminate
      }

  implicit val writes: Writes[AccountNumberIsWellFormatted] = implicitly[Writes[String]].contramap[AccountNumberIsWellFormatted] {
    case Yes           => "yes"
    case No            => "no"
    case Indeterminate => "indeterminate"
  }
}

sealed trait SortCodeIsPresentOnEISCD
object SortCodeIsPresentOnEISCD {
  case object Yes extends SortCodeIsPresentOnEISCD
  case object No extends SortCodeIsPresentOnEISCD
  case object Error extends SortCodeIsPresentOnEISCD

  val values = List(Yes, No, Error)

  implicit val reads: Reads[SortCodeIsPresentOnEISCD] =
    JsPath
      .read[String]
      .map {
        case "yes"   => Yes
        case "no"    => No
        case "error" => Error
      }

  implicit val writes: Writes[SortCodeIsPresentOnEISCD] = implicitly[Writes[String]].contramap[SortCodeIsPresentOnEISCD] {
    case Yes   => "yes"
    case No    => "no"
    case Error => "error"
  }
}

sealed trait NonStandardAccountDetailsRequiredForBacs
object NonStandardAccountDetailsRequiredForBacs {
  case object Yes extends NonStandardAccountDetailsRequiredForBacs
  case object No extends NonStandardAccountDetailsRequiredForBacs
  case object Inapplicable extends NonStandardAccountDetailsRequiredForBacs

  val values = List(Yes, No, Inapplicable)

  implicit val reads: Reads[NonStandardAccountDetailsRequiredForBacs] =
    JsPath
      .read[String]
      .map {
        case "yes"          => Yes
        case "no"           => No
        case "inapplicable" => Inapplicable
      }

  implicit val writes: Writes[NonStandardAccountDetailsRequiredForBacs] =
    implicitly[Writes[String]].contramap[NonStandardAccountDetailsRequiredForBacs] {
      case Yes          => "yes"
      case No           => "no"
      case Inapplicable => "inapplicable"
    }
}

sealed trait AccountExists
object AccountExists {
  case object Yes extends AccountExists
  case object No extends AccountExists
  case object Inapplicable extends AccountExists
  case object Indeterminate extends AccountExists
  case object Error extends AccountExists

  val values = List(Yes, No, Inapplicable, Indeterminate, Error)

  implicit val reads: Reads[AccountExists] =
    JsPath
      .read[String]
      .map {
        case "yes"           => Yes
        case "no"            => No
        case "inapplicable"  => Inapplicable
        case "indeterminate" => Indeterminate
        case "error"         => Error
      }

  implicit val writes: Writes[AccountExists] =
    implicitly[Writes[String]].contramap[AccountExists] {
      case Yes           => "yes"
      case No            => "no"
      case Inapplicable  => "inapplicable"
      case Indeterminate => "indeterminate"
      case Error         => "error"
    }
}

sealed trait NameMatches
object NameMatches {
  case object Yes extends NameMatches
  case object Partial extends NameMatches
  case object No extends NameMatches
  case object Inapplicable extends NameMatches
  case object Indeterminate extends NameMatches
  case object Error extends NameMatches

  val values = List(Yes, No, Inapplicable, Indeterminate, Error)

  implicit val reads: Reads[NameMatches] =
    JsPath
      .read[String]
      .map {
        case "yes"           => Yes
        case "partial"       => Partial
        case "no"            => No
        case "inapplicable"  => Inapplicable
        case "indeterminate" => Indeterminate
        case "error"         => Error
      }

  implicit val writes: Writes[NameMatches] =
    implicitly[Writes[String]].contramap[NameMatches] {
      case Yes           => "yes"
      case Partial       => "partial"
      case No            => "no"
      case Inapplicable  => "inapplicable"
      case Indeterminate => "indeterminate"
      case Error         => "error"
    }
}

sealed trait SortCodeSupportsDirectDebit
object SortCodeSupportsDirectDebit {
  case object Yes extends SortCodeSupportsDirectDebit
  case object No extends SortCodeSupportsDirectDebit
  case object Error extends SortCodeSupportsDirectDebit

  val values = List(Yes, No, Error)

  implicit val reads: Reads[SortCodeSupportsDirectDebit] =
    JsPath
      .read[String]
      .map {
        case "yes"   => Yes
        case "no"    => No
        case "error" => Error
      }

  implicit val writes: Writes[SortCodeSupportsDirectDebit] =
    implicitly[Writes[String]].contramap[SortCodeSupportsDirectDebit] {
      case Yes   => "yes"
      case No    => "no"
      case Error => "error"
    }
}

sealed trait SortCodeSupportsDirectCredit
object SortCodeSupportsDirectCredit {
  case object Yes extends SortCodeSupportsDirectCredit
  case object No extends SortCodeSupportsDirectCredit
  case object Error extends SortCodeSupportsDirectCredit

  val values = List(Yes, No, Error)

  implicit val reads: Reads[SortCodeSupportsDirectCredit] =
    JsPath
      .read[String]
      .map {
        case "yes"   => Yes
        case "no"    => No
        case "error" => Error
      }

  implicit val writes: Writes[SortCodeSupportsDirectCredit] =
    implicitly[Writes[String]].contramap[SortCodeSupportsDirectCredit] {
      case Yes   => "yes"
      case No    => "no"
      case Error => "error"
    }
}
