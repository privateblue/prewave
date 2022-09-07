package prewave

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest._
import matchers.should.Matchers._

import java.time.ZonedDateTime

class MatchTest extends AnyFlatSpec {
  val term1 = Term(
    id = 1,
    target = 0,
    text = "hello world",
    language = "en",
    keepOrder = true
  )

  val term2 = Term(
    id = 2,
    target = 0,
    text = "hello world",
    language = "en",
    keepOrder = false
  )

  val term3 = Term(
    id = 3,
    target = 0,
    text = "hello world",
    language = "de",
    keepOrder = true
  )

  val term4 = Term(
    id = 4,
    target = 0,
    text = "hello world",
    language = "de",
    keepOrder = false
  )

  val terms = List(term1, term2, term3, term4)

  def mkAlert(id: String, text: String, lang: String): Alert =
    Alert(id, List(Content(text, None, lang)), ZonedDateTime.now(), "test")

  "Match" should "check the language first" in {
    val alerts = List(
      mkAlert("a", "hello world", "it")
    )
    Match.find(alerts, terms) shouldBe empty
  }

  "Match" should "correctly match the language" in {
    val alerts = List(
      mkAlert("a", "hello world", "de")
    )
    val matches = Match.find(alerts, terms)
    val result = Match.groupByAlertId(matches)
    result shouldBe Map("a" -> List(3, 4))
  }

  "Match" should "only match actually matching terms" in {
    val alerts = List(
      mkAlert("a", "foobar", "de"),
      mkAlert("b", "hello world", "de"),
      mkAlert("c", "whatever", "en"),
      mkAlert("d", "the world is not hello at all", "en")
    )
    val matches = Match.find(alerts, terms)
    val result = Match.groupByAlertId(matches)
    result shouldBe Map("b" -> List(3, 4), "d" -> List(2))
  }

  "Match" should "respect the keepOrder flag" in {
    val alerts = List(
      mkAlert("a", "it's a hello world message", "en"),
      mkAlert("b", "it's a hello world message", "de"),
      mkAlert("c", "the world is not hello at all", "en"),
      mkAlert("d", "the world is not hello at all", "de")
    )
    val matches = Match.find(alerts, terms)
    val result = Match.groupByAlertId(matches)
    result shouldBe Map("a" -> List(1, 2), "b" -> List(3, 4), "c" -> List(2), "d" -> List(4))
  }

}
