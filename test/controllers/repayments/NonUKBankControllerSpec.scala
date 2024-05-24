package controllers.repayments

import base.SpecBase
import connectors.UserAnswersConnectors
import controllers.rfm.routes
import forms.NonUKBankFormProvider
import models.repayments.NonUKBank
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.{NonUKBankPage, RfmPrimaryContactNamePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.repayments.NonUKBankView

import scala.concurrent.Future

class NonUKBankControllerSpec extends SpecBase {

  val formProvider = new NonUKBankFormProvider()

  val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      NonUKBankPage.toString -> Json.obj(
        "bankName"          -> "Bank name",
        "nameOnBankAccount" -> "Name",
        "bic"               -> "bic",
        "iban"              -> "iban"
      )
    )
  )

  "NonUKBank Controller" when {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.repayments.routes.NonUKBankController.onPageLoad(NormalMode).url)

        val view = application.injector.instanceOf[NonUKBankView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.repayments.routes.NonUKBankController.onPageLoad(NormalMode).url)

        val view = application.injector.instanceOf[NonUKBankView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(formProvider().fill(NonUKBank("Bank name", "Name", "bic", "iban")), NormalMode)(
            request,
            appConfig(application),
            messages(application)
          ).toString
      }
    }

    "must redirect under construction when valid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors))
        .build()

      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, controllers.repayments.routes.NonUKBankController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(
              ("bankName", "Bank name"),
              ("nameOnBankAccount", "Name"),
              ("bic", "12345678"),
              ("iban", "iban")
            )

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.repayments.routes.NonUKBankController.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = formProvider().bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[NonUKBankView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

  }
}
