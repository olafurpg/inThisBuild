package a
import io.circe.generic.JsonCodec, io.circe.syntax._
// import io.circe.syntax._

@JsonCodec case class Bar(i: Int, s: String)
// defined class Bar
// defined object Bar

object Main extends App {
  println(Bar(13, "Qux").asJson)
}
