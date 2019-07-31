package com.afranzi.data.redis

import java.time.Clock

import com.redis._

import scala.util.Random

object NotificationPublisher extends App with RedisPoc {

  def run(): Unit = {
    implicit val r: RedisClient = new RedisClient("localhost", 6379)
    implicit val random: Random = new Random()
    implicit val clock: Clock = Clock.systemDefaultZone()

    messageProducerInfinite(delayedQueue, 40, 1000)
  }

  run()

}


object NotificationPoller extends App with RedisPoc {

  def run(): Unit = {
    implicit val r: RedisClient = new RedisClient("localhost", 6379)
    implicit val random: Random = new Random()
    implicit val clock: Clock = Clock.systemDefaultZone()

    pollTaskInfinite(delayedQueue, tasksQueue, 1000)
  }

  run()

}

