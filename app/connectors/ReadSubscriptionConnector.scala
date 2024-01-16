package connectors

import config.FrontendAppConfig
import models.subscription.ReadSubscriptionRequestParameters
import play.api.Logging
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, NotFoundException, UpstreamErrorResponse}

import java.io.IOException
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import utils.Pillar2SessionKeys

class ReadSubscriptionConnector @Inject() (val userAnswersConnectors: UserAnswersConnectors, val config: FrontendAppConfig, val http: HttpClient)
    extends Logging {

  private def constructUrl(readSubscriptionParameter: ReadSubscriptionRequestParameters): String =
    s"${config.pillar2BaseUrl}/report-pillar2-top-up-taxes/subscription/read-subscription/${readSubscriptionParameter.id}/${readSubscriptionParameter.plrReference}"
  def readSubscription(
    readSubscriptionParameter: ReadSubscriptionRequestParameters
  )(implicit hc:               HeaderCarrier, ec: ExecutionContext): Future[Option[JsValue]] = {
    val subscriptionUrl = constructUrl(readSubscriptionParameter)
    http
      .GET[HttpResponse](subscriptionUrl)
      .map {
        case response if is2xx(response.status) =>
          Some(response.json)
      }
      .recoverWith {
        case _: NotFoundException | _: UpstreamErrorResponse =>
          Future.successful(None)
        case e: IOException =>
          logger.warn(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Connection issue when calling read subscription: ${e.getMessage}")
          Future.successful(None)
        case e: Exception =>
          logger.error(s"[Session ID: ${Pillar2SessionKeys.sessionId(hc)}] - Unexpected error when calling read subscription: ${e.getMessage}")
          Future.failed(e)
      }

  }
}
