package utils

import scala.concurrent.Future

object FutureConverter {

  implicit class FutureOps[A](val a: A) extends AnyVal {
    def toFuture: Future[A] = Future.successful(a)
  }

}
