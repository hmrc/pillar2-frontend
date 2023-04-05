package controllers

import base.SpecBase
import forms.TradingBusinessConfirmationFormProvider
import helpers.BaseSpec
import models.TradingBusinessConfirmation
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.TradingBusinessConfirmationView

class TradingBusinessConfirmationControllerSpec extends BaseSpec {
  val tradingBusinessConfirmationTemplate = new TradingBusinessConfirmationView()

  val tradingBusinessConfirmationTemplate: transport_contract =
    new TradingBusinessConfirmationView(mainTemplate, govukRadios, govukButton, govukFieldSet, formWithCSRF, govukErrorSummary, hmrcPageHeading)

  "Trading Business Confirmation Controller" {
    val formProvider = new TradingBusinessConfirmationFormProvider()
    val form: Form[TradingBusinessConfirmation] = formProvider()
    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.TradingBusinessConfirmationController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TradingBusinessConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }
  }
}
