package org.github.seonwkim.springpekko.actors

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.cluster.ClusterEvent
import org.apache.pekko.cluster.typed.Cluster
import org.apache.pekko.cluster.typed.Subscribe

/**
 * For logging cluster events
 */
object ClusterListenerBehavior {
    fun create(): Behavior<ClusterEvent.MemberEvent> {
        return Behaviors.setup { context ->
            val cluster = Cluster.get(context.system)
            context.log.info("Cluster Listener initialized on node: ${cluster.selfMember().address()}")

            // Subscribe to cluster membership changes
            cluster.subscriptions().tell(Subscribe(context.self, ClusterEvent.MemberEvent::class.java))

            Behaviors.receiveMessage { event ->
                when (event) {
                    is ClusterEvent.MemberUp -> {
                        context.log.info("🚀 New node joined the cluster: ${event.member().address()}")
                    }

                    is ClusterEvent.MemberRemoved -> {
                        context.log.info("❌ Node left the cluster: ${event.member().address()} (was ${event.previousStatus()})")
                    }

                    is ClusterEvent.MemberWeaklyUp -> {
                        context.log.info("⚠️ Node is weakly up: ${event.member().address()}")
                    }

                    else -> {
                        context.log.debug("Other cluster event: $event")
                    }
                }
                Behaviors.same()
            }
        }
    }
}
