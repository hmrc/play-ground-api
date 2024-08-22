package uk.gov.hmrc.playgroundapi.models.api

import play.api.libs.json.{Json, Reads}

case class TestResponseModel (field1: Int, field2: String, field3: Seq[Boolean], field4: Option[String])

object TestResponseModel {
  implicit val reads: Reads[TestResponseModel] = Json.reads[TestResponseModel]
}
