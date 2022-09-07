package prewave

object Match {
  def find(alerts: List[Alert], terms: List[Term]): List[(Alert, Term)] =
    for {
      alert <- alerts
      term <- terms
      if alert.contents.exists(c =>
        c.language == term.language && (
          (term.keepOrder && c.text.contains(term.text)) ||
            (!term.keepOrder && term.text.split(" ").forall(c.text.contains))
        )
      )
    } yield (alert, term)

  def groupByAlertId(matches: List[(Alert, Term)]): Map[String, List[Int]] =
    matches
      .groupBy(_._1)
      .map { case (a, t) => a.id -> t.map(_._2.id) }

  def show(matches: Map[String, List[Int]]): String =
    matches.map(m => s"""Alert ${m._1} matches term(s) ${m._2.mkString(", ")}""").mkString("\n")
}
