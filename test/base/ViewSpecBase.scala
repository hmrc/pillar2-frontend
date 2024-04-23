package base

import config.FrontendAppConfig
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.{FakeRequest, Injecting}

trait ViewSpecBase extends PlaySpec with GuiceOneAppPerSuite with Injecting with Matchers {

  val request:                        Request[AnyContent] = FakeRequest().withCSRFToken
  protected lazy val realMessagesApi: MessagesApi         = inject[MessagesApi]
  val appConfig = inject[FrontendAppConfig]

  implicit def messages: Messages =
    realMessagesApi.preferred(request)

}
