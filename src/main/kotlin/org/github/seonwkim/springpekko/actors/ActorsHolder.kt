package org.github.seonwkim.springpekko.actors

import org.apache.pekko.actor.typed.ActorRef

object ActorsHolder {
    private val map = hashMapOf<String, ActorRef<*>>()

    enum class Name(val key: String) {
        CLUSTER_LISTENER_ACTOR("cluster-listener-actor"),
        UTILS_ACTOR("utils-actor")
    }

    fun register(name: Name, behavior: ActorRef<*>) {
        map[name.key] = behavior
    }

    fun get(name: Name): ActorRef<*> {
        return map[name.key]!!
    }
}
