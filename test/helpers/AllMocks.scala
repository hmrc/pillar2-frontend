/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package helpers

import config.FrontendAppConfig
import org.mockito.Mockito
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

//TODO: Add all mocking instants in here.
trait AllMocks extends MockitoSugar { me: BeforeAndAfterEach =>

  val mockAuditConnector:    AuditConnector    = mock[AuditConnector]
  val mockAuthConnector:     AuthConnector     = mock[AuthConnector]
  val mockFrontendAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  override protected def beforeEach(): Unit =
    Seq(
      mockAuditConnector,
      mockAuthConnector,
      mockFrontendAppConfig
    ).foreach(Mockito.reset(_))
}
