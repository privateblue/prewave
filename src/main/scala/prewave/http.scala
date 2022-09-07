package prewave

import cats.syntax.all._
import cats.effect._

import org.http4s.client._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.{Method, Request}
import org.http4s.circe._
import org.http4s._

import io.circe._

object http {
  def client[F[_]: Async]: Resource[F, Client[F]] =
    EmberClientBuilder.default[F].build

  def get[F[_]: Concurrent, T: Decoder](httpClient: Client[F], uri: Uri): F[List[T]] =
    for {
      json <- httpClient.expect[Json](Request[F](Method.GET, uri))
      items <- json.as[List[T]].liftTo[F]
    } yield items
}
