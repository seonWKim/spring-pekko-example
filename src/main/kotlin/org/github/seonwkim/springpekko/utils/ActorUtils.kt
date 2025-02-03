package org.github.seonwkim.springpekko.utils

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.javadsl.ActorContext
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityRef
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityTypeKey
import org.github.seonwkim.springpekko.actors.CommonActor
import java.util.concurrent.CompletableFuture

object ActorUtils {

    private lateinit var clusterSharding: ClusterSharding
    private lateinit var commonActor: ActorRef<CommonActor.Command>

    fun setCommonActor(commonActor: ActorRef<CommonActor.Command>) {
        ActorUtils.commonActor = commonActor
    }

    fun setSharding(clusterSharding: ClusterSharding) {
        ActorUtils.clusterSharding = clusterSharding
    }

    fun tellActor(runnable: (context: ActorContext<*>) -> Unit) {
        commonActor.tell(CommonActor.RunnableWithContextCommand(runnable))
    }

    fun <T> askActor(runnable: (context: ActorContext<*>) -> CompletableFuture<T>): CompletableFuture<T> {
        val result = CompletableFuture<T>()
        commonActor.tell(CommonActor.RunnableWithContextCommand { context ->
            val completableFuture = runnable(context)

            completableFuture.whenComplete { response, exception ->
                if (exception != null) {
                    result.completeExceptionally(exception)
                } else {
                    result.complete(response)
                }
            }
        })

        return result
    }

    fun <T> getShardedActor(entityTypeKey: EntityTypeKey<T>, entityId: String): EntityRef<T> {
        return clusterSharding.entityRefFor(entityTypeKey, entityId)
    }
}
