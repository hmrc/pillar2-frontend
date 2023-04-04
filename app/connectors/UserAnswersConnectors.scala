package connectors

import models.UserAnswers
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.http.HttpReads.is2xx
import play.api.http.Status._
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserAnswersConnectors  @Inject()(
                                        @Named("pillar2Url") pillar2BaseUrl: String,
                                        httpClient:                  HttpClient
                                      )(implicit ec:                 ExecutionContext)
{
  private val url = s"$pillar2BaseUrl/pillar2"

  def save(id: String, data: JsValue)(implicit headerCarrier: HeaderCarrier): Future[UserAnswers] = {
    httpClient.POST[JsValue,HttpResponse](s"$url/registration-subscription/$id", data).map {
      response => response.status match {
        case OK => Future.successful(data)
      }


    }
  }

  def get(id: String)(implicit headerCarrier: HeaderCarrier): Future[Option[UserAnswers]] = {

  }

  def remove(id: String)(implicit headerCarrier: HeaderCarrier): Future[Option[UserAnswers]] = {

  }



  def lastUpdated(id: String)(implicit headerCarrier: HeaderCarrier): Future[Option[UserAnswers]] = {

  }

}
