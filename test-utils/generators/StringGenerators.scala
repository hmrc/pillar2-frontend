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

trait StringGenerators {
  def randomStringGenerator(n: Int): String =
    n match {
      case 1 => util.Random.nextPrintableChar.toString
      case _ => util.Random.nextPrintableChar.toString ++ randomStringGenerator(n - 1)
    }

  def randomAlphaNumericStringGenerator(n: Int): String = {
    val alphanumericChars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    val r                 = new scala.util.Random

    Vector.fill(n)(alphanumericChars(r.nextInt(alphanumericChars.length))).mkString
  }
}
