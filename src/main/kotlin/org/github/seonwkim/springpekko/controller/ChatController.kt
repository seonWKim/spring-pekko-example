package org.github.seonwkim.springpekko.controller

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.github.seonwkim.springpekko.service.ChatService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ChatController(
    private val chatService: ChatService
) {
    @MessageMapping("/chat/join")
    fun join(request: JoinChatRoomRequest) {
        chatService.join(roomId = request.roomId, user = request.user)
    }

    @PostMapping("/chat/participants/{roomId}")
    suspend fun getParticipants(@PathVariable roomId: String): List<String> {
        return withContext(Dispatchers.IO) {
            chatService.getParticipants(roomId)
        }
    }

    @PostMapping("/chat/send")
    suspend fun send(@RequestBody request: SendMessageRequest) {
        withContext(Dispatchers.IO) {
            chatService.send(
                roomId = request.roomId,
                user = request.user,
                message = request.message
            )
        }
    }
}

data class JoinChatRoomRequest(
    val roomId: String,
    val user: String,
)

data class SendMessageRequest(
    val roomId: String,
    val user: String,
    val message: String
)
