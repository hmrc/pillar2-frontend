package controllers.rfm

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.rfm.RfmFinalCheckYourAnswersView

class RfmFinalCheckYourAnswersControllerSpec extends SpecBase {

  "RfmFinalCheckYourAnswers Controller" when {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.rfm / RfmFinalCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RfmFinalCheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, appConfig(application), messages(application)).toString
      }
    }
  }
}
