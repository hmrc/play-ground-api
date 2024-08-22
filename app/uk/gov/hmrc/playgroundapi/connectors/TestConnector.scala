package uk.gov.hmrc.playgroundapi.connectors

import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.playgroundapi.config.AppConfig
import uk.gov.hmrc.playgroundapi.connectors.DownstreamUri.DesUri
import uk.gov.hmrc.playgroundapi.connectors.httpparsers.StandardDownstreamHttpParsing
import uk.gov.hmrc.playgroundapi.models.api.TestResponseModel

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestConnector @Inject()(val http: HttpClientV2, val appConfig: AppConfig)
  extends BaseDownstreamConnector with StandardDownstreamHttpParsing {

    def testGetCall(nino: String)(implicit hc: HeaderCarrier,
                                  ec: ExecutionContext,
                                  correlationId: String): Future[DownstreamOutcome[TestResponseModel]] = {
      val downstreamUri: DesUri = DesUri(apiNumber = "#1234", value = s"some/uri/$nino")

      get[TestResponseModel](downstreamUri)
  }
}
