package com.nadoyo.reactivemodels

import play.api.libs.iteratee._
import reactivemongo.bson.BSONDocument
import scala.concurrent.ExecutionContext

object Iteratees {
  /**
   * Generalization of withDocumentLimit.
   * This just lets you apply a function A => B to each element in an enumerator of As, and accumulate the results in a future of a list of Bs.
   * BUT, it also takes a limit which stops reading from the enumerator after it has reached that number of elements.
   */
  def withLimit[FROM, TO](limit: Int)(f: FROM => TO): Iteratee[FROM, List[TO]] = {
    def step(idx: Int, acc: List[TO])(input: Input[FROM]): Iteratee[FROM, List[TO]] = input match {
      case Input.EOF | Input.Empty => Done(acc, Input.EOF)
      case in @ Input.El(from) if idx >= limit => Done(acc, in)
      case Input.El(from) => Cont[FROM, List[TO]](in => step(idx + 1, f(from) :: acc)(in))
    }
    Cont[FROM, List[TO]](in => step(0, List())(in))
  }

  def withDocumentLimit[U](limit: Int)(f: BSONDocument => U): Iteratee[BSONDocument, List[U]] = withLimit(limit)(f)

  def withLimitIdentity[FROM](limit: Int): Iteratee[FROM, List[FROM]] = withLimit[FROM, FROM](limit)(x => x)

  def collectIteratee[A](implicit ec : ExecutionContext) : Iteratee[A, List[A]] = Iteratee.fold(List[A]())((acc, a) => a :: acc)
}