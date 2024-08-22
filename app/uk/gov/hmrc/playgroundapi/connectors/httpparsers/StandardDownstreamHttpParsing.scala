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

package uk.gov.hmrc.playgroundapi.connectors.httpparsers

import play.api.http.Status.*
import play.api.libs.json.Reads
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.playgroundapi.connectors.DownstreamOutcome
import uk.gov.hmrc.playgroundapi.models.{ResponseWrapper, ServiceError}

trait StandardDownstreamHttpParsing extends HttpParsing {

  // Return Right[DownstreamOutcome[Unit]] as success response has no body - no need to assign it a value
  implicit def readsEmpty(implicit successCode: SuccessCode = SuccessCode(NO_CONTENT)): HttpReads[DownstreamOutcome[Unit]] =
    (_: String, url: String, response: HttpResponse) => handleResponse(url, response)(corrId =>
      Right(ResponseWrapper(corrId, ()))
    )

  implicit def reads[A: Reads](implicit successCode: SuccessCode = SuccessCode(OK)): HttpReads[DownstreamOutcome[A]] =
    (_: String, url: String, response: HttpResponse) => handleResponse(url, response)(corrId =>
      response.validateJson[A] match {
        case Some(ref) => Right(ResponseWrapper(corrId, ref))
        case None      => Left(ResponseWrapper(corrId, ServiceError.InternalError))
      }
    )

  private def handleResponse[Resp](url: String, response: HttpResponse)
                                  (successHandling: String => DownstreamOutcome[Resp])
                                  (implicit successCode: SuccessCode): DownstreamOutcome[Resp] = {
    val correlationId: String = retrieveCorrelationId(response)

    if (response.status != successCode.status) {
      logger.warn(
        "[StandardDownstreamHttpParser][read] - " +
          s"Error response received from Downstream with status: ${response.status} and body\n" +
          s"${response.body} and correlationId: $correlationId when calling $url")
    } else {
      logger.info(
        "[StandardDownstreamHttpParser][read] - " +
          s"Success response received from Downstream with correlationId: $correlationId when calling $url"
      )
    }

    response.status match {
      case successCode.status => successHandling(correlationId)
      case BAD_REQUEST | NOT_FOUND | FORBIDDEN | CONFLICT | UNPROCESSABLE_ENTITY =>
        Left(ResponseWrapper(correlationId, parseErrors(response)))
      case _ => Left(ResponseWrapper(correlationId, ServiceError.InternalError))
    }
  }

}
