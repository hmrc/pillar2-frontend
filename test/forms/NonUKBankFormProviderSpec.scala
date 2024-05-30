package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class NonUKBankFormProviderSpec extends StringFieldBehaviours {

  val form = new NonUKBankFormProvider()()

  ".bankName" - {

    val fieldName   = "bankName"
    val requiredKey = "repayments.nonUKBank.error.bankName.required"
    val lengthKey   = "repayments.nonUKBank.error.bankName.length"
    val maxLength   = 40

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".nameOnBankAccount" - {

    val fieldName   = "nameOnBankAccount"
    val requiredKey = "repayments.nonUKBank.error.nameOnBankAccount.required"
    val lengthKey   = "repayments.nonUKBank.error.nameOnBankAccount.length"
    val maxLength   = 60

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".bic" - {

    val fieldName   = "bic"
    val requiredKey = "repayments.nonUKBank.error.bic.required"
    val lengthKey   = "repayments.nonUKBank.error.bic.length"
    val formatKey   = "repayments.nonUKBank.error.bic.format"
    val regex       = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$"
    val maxLength   = 11

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonEmptyRegexConformingStringWithMaxLength(regex, maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".iban" - {

    val fieldName   = "iban"
    val requiredKey = "repayments.nonUKBank.error.iban.required"
    val lengthKey   = "repayments.nonUKBank.error.iban.length"
    val regex       = "^GB[0-9]{2}[A-Z]{4}[0-9]{14}$"
    val maxLength   = 34

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonEmptyRegexConformingStringWithMaxLength(regex, maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
