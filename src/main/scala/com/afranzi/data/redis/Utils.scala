package com.afranzi.data.redis

import scala.util.Random

object Utils {

  implicit class Rep(n: Int) {
    def times[A](f: => A) {
      1 to n foreach (_ => f)
    }
  }

  implicit class RandomSeq[T](s: Seq[T]) {
    def randomItem(implicit random: Random): T = s(random.nextInt(s.length))
  }

}
