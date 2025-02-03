package org.github.seonwkim.springpekko.actors

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding
import org.apache.pekko.cluster.sharding.typed.javadsl.Entity

object ClusterShardingSetup {
    fun create(system: ActorSystem<*>) {
        val sharding = ClusterSharding.get(system)
        sharding.init(
            Entity.of(ChatRoomSharded.entityTypeKey) { entityContext ->
                // entityContext.entityId is used to create a unique instance of the ChatRoom actor for each shard
                ChatRoomSharded.create(entityContext.entityId)
            }.withMessageExtractor(ChatRoomMessageExtractor)
        )

        system.log().info("✅ ChatRoom Sharding Initialized")
    }
}
