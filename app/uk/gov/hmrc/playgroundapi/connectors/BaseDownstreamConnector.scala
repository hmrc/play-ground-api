/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.playgroundapi.connectors

import izumi.reflect.Tag
import play.api.Logger
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.ws.BodyWritable
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.playgroundapi.config.AppConfig

import java.net.{URI, URL}
import uk.gov.hmrc.playgroundapi.connectors.DownstreamUri.*

import scala.concurrent.{ExecutionContext, Future}

trait BaseDownstreamConnector {
  val http: HttpClientV2
  val appConfig: AppConfig

  val logger: Logger = Logger(this.getClass)

  private val jsonContentTypeHeader = HeaderNames.CONTENT_TYPE -> MimeTypes.JSON

  def post[Body: BodyWritable: Tag, Resp](body: Body, request: DownstreamUri)(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier,
      httpReads: HttpReads[DownstreamOutcome[Resp]],
      correlationId: String): Future[DownstreamOutcome[Resp]] = {

    def doPost(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] = {
      http
        .post(url = getBackendUri(request))
        .withBody(body)
        .execute
    }

    doPost(getBackendHeaders(request, hc, correlationId, jsonContentTypeHeader))
  }

  def put[Body: BodyWritable: Tag, Resp](body: Body, uri: DownstreamUri)(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier,
      httpReads: HttpReads[DownstreamOutcome[Resp]],
      correlationId: String): Future[DownstreamOutcome[Resp]] = {

    def doPut(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] = {
      http
        .put(url = getBackendUri(uri))
        .withBody(body)
        .execute
    }

    doPut(getBackendHeaders(uri, hc, correlationId, jsonContentTypeHeader))
  }

  def get[Resp](uri: DownstreamUri)(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier,
      httpReads: HttpReads[DownstreamOutcome[Resp]],
      correlationId: String): Future[DownstreamOutcome[Resp]] = {

    def doGet(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] =
      http
        .get(url = getBackendUri(uri))
        .execute

    doGet(getBackendHeaders(uri, hc, correlationId))
  }

  def delete[Resp](uri: DownstreamUri)(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier,
      httpReads: HttpReads[DownstreamOutcome[Resp]],
      correlationId: String): Future[DownstreamOutcome[Resp]] = {

    def doDelete(implicit hc: HeaderCarrier): Future[DownstreamOutcome[Resp]] =
      http
        .delete(url = getBackendUri(uri))
        .execute

    doDelete(getBackendHeaders(uri, hc, correlationId))
  }

  private def getBackendUri[Resp](uri: DownstreamUri): URL =
    URI(s"${configFor(uri).baseUrl}/${uri.value}").toURL

  private def getBackendHeaders[Resp](uri: DownstreamUri,
                                      hc: HeaderCarrier,
                                      correlationId: String,
                                      additionalHeaders: (String, String)*): HeaderCarrier = {
    val downstreamConfig = configFor(uri)

    val passThroughHeaders = hc
      .headers(downstreamConfig.environmentHeaders.getOrElse(Seq.empty))
      .filterNot(hdr => additionalHeaders.exists(_._1.equalsIgnoreCase(hdr._1)))

    HeaderCarrier(
      extraHeaders = hc.extraHeaders ++
        // Contract headers
        Seq(
          "Authorization" -> s"Bearer ${downstreamConfig.token}",
          "Environment"   -> downstreamConfig.env,
          "CorrelationId" -> correlationId
        ) ++
        additionalHeaders ++
        passThroughHeaders
    )
  }

  private def configFor[Resp](uri: DownstreamUri) =
    uri match {
      case DesUri(_, _) => appConfig.desDownstreamConfig
      case IfUri(_, _) => appConfig.ifDownstreamConfig
    }

}
