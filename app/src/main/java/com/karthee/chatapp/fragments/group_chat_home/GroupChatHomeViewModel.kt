package com.karthee.chatapp.fragments.group_chat_home

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.CollectionReference
import com.karthee.chatapp.db.DbRepository
import com.karthee.chatapp.utils.MPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GroupChatHomeViewModel @Inject constructor(
    private val preference: MPreference,
    private val dbRepository: DbRepository,
    private val usersCollection: CollectionReference) : ViewModel()  {


    fun getGroupMessages() = dbRepository.getGroupWithMessages()

    fun getGroupMessagesAsList() = dbRepository.getGroupWithMessagesList()


}