package controllers

import java.time.{LocalDate, ZoneOffset}

import base.SpecBase
import play.api.inject.bind
import forms.$className$FormProvider
import models.{NormalMode, UserAnswers}
import pages.$className$Page
import connectors.UserAnswersConnectors
import play.api.test.FakeRequest
import play.api.libs.json.Json
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import play.api.test.Helpers.*
import views.html.$className$View
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import scala.concurrent.Future

class $className$ControllerSpec extends SpecBase {
  private val formProvider = new $className$FormProvider()
  private val validAnswer: LocalDate = LocalDate.now(ZoneOffset.UTC)

  private lazy val $className;format="decap"$Route : String = routes.$className$Controller.onPageLoad(NormalMode).url

  private def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, routes.$className$Controller.onPageLoad(NormalMode).url)

  private def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, routes.$className$Controller.onPageLoad(NormalMode).url)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "$className$ Controller" when {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val result = route(application, getRequest()).value

        val view = application.injector.instanceOf[$className$View]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(getRequest(), applicationConfig, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set($className$Page, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val view = application.injector.instanceOf[$className$View]

        val result = route(application, getRequest()).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(validAnswer), NormalMode)(getRequest(), applicationConfig, messages(application)).toString
      }
    }



    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, $className;format="decap"$Route)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = formProvider().bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[$className$View]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, applicationConfig, messages(application)).toString
      }
    }

    "redirect to under construction page in case of a valid submission and save the relevant data" in {
      val expectedUserAnswers = emptyUserAnswers.setOrException($className$Page, validAnswer)
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[UserAnswersConnectors].toInstance(mockUserAnswersConnectors)
          )
          .build()
      running(application) {
        when(mockUserAnswersConnectors.save(any(), any())(using any())).thenReturn(Future(Json.toJson(Json.obj())))
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.UnderConstructionController.onPageLoad.url
        verify(mockUserAnswersConnectors).save(eqTo(expectedUserAnswers.id), eqTo(expectedUserAnswers.data))(using any())
      }
    }


  }
}
