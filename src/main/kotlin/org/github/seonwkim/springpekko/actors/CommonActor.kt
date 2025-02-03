package org.github.seonwkim.springpekko.actors

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.ActorContext
import org.apache.pekko.actor.typed.javadsl.Behaviors
import java.io.Serializable

object CommonActor {
    sealed interface Command : Serializable
    data class RunnableWithContextCommand<T>(
        val runnable: (context: ActorContext<*>) -> T,
    ) : Command

    fun create(): Behavior<Command> {
        return Behaviors.setup { context ->
            Behaviors.receiveMessage { command ->
                when (command) {
                    is RunnableWithContextCommand<*> -> {
                        command.runnable.invoke(context)
                    }
                }
                Behaviors.same()
            }
        }
    }
}
