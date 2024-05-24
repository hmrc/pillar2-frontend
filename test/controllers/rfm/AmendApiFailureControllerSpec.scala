package controllers.rfm

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.rfm.AmendApiFailureView

class AmendApiFailureControllerSpec extends SpecBase {

  "AmendApiFailure Controller" when {

    "must return OK and the correct view for a GET when rfm feature true" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> true
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.AmendApiFailureController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AmendApiFailureView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, appConfig(application), messages(application)).toString
      }
    }

    "must redirect to UnderConstruction page when rfm feature false" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure(
          Seq(
            "features.rfmAccessEnabled" -> false
          ): _*
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rfm.routes.AmendApiFailureController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
      }

    }
  }
}
