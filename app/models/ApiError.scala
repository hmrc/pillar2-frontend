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

package models

sealed trait ApiError extends Throwable
case object RetryableGatewayError extends ApiError {
  val retryableStatuses: Set[Int] = Set(500, 502)
}
case object InternalIssueError extends ApiError
case object NoResultFound extends ApiError
case object UnexpectedResponse extends ApiError
case object UnexpectedJsResult extends ApiError
case object DuplicateSubmissionError extends ApiError
case object UnprocessableEntityError extends ApiError
case object DuplicateSafeIdError extends ApiError
case object MissingReferenceNumberError extends ApiError
