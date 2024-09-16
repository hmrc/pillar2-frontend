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

package generators

import models.{FinancialHistory, TransactionHistory}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalacheck.{Arbitrary, Gen, Shrink}
import wolfendale.scalacheck.regexp.RegexpGen

import java.time.{Instant, LocalDate, ZoneOffset}
import scala.math.BigDecimal.RoundingMode

trait Generators extends UserAnswersGenerator with PageGenerators with ModelGenerators with UserAnswersEntryGenerators {

  implicit val dontShrink: Shrink[String] = Shrink.shrinkAny

  def genIntersperseString(gen: Gen[String], value: String, frequencyV: Int = 1, frequencyN: Int = 10): Gen[String] = {

    val genValue: Gen[Option[String]] = Gen.frequency(frequencyN -> None, frequencyV -> Gen.const(Some(value)))

    for {
      seq1 <- gen
      seq2 <- Gen.listOfN(seq1.length, genValue)
    } yield seq1.toSeq.zip(seq2).foldLeft("") {
      case (acc, (n, Some(v))) =>
        acc + n + v
      case (acc, (n, _)) =>
        acc + n
    }
  }

  def intsInRangeWithCommas(min: Int, max: Int): Gen[String] = {
    val numberGen = choose[Int](min, max).map(_.toString)
    genIntersperseString(numberGen, ",")
  }

  def intsLargerThanMaxValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat (x => x > Int.MaxValue)

  def intsSmallerThanMinValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat (x => x < Int.MinValue)

  def nonNumerics: Gen[String] =
    alphaStr suchThat (_.size > 0)

  def decimals: Gen[String] =
    arbitrary[BigDecimal]
      .suchThat(_.abs < Int.MaxValue)
      .suchThat(!_.isValidInt)
      .map(_.formatted("%f"))

  def intsBelowValue(value: Int): Gen[Int] =
    arbitrary[Int] suchThat (_ < value)

  def intsAboveValue(value: Int): Gen[Int] =
    arbitrary[Int] suchThat (_ > value)

  def intsOutsideRange(min: Int, max: Int): Gen[Int] =
    arbitrary[Int] suchThat (x => x < min || x > max)

  def nonBooleans: Gen[String] =
    arbitrary[String]
      .suchThat(_.nonEmpty)
      .suchThat(_ != "true")
      .suchThat(_ != "false")

  def nonEmptyString: Gen[String] =
    arbitrary[String] suchThat (_.nonEmpty)

  def stringsWithMaxLength(maxLength: Int): Gen[String] =
    for {
      length <- choose(1, maxLength)
      chars  <- listOfN(length, arbitrary[Char])
    } yield chars.mkString

  def stringsWithMinLength(minLength: Int): Gen[String] =
    for {
      length <- choose(minLength + 1, minLength + 5)
      chars  <- listOfN(length, arbitrary[Char])
    } yield chars.mkString

  def nonEmptyRegexConformingStringWithMaxLength(regex: String, maxLength: Int): Gen[String] = {
    val regexGen = RegexpGen.from(regex)
    regexGen
      .suchThat(s => s.trim.nonEmpty && s.length <= maxLength)
  }

  def longStringsConformingToRegex(regex: String, minLength: Int): Gen[String] =
    RegexpGen
      .from(regex)
      .suchThat(_.length > minLength)
      .map(_.padTo(minLength + 1, 'a'))

  def regexWithMaxLength(maxLength: Int, genLimit: Int, regex: String): Gen[String] =
    for {
      length      <- choose(maxLength, genLimit)
      regexString <- Gen.listOfN(length, RegexpGen.from(regex))
    } yield regexString.mkString
  def stringsLongerThan(minLength: Int): Gen[String] = for {
    maxLength <- (minLength * 2).max(100)
    length    <- Gen.chooseNum(minLength + 1, maxLength)
    chars     <- listOfN(length, arbitrary[Char])
  } yield chars.mkString

  def stringsExceptSpecificValues(excluded: Seq[String]): Gen[String] =
    nonEmptyString suchThat (!excluded.contains(_))

  def oneOf[T](xs: Seq[Gen[T]]): Gen[T] =
    if (xs.isEmpty) {
      throw new IllegalArgumentException("oneOf called on empty collection")
    } else {
      val vector = xs.toVector
      choose(0, vector.size - 1).flatMap(vector(_))
    }

  def datesBetween(min: LocalDate, max: LocalDate): Gen[LocalDate] = {

    def toMillis(date: LocalDate): Long =
      date.atStartOfDay.atZone(ZoneOffset.UTC).toInstant.toEpochMilli

    Gen.choose(toMillis(min), toMillis(max)).map { millis =>
      Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate
    }
  }

  def decimalInRangeWithCommas(min: Double, max: Double): Gen[String] = {
    val numberGen = choose[Double](min, max)
    genIntersperseString(numberGen.toString, ",")
  }

  def decimalsBelowValue(value: BigDecimal): Gen[BigDecimal] =
    arbitraryBigDecimalWithMax2DecimalPlaces suchThat (_ < value)

  def decimalsAboveValue(value: BigDecimal): Gen[BigDecimal] =
    arbitraryBigDecimalWithMax2DecimalPlaces suchThat (_ > value)

  def decimalsOutsideRange(min: BigDecimal, max: BigDecimal): Gen[BigDecimal] =
    arbitraryBigDecimalWithMax2DecimalPlaces suchThat (x => x < min - 1 || x > max + 1)

  private def arbitraryBigDecimalWithMax2DecimalPlaces: Gen[BigDecimal] =
    Gen
      .chooseNum(Double.MinValue, Double.MaxValue)
      .map(d => BigDecimal(d).setScale(2, RoundingMode.HALF_UP))

  implicit lazy val financialHistoryArbitrary: Arbitrary[FinancialHistory] =
    Arbitrary {
      for {
        date         <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        paymentType  <- nonEmptyString
        amountPaid   <- arbitrary[BigDecimal]
        amountRepaid <- arbitrary[BigDecimal]
      } yield FinancialHistory(date, paymentType, amountPaid, amountRepaid)
    }

  implicit lazy val transactionHistoryArbitrary: Arbitrary[TransactionHistory] =
    Arbitrary {
      for {
        plrReference     <- nonEmptyString
        financialHistory <- Gen.listOf(arbitrary[FinancialHistory])
      } yield TransactionHistory(plrReference, financialHistory)
    }

}
