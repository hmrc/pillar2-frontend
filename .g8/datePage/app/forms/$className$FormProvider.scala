package forms

import java.time.LocalDate

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey     = "$className;format="decap"$.error.invalid",
        allRequiredKey = "$className;format="decap"$.error.required.all",
        twoRequiredKey = "$className;format="decap"$.error.required.two",
        requiredKey    = "$className;format="decap"$.error.required",
        invalidDay = "$className;format="decap"$.error.day.nan",
        invalidDayLength = "$className;format="decap"$.error.day.length",
        invalidMonth = "$className;format="decap"$.error.month.nan",
        invalidMonthLength = "$className;format="decap"$.error.month.length",
        invalidYear = "$className;format="decap"$.year.nan",
        invalidYearLength = "$className;format="decap"$.error.year.length",
        messageKeyPart = "$className;format="decap"$"
      )
    )
}
