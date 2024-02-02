package controllers.rfm

import config.{AppConfig, ErrorHandler, FrontendAppConfig}
import models.Mode
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.Future
import forms.rfm.StartPageFormProvider
import views.html.rfm.StartPageView

class StartPageController @Inject()(
    formProvider: StartPageFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: StartPageView
)
(implicit val appConfig: FrontendAppConfig) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Ok(view(StartPageFormProvider)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = Action.async { implicit request =>
    StartPageFormProvider.bindFromRequest().fold(
      error => {
        Future.successful(BadRequest(view(error)))
      },
      _ =>
        Future.successful(Redirect(???))
    )
  }
}
