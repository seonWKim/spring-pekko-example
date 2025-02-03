package org.github.seonwkim.springpekko.actors

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.ActorContext
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.github.seonwkim.springpekko.utils.ActorUtils
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.io.Serializable

object ChatRoomLocalActor {

    sealed interface Command : Serializable

    data class JoinRoom(
        val roomId: String,
        val user: String,
    ) : Command

    data class NewMessageReceived(
        val roomId: String,
        val messages: List<String>
    ) : Command

    fun create(
        messagingTemplate: SimpMessagingTemplate
    ): Behavior<Command> {
        return Behaviors.setup { context: ActorContext<Command> ->

            val messageAdapter: ActorRef<ChatRoomShardedActor.NewMessage> =
                context.messageAdapter(ChatRoomShardedActor.NewMessage::class.java) { ack ->
                    NewMessageReceived(ack.roomId, ack.messages)
                }

            Behaviors.receiveMessage { command ->
                when (command) {
                    is JoinRoom -> {
                        context.log.info("Join received for user(${command.user}) to room(${command.roomId})")
                        val chatRoom = ActorUtils.getShardedActor(ChatRoomShardedActor.entityTypeKey, command.roomId)
                        chatRoom.tell(
                            ChatRoomShardedActor.JoinRoom(
                                roomId = command.roomId,
                                user = command.user,
                                replyTo = messageAdapter
                            )
                        )
                    }

                    is NewMessageReceived -> {
                        val address = context.self.path().toString()
                        val port = context.system.address().port().get()
                        context.log.info("New message received $command")
                        val messages = command.messages + listOf("Actor: $address:$port")
                        messagingTemplate.convertAndSend(
                            "/topic/${command.roomId}",
                            messages
                        )
                    }
                }
                Behaviors.same()
            }
        }
    }
}
