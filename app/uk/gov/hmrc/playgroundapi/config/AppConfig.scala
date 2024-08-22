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

package uk.gov.hmrc.playgroundapi.config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(config: ServicesConfig, configuration: Configuration) {

  val appName: String = configuration.get[String]("appName")

  // Downstream Config
  private val desBaseUrl: String                         = config.baseUrl("des")
  private val desEnv: String                             = config.getString("microservice.services.des.env")
  private val desToken: String                           = config.getString("microservice.services.des.token")
  private val desEnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.des.environmentHeaders")

  private val ifBaseUrl: String = config.baseUrl("if")
  private val ifEnv: String = config.getString("microservice.services.if.env")
  private val ifToken: String = config.getString("microservice.services.if.token")
  private val ifEnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.if.environmentHeaders")

  lazy val desDownstreamConfig: DownstreamConfig = DownstreamConfig(
    baseUrl = desBaseUrl,
    env = desEnv,
    token = desToken,
    environmentHeaders = desEnvironmentHeaders
  )

  lazy val ifDownstreamConfig: DownstreamConfig = DownstreamConfig(
    baseUrl = ifBaseUrl,
    env = ifEnv,
    token = ifToken,
    environmentHeaders = ifEnvironmentHeaders
  )
}
