package com.afranzi.data.redis

import java.time.Clock

import com.redis._

import scala.util.Random

object NotificationPublisher extends App with RedisPoc {

  /**
    * It publish notifications with Random delays
    */

  def run(): Unit = {
    implicit val r: RedisClient = new RedisClient("localhost", 6379)
    implicit val random: Random = new Random()
    implicit val clock: Clock = Clock.systemDefaultZone()

    messageProducerInfinite(delayedQueue, 300, 5)
  }

  run()
}

object NotificationPuller extends App with RedisPoc {

  /**
    * It pulls the delayed queue where the Score is the timestamp from now()
    */

  def run(): Unit = {
    implicit val r: RedisClient = new RedisClient("localhost", 6379)
    implicit val random: Random = new Random()
    implicit val clock: Clock = Clock.systemDefaultZone()

    pullTaskInfinite(delayedQueue, tasksQueue, 100)
  }

  run()
}

object NotificationSubscriber extends App with RedisPoc {

  /**
    * It subscribes to the channel with Notifications ready to be send
    */

  def run(): Unit = {
    implicit val r: RedisClient = new RedisClient("localhost", 6379)
    implicit val random: Random = new Random()
    implicit val clock: Clock = Clock.systemDefaultZone()

    r.subscribe(tasksQueue)(consumeChannel)
  }

  run()
}
