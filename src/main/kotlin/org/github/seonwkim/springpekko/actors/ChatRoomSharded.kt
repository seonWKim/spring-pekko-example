package org.github.seonwkim.springpekko.actors

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.javadsl.ActorContext
import org.apache.pekko.actor.typed.javadsl.Behaviors
import org.apache.pekko.cluster.sharding.typed.ShardingMessageExtractor
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityTypeKey
import org.apache.pekko.cluster.typed.Cluster
import java.io.Serializable

object ChatRoomShardedActor {

    val entityTypeKey: EntityTypeKey<Command> = EntityTypeKey.create(Command::class.java, "ChatRoom")

    sealed interface Command : Serializable {
        val roomId: String
    }

    data class JoinRoom(
        override val roomId: String,
        val user: String,
        val replyTo: ActorRef<NewMessage>
    ) : Command

    data class SendMessage(
        override val roomId: String,
        val user: String,
        val message: String
    ) : Command

    data class NewMessage(
        override val roomId: String,
        val messages: List<String>
    ) : Command

    data class GetParticipantsRequest(
        override val roomId: String,
        val replyTo: ActorRef<GetParticipantsResponse>
    ) : Command

    data class GetParticipantsResponse(
        val participants: List<String>
    ) : Serializable

    fun create(
        roomId: String,
    ): Behavior<Command> {
        return Behaviors.setup { context: ActorContext<Command> ->
            context.log.info("Chat room [$roomId] initialized")

            val localChatRoomActorToUsers = mutableMapOf<ActorRef<NewMessage>, MutableList<String>>()
            val cluster = Cluster.get(context.system)
            val nodeAddress = cluster.selfMember().address()

            Behaviors.receiveMessage { command ->
                when (command) {
                    is JoinRoom -> {
                        val users = localChatRoomActorToUsers.getOrDefault(command.replyTo, mutableListOf())
                        users.add(command.user)
                        context.log.info("[$nodeAddress] User [${command.user}] joined ChatRoom [$roomId]")
                        localChatRoomActorToUsers[command.replyTo] = users
                        localChatRoomActorToUsers.forEach { (ref, _) ->
                            ref.tell(
                                NewMessage(
                                    roomId = roomId,
                                    messages = listOf("User(${command.user}) joined the room")
                                )
                            )
                        }
                    }

                    is SendMessage -> {
                        val address = context.self.path().toString()
                        val port = context.system.address().port().get()
                        localChatRoomActorToUsers.forEach { (ref, _) ->
                            ref.tell(
                                NewMessage(
                                    roomId = roomId,
                                    messages = listOf(
                                        "User(${command.user}): ${command.message}",
                                        "Actor: $address:$port"
                                    ),
                                )
                            )
                        }
                    }

                    is GetParticipantsRequest -> {
                        val participants = localChatRoomActorToUsers.values.flatten().toList()
                        command.replyTo.tell(GetParticipantsResponse(participants))
                    }

                    else -> {

                    }
                }

                Behaviors.same()
            }
        }
    }


}

object ChatRoomMessageExtractor : ShardingMessageExtractor<ChatRoomShardedActor.Command, ChatRoomShardedActor.Command>() {
    override fun entityId(message: ChatRoomShardedActor.Command): String {
        return message.roomId
    }

    override fun shardId(entityId: String): String {
        return (entityId.hashCode() % 100).toString()
    }

    override fun unwrapMessage(message: ChatRoomShardedActor.Command): ChatRoomShardedActor.Command {
        return message
    }
}
