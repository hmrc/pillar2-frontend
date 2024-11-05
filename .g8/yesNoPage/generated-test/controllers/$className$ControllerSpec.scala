
package controllers

import base.SpecBase
import forms.$className$FormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.$className$Page
import play.api.inject.bind
import org.mockito.ArgumentMatchersSugar.eqTo
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.libs.json.Json
import views.html.$className$View
import connectors.UserAnswersConnectors
import scala.concurrent.Future

class $className$ControllerSpec extends SpecBase {


  val formProvider = new $className$FormProvider()

  "$className$ Controller" when {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.$className$Controller.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[$className$View]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(request, appConfig(), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set($className$Page, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.$className$Controller.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[$className$View]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(true), NormalMode)(request, appConfig(), messages(application)).toString
      }
    }


    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, routes.$className$Controller.onPageLoad(NormalMode).url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = formProvider().bind(Map("value" -> ""))

        val view = application.injector.instanceOf[$className$View]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(), messages(application)).toString
      }
    }

    "redirect to under construction page in case of a valid submission and save the relevant data" in {
      val expectedUserAnswers = emptyUserAnswers.setOrException($className$Page, true)
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(any())).thenReturn(Future(Json.toJson(Json.obj())))
        val request =
          FakeRequest(POST, routes.$className$Controller.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody(("value", "true"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
        verify(mockUserAnswersConnectors).save(eqTo(expectedUserAnswers.id), eqTo(expectedUserAnswers.data))(any())
      }
    }


  }
}