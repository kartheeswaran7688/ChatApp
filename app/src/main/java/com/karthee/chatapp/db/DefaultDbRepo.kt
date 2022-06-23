package com.karthee.chatapp.db

import androidx.lifecycle.LiveData
import com.karthee.chatapp.db.data.ChatUser
import com.karthee.chatapp.db.data.ChatUserWithMessages
import kotlinx.coroutines.flow.Flow

interface DefaultDbRepo {

    fun insertUser(user: ChatUser)

    fun insertMultipleUser(users: List<ChatUser>)

    fun getAllChatUser(): LiveData<List<ChatUser>>

    fun getChatUserList(): List<ChatUser>

    fun getChatUserById(id: String): ChatUser?

    fun deleteUserById(userId: String)

    fun getChatUserWithMessages(): Flow<List<ChatUserWithMessages>>

    fun getChatUserWithMessagesList(): List<ChatUserWithMessages>

    fun nukeTable()
}