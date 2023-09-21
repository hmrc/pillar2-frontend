package controllers

import base.SpecBase
import forms.FmContactAddressFormProvider
import models.{NormalMode, UserAnswers}
<<<<<<<< HEAD:generated-test/controllers/FmContactAddressControllerSpec.scala
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.FmContactAddressPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.FmContactAddressView

import scala.concurrent.Future
========
import pages.CaptureContactAddressPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.subscriptionview.CaptureContactAddressView
>>>>>>>> c9f044c (PIL-416: add code for contact address page):test/controllers/CaptureContactAddressControllerSpec.scala

class FmContactAddressControllerSpec extends SpecBase {


  val formProvider = new FmContactAddressFormProvider()

  def controller(): FmContactAddressController =
    new FmContactAddressController(
      mockUserAnswersConnectors,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      formProvider,
      stubMessagesControllerComponents(),
      viewFmContactAddress
    )


  "FmContactAddress Controller" when {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
<<<<<<<< HEAD:generated-test/controllers/FmContactAddressControllerSpec.scala
        val request = FakeRequest(GET, routes.FmContactAddressController.onPageLoad(NormalMode).url)
========
        val request = FakeRequest(GET, controllers.subscription.routes.CaptureContactAddressController.onPageLoad(NormalMode).url)
>>>>>>>> c9f044c (PIL-416: add code for contact address page):test/controllers/CaptureContactAddressControllerSpec.scala

        val result = route(application, request).value

        val view = application.injector.instanceOf[FmContactAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(FmContactAddressPage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
<<<<<<<< HEAD:generated-test/controllers/FmContactAddressControllerSpec.scala
        val request = FakeRequest(GET, routes.FmContactAddressController.onPageLoad(NormalMode).url)

        val view = application.injector.instanceOf[FmContactAddressView]
========
        val request = FakeRequest(GET, controllers.subscription.routes.CaptureContactAddressController.onPageLoad(NormalMode).url)
>>>>>>>> c9f044c (PIL-416: add code for contact address page):test/controllers/CaptureContactAddressControllerSpec.scala

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill("answer"), NormalMode)(request, appConfig(application),messages(application)).toString
      }
    }



    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
<<<<<<<< HEAD:generated-test/controllers/FmContactAddressControllerSpec.scala
          FakeRequest(POST, routes.FmContactAddressController.onPageLoad(NormalMode).url)
========
          FakeRequest(POST, controllers.subscription.routes.CaptureContactAddressController.onPageLoad(NormalMode).url)
>>>>>>>> c9f044c (PIL-416: add code for contact address page):test/controllers/CaptureContactAddressControllerSpec.scala
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[FmContactAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application),messages(application)).toString
      }
    }


  }
}
