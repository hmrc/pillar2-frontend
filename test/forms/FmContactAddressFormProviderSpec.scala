package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class SubscriptionAddressFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "SubscriptionAddress.error.required"
  val lengthKey   = "SubscriptionAddress.error.length"
  val maxLength   = 200

  val form = new SubscriptionAddressFormProvider()()

  ".value" - {

    val fieldName = "value"

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
}
