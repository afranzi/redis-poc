package com.afranzi.data.redis

import java.time.{Clock, LocalDateTime, ZoneOffset}

import com.redis._

import scala.collection.immutable
import scala.util.Random

trait RedisPoc {

  val utcZone: ZoneOffset = ZoneOffset.UTC

  //- QUEUES
  val delayedQueue = "delayed"
  val tasksQueue = "tasks"

  //- PUBLISHER
  def delayTask(queueName: String)(id: String, delay: Int)(implicit r: RedisClient): Unit = {
    r.zadd(queueName, delay, id)
    println(s"[-] Delaying task [$id]")
  }

  def messageProducerInfinite(queueName: String, secondsDelay: Int, sleepMillisDelay: Int)(implicit r: RedisClient, clock: Clock, random: Random): Unit = {
    val taskDelayer: (String, Int) => Unit = delayTask(queueName)

    var i = 1
    while (true) {
      val taskDelay = random.nextInt(secondsDelay)
      val expirationTime = LocalDateTime.now(clock).plusSeconds(taskDelay)
      val taskScore = expirationTime.toEpochSecond(utcZone).toInt
      taskDelayer(s"Notification ID:[$i] - [$expirationTime]", taskScore)
      Thread.sleep(sleepMillisDelay)
      i += 1
    }
  }

  //- PULLER
  def pullTasks(delayedQueueName: String, readyQueueName: String)(implicit r: RedisClient, clock: Clock): Seq[String] = {
    val now = LocalDateTime.now(clock)
    val ts: Int = now.toEpochSecond(utcZone).toInt
    val rangedItems: Option[List[(String, Double)]] = r.zrangebyscoreWithScore(delayedQueueName, max = ts, limit = Some(0, 10))
    val items: immutable.Seq[(String, Double)] = rangedItems match {
      case Some(Nil) =>
        print(".")
        List.empty
      case Some(list) =>
        println()
        list
      case None =>
        print(".")
        List.empty
    }

    items.map {
      case (item: String, score: Double) =>
        val time = LocalDateTime.ofEpochSecond(score.toLong, 0, utcZone)
        println(s"[x] Received delayed task [$item] with score[$score] - [$time]")

        r.publish(readyQueueName, item)
        r.zrem(delayedQueueName, item)
        item
    }
  }

  def pullTaskInfinite(delayedQueueName: String, readyQueueName: String, sleepMillisDelay: Int)(implicit r: RedisClient, clock: Clock): Unit = {
    while (true) {
      pullTasks(delayedQueueName, readyQueueName)
      Thread.sleep(sleepMillisDelay)
    }
  }

  //- SUBSCRIBER
  def consumeChannel(pubsub: PubSubMessage)(implicit r: RedisClient, clock: Clock): Unit = pubsub match {
    case S(channel, no) => println("Subscribed to " + channel + " and count = " + no)
    case U(channel, no) => println("Unsubscribed from " + channel + " and count = " + no)
    case M(_, msg) =>
      val now = LocalDateTime.now(clock)
      println(s"[x] Received task [$msg] [$now]")
  }

}
