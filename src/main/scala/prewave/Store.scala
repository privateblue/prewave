package prewave

import cats.syntax.all._
import cats.effect._

import scala.io.Source
import scala.util.Try

import java.io._

trait Store {
  def readTerms[F[_]: Sync]: F[List[Term]]
  def saveTerms[F[_]: Sync](terms: List[Term]): F[Unit]
  def readMatches[F[_]: Sync]: F[Map[String, List[Int]]]
  def appendMatches[F[_]: Sync](matches: Map[String, List[Int]]): F[Unit]
}

object DefaultStore extends Store {
  val termsFile = "terms"
  val matchesFile = "matches"

  def readTerms[F[_]: Sync]: F[List[Term]] = {
    val file = new File(termsFile)
    if (file.exists) {
      Resource
        .make {
          Sync[F].blocking(Source.fromFile(termsFile))
        } { source =>
          Sync[F].blocking(source.close())
        }
        .use {
          _.getLines()
            .map(l => termFromLine(l))
            .toList
            .sequence
        }
    } else
      Sync[F].raiseError(new FileNotFoundException(s"$termsFile cannot be found"))
  }

  def saveTerms[F[_]: Sync](terms: List[Term]): F[Unit] =
    Resource
      .make {
        Sync[F].blocking(new BufferedWriter(new FileWriter(termsFile)))
      } { bw =>
        Sync[F].blocking(bw.close())
      }
      .use { bw =>
        Sync[F].blocking(terms.foreach(t => bw.write(termToLine(t))))
      }

  def termToLine(term: Term): String =
    s"${term.id},${term.target},${term.text},${term.language},${term.keepOrder}\n"

  def termFromLine[F[_]: Sync](line: String): F[Term] = {
    val tryTerm = for {
      columns <- Try(line.split(","))
      id <- Try(columns(0).toInt)
      target <- Try(columns(1).toInt)
      text = columns(2)
      language = columns(3)
      keepOrder <- Try(columns(4).toBoolean)
    } yield Term(id, target, text, language, keepOrder)
    Sync[F].fromEither(tryTerm.toEither)
  }

  def readMatches[F[_]: Sync]: F[Map[String, List[Int]]] = {
    val file = new File(matchesFile)
    if (file.exists())
      Resource
        .make {
          Sync[F].blocking(Source.fromFile(matchesFile))
        } { source =>
          Sync[F].blocking(source.close())
        }
        .use {
          _.getLines()
            .map(l => matchFromLine(l))
            .toList
            .sequence
            .map(_.toMap)
        }
    else Sync[F].pure(Map.empty)
  }

  def appendMatches[F[_]: Sync](matches: Map[String, List[Int]]): F[Unit] =
    Resource
      .make {
        Sync[F].blocking(new BufferedWriter(new FileWriter(matchesFile, true)))
      } { bw =>
        Sync[F].blocking(bw.close())
      }
      .use { bw =>
        Sync[F].blocking(matches.foreach(m => bw.write(matchToLine(m._1, m._2))))
      }

  def matchToLine(alertId: String, termIds: List[Int]): String =
    s"""$alertId,${termIds.mkString(" ")}\n"""

  def matchFromLine[F[_]: Sync](line: String): F[(String, List[Int])] = {
    val tryAlert = for {
      columns <- Try(line.split(","))
      alertId = columns(0)
      termIds <- columns(1).split(" ").toList.map(tid => Try(tid.toInt)).sequence
    } yield (alertId, termIds)
    Sync[F].fromEither(tryAlert.toEither)
  }

}
