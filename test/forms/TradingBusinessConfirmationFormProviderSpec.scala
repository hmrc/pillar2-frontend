package forms

import forms.behaviours.OptionFieldBehaviours
import models.TradingBusinessConfirmation
import play.api.data.FormError

class TradingBusinessConfirmationFormProviderSpec extends OptionFieldBehaviours {

  val form = new TradingBusinessConfirmationFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "tradingBusinessConfirmation.error.required"

    behave like optionsField[TradingBusinessConfirmation](
      form,
      fieldName,
      validValues = TradingBusinessConfirmation.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
