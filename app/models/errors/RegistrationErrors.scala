package models.errors

sealed trait RegistrationError extends Exception

case class MalformedDataError(message: String) extends Exception(message) with RegistrationError

case class InvalidOrgTypeError() extends Exception("Invalid Org Type") with RegistrationError
