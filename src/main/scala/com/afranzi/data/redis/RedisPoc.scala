package com.afranzi.data.redis

import java.time.{Clock, LocalDateTime, ZoneOffset}

import com.redis.RedisClient

import scala.collection.immutable
import scala.util.Random

trait RedisPoc {

  val utcZone: ZoneOffset = ZoneOffset.UTC

  //- QUEUES
  val delayedQueue = "delayed"
  val tasksQueue = "tasks"


  def delayTask(queueName: String)(id: String, delay: Int)(implicit r: RedisClient): Unit = {
    r.zadd(queueName, delay, id)
    println(s"[-] Delaying task [$id]")
  }

  def consumeTasks(queueName: String)(implicit r: RedisClient): Unit = {
    r.lpop(queueName) match {
      case Some(item) => println(s"[x] Received task [$item]")
      case _ => print(".")
    }
  }

  def pollTasks(delayedQueueName: String, readyQueueName: String)(implicit r: RedisClient, clock: Clock): Seq[String] = {
    val now = LocalDateTime.now(clock)

    val ts: Int = now.toEpochSecond(utcZone).toInt

    val rangedItems: Option[List[(String, Double)]] = r.zrangebyscoreWithScore(delayedQueueName, max = ts, limit = Some(0, 10))

    val items: immutable.Seq[(String, Double)] = rangedItems match {
      case Some(Nil) => print(".")
        List.empty
      case Some(list) => list
      case None =>
        print(".")
        List.empty
    }

    items.map { case (item: String, score: Double) =>
      val time = LocalDateTime.ofEpochSecond(score.toLong, 0, utcZone)

      println(s"[x] Received delayed task [$item] with score[$score] - [$time]")
      r.rpush(readyQueueName, item)
      r.zrem(delayedQueueName, item)
      item
    }
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

  def pollTaskInfinite(delayedQueueName: String, readyQueueName: String, sleepMillisDelay: Int)(implicit r: RedisClient, clock: Clock): Unit = {
    while (true) {
      pollTasks(delayedQueueName, readyQueueName)
      Thread.sleep(sleepMillisDelay)
    }
  }

}
