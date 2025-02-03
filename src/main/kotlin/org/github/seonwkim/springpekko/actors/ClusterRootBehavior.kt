package org.github.seonwkim.springpekko.actors

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.ActorContext
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.cluster.typed.Cluster
import org.github.seonwkim.springpekko.utils.ActorUtils

object ClusterRootBehavior {
    fun create(): Behavior<String> {
        return Behaviors.setup { context ->
            val cluster = Cluster.get(context.system)
            context.log.info("Cluster node started at ${cluster.selfMember().address()}")

            // Spawn cluster related actors
            spawnAndRegisterActor(
                context,
                "ClusterListener",
                ClusterListenerBehavior.create(),
                ActorsHolder.Name.CLUSTER_LISTENER_ACTOR
            )
            spawnAndRegisterActor(
                context,
                "UtilsActor",
                CommonActor.create(),
                ActorsHolder.Name.UTILS_ACTOR
            ).let { ActorUtils.setCommonActor(it) }

            // Spawn sharding related actors
            ClusterShardingSetup.create(context.system)

            Behaviors.receiveMessage { message ->
                context.log.info("Received message: {}", message)
                Behaviors.same()
            }
        }
    }

    private fun <T> spawnAndRegisterActor(
        context: ActorContext<String>,
        actorName: String,
        behavior: Behavior<T>,
        holderName: ActorsHolder.Name
    ): ActorRef<T> {
        val actorRef = context.spawn(behavior, actorName)
        ActorsHolder.register(holderName, actorRef)
        return actorRef
    }
}
