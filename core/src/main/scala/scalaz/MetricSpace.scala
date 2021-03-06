package scalaz

/*
1.
(distance(a, b) == 0) == (a == b)

2. Commutativity
distance(a, b) == distance(b, a)

3. Triangle Equality
distance(a, b) + distance(b, c) >= distance(a, c)
*/
sealed trait MetricSpace[-A] {
  def distance(a1: A, a2: A): Int
}

trait MetricSpaces {
  def metricSpace[A](f: (A, A) => Int): MetricSpace[A] = new MetricSpace[A] {
    def distance(a1: A, a2: A) = f(a1, a2)
  }

  import Scalaz._

  def levenshtein[M[_], A](implicit l: Length[M], i: Index[M], e: Equal[A]): MetricSpace[M[A]] = metricSpace[M[A]](_ <---> _)

  implicit def levenshteins: MetricSpace[String] = levenshtein[List, Char] ∙ ((s: String) => s.toList)
}
