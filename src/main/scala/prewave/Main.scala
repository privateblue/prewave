package prewave

import cats.syntax.all._
import cats.effect._
import cats.effect.std.Console

import org.http4s.client._
import org.http4s.implicits._

import io.circe.generic.auto._

object Main extends IOApp {
  val apiKey = "mikolaj:2e88a961a6c6eb2e1fbc42fcc9b69e8ff7d8c23ea3f05a42eea2fc0736d41408"

  val termsApi = uri"https://services.prewave.ai/adminInterface/api/testQueryTerm"
    .withQueryParam("key", apiKey)

  val alertsApi = uri"https://services.prewave.ai/adminInterface/api/testAlerts"
    .withQueryParam("key", apiKey)

  def run(args: List[String]): IO[ExitCode] =
    http
      .client[IO]
      .use(client => program(client))
      .as(ExitCode.Success)

  def program[F[_]: Async: Console](httpClient: Client[F]): F[Unit] =
    for {
      // read terms from file
      terms <- DefaultStore.readTerms
        .handleErrorWith { _ =>
          // if no terms file exists yet, create one, after retrieving terms from the api
          for {
            ts <- http.get[F, Term](httpClient, termsApi)
            _ <- DefaultStore.saveTerms(ts)
          } yield ts
        }
      // read previous matches from file
      previousMatches <- DefaultStore.readMatches
      // retrieve alerts from api
      apiAlerts <- http.get[F, Alert](httpClient, alertsApi)
      // filter out alerts that were already matched before
      alerts = apiAlerts.filterNot(a => previousMatches.keySet.contains(a.id))
      // calculate matches
      matches = Match.find(alerts, terms)
      result = Match.groupByAlertId(matches)
      // append new matches to file
      _ <- DefaultStore.appendMatches(result)
      // print new matches to console
      _ <- Console[F] println Match.show(result)
    } yield ()
}
