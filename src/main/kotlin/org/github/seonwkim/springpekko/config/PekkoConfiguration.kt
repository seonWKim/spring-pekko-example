package org.github.seonwkim.springpekko.config

import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding
import org.github.seonwkim.springpekko.actors.ClusterRootBehavior
import org.github.seonwkim.springpekko.utils.ActorUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PekkoConfiguration(
    @Value("\${pekko.remote.artery.canonical.port}")
    val port: Int,
) {

    @Bean
    fun actorSystem(): ActorSystem<*> {
        val clusterName = "voyager-actor-cluster-system"
        val config = ConfigFactory.parseString(
            """
            pekko.actor.provider = "cluster"
            pekko.actor.allow-java-serialization = on
            pekko.actor.warn-about-java-serializer-usage = off
            pekko.remote.artery.canonical.hostname = "127.0.0.1"
            pekko.remote.artery.canonical.port = $port
            pekko.cluster.seed-nodes = [
                "pekko://$clusterName@127.0.0.1:2551",
                "pekko://$clusterName@127.0.0.1:2552",
                "pekko://$clusterName@127.0.0.1:2553"
            ]
            pekko.cluster.downing-provider-class = "org.apache.pekko.cluster.sbr.SplitBrainResolverProvider"
        """.trimIndent()
        ).withFallback(ConfigFactory.load())

        return ActorSystem.create(
            ClusterRootBehavior.create(),
            clusterName,
            config
        )
    }

    @Bean
    fun sharding(): ClusterSharding {
        return ClusterSharding.get(actorSystem()).apply { ActorUtils.setSharding(this) }
    }
}
