# Redis-PoC
This PoC aims to investigate how to handle a notification system using Redis.

## Run

```
# Build ignoring tests
sbt 'set test in assembly := {}' clean assembly

# Notification Publisher
java -cp target/scala-2.11/redis-assembly-0.0.1-SNAPSHOT.jar  com.afranzi.data.redis.NotificationPublisher

# Notification Puller
java -cp target/scala-2.11/redis-assembly-0.0.1-SNAPSHOT.jar  com.afranzi.data.redis.NotificationPuller

# Notification Subscriber to the task channel
java -cp target/scala-2.11/redis-assembly-0.0.1-SNAPSHOT.jar  com.afranzi.data.redis.NotificationSubscriber
```

## Notes
In case we need more than one Notification Puller to support higher frequencies, we shall implement a pool/lock system to avoid pulling twice the same message.
> Ref: [6.2 Distributed locking](https://redislabs.com/ebook/part-2-core-concepts/chapter-6-application-components-in-redis/6-2-distributed-locking/)

Besides, we need to persist in some way the delayed notifications in case Redis *crashes/shuts down* since we could lose all the pending notifications.

## Tutorials

- [e-Book - Redis in Action](https://redislabs.com/community/ebook/)


## Links of Interest
- [3.6 Publish/subscribe](https://redislabs.com/ebook/part-2-core-concepts/chapter-3-commands-in-redis/3-6-publishsubscribe/)
- [6.4.2 Delayed tasks](https://redislabs.com/ebook/part-2-core-concepts/chapter-6-application-components-in-redis/6-4-task-queues/6-4-2-delayed-tasks/)
- [6.2.3 Building a lock in Redis](https://redislabs.com/ebook/part-2-core-concepts/chapter-6-application-components-in-redis/6-2-distributed-locking/6-2-3-building-a-lock-in-redis/)
- [Pubsub implementation using Redis and Akka actors](https://github.com/debasishg/akka-redis-pubsub)