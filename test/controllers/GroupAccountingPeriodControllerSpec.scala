package controllers

import java.time.{LocalDate, ZoneOffset}
import base.SpecBase
import controllers.subscription.GroupAccountingPeriodController
import forms.GroupAccountingPeriodFormProvider
import models.{NormalMode, UserAnswers}
import pages.GroupAccountingPeriodPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.GroupAccountingPeriodView
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}

class GroupAccountingPeriodControllerSpec extends SpecBase {

  def controller(): GroupAccountingPeriodController =
    new GroupAccountingPeriodController(
      mockUserAnswersConnectors,
      preAuthenticatedActionBuilders,
      preDataRetrievalActionImpl,
      preDataRequiredActionImpl,
      formProvider,
      stubMessagesControllerComponents(),
      viewGroupAccountingPeriod
    )

  val formProvider = new GroupAccountingPeriodFormProvider()

  val validAnswer = LocalDate.now(ZoneOffset.UTC)

  lazy val groupAccountingPeriodRoute = routes.GroupAccountingPeriodController.onPageLoad(NormalMode).url

  override val emptyUserAnswers = UserAnswers(userAnswersId)

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, routes.GroupAccountingPeriodController.onPageLoad(NormalMode).url)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, routes.GroupAccountingPeriodController.onPageLoad(NormalMode).url)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "GroupAccountingPeriod Controller" when {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val result = route(application, getRequest).value

        val view = application.injector.instanceOf[GroupAccountingPeriodView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider(), NormalMode)(getRequest, appConfig(application), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(GroupAccountingPeriodPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val view = application.injector.instanceOf[GroupAccountingPeriodView]

        val result = route(application, getRequest).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formProvider().fill(validAnswer), NormalMode)(
          getRequest,
          appConfig(application),
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, groupAccountingPeriodRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = formProvider().bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[GroupAccountingPeriodView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, appConfig(application), messages(application)).toString
      }
    }

  }
}
