package com.karthee.chatapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.karthee.chatapp.db.daos.ChatUserDao
import com.karthee.chatapp.db.daos.GroupDao
import com.karthee.chatapp.db.daos.GroupMessageDao
import com.karthee.chatapp.db.daos.MessageDao
import com.karthee.chatapp.db.data.ChatUser
import com.karthee.chatapp.db.data.Group
import com.karthee.chatapp.db.data.GroupMessage
import com.karthee.chatapp.db.data.Message

@Database(entities = [ChatUser::class, Message::class,Group::class,GroupMessage::class],
    version = 1, exportSchema = false)
@TypeConverters(TypeConverter::class)
abstract class ChatUserDatabase : RoomDatabase()  {
    abstract fun getChatUserDao(): ChatUserDao
    abstract fun getMessageDao(): MessageDao
    abstract fun getGroupDao(): GroupDao
    abstract fun getGroupMessageDao(): GroupMessageDao
}