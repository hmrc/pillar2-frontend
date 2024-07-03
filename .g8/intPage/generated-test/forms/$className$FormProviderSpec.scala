package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError
import scala.util.Random

class $className$FormProviderSpec extends IntFieldBehaviours {

  val form = new $className$FormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = $minimum$
    val maximum = $maximum$

    val validDataGenerator = Random.between(minimum, maximum+1).toString

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "$className;format="decap"$.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "$className;format="decap"$.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "$className;format="decap"$.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "$className;format="decap"$.error.required")
    )
  }
}
