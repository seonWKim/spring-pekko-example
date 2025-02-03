package org.github.seonwkim.springpekko.service

import kotlinx.coroutines.future.await
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.javadsl.AskPattern
import org.github.seonwkim.springpekko.actors.ChatRoomLocal
import org.github.seonwkim.springpekko.actors.ChatRoomSharded
import org.github.seonwkim.springpekko.utils.ActorUtils
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import kotlin.jvm.optionals.getOrNull

@Service
class ChatService(
    private val messagingTemplate: SimpMessagingTemplate,
) {
    fun join(roomId: String, user: String) {
        ActorUtils.tellActor { context ->
            val actorName = "chat-room-local-$roomId"
            val actor: ActorRef<ChatRoomLocal.Command> =
                context.getChild(actorName).getOrNull()?.let { it as ActorRef<ChatRoomLocal.Command> } ?: context.spawn(
                    ChatRoomLocal.create(messagingTemplate),
                    actorName
                )
            actor.tell(
                ChatRoomLocal.JoinRoom(
                    roomId = roomId,
                    user = user
                )
            )
        }
    }

    suspend fun getParticipants(roomId: String): List<String> {
        return ActorUtils.askActor { context ->
            val chatRoom = ActorUtils.getShardedActor(ChatRoomSharded.entityTypeKey, roomId)
            val timeout = Duration.ofSeconds(5)
            AskPattern.ask(
                chatRoom,
                { replyTo -> ChatRoomSharded.GetParticipantsRequest(roomId = roomId, replyTo = replyTo) },
                timeout,
                context.system.scheduler()
            ).toCompletableFuture()
        }.await().participants
    }

    fun send(roomId: String, user: String, message: String) {
        val chatRoom = ActorUtils.getShardedActor(ChatRoomSharded.entityTypeKey, roomId)
        chatRoom.tell(
            ChatRoomSharded.SendMessage(
                roomId = roomId,
                user = user,
                message = message
            )
        )
    }
}
