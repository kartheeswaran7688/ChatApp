package com.karthee.chatapp.core

import android.content.Context
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ListenerRegistration
import com.karthee.chatapp.db.DbRepository
import com.karthee.chatapp.db.data.ChatUser
import com.karthee.chatapp.db.data.Group
import com.karthee.chatapp.models.UserProfile
import com.karthee.chatapp.utils.MPreference
import com.karthee.chatapp.utils.UserUtils
import com.karthee.chatapp.utils.Utils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatUserProfileListener @Inject
constructor(@ApplicationContext val context: Context,
             private val userCollectionRef: CollectionReference,
             private val preference: MPreference,
             private val dbRepository: DbRepository){

    private var instanceCreated=false

    companion object{
        private val listOfListeners=ArrayList<ListenerRegistration>()
        fun removeListener(){
            listOfListeners.forEach {
                it.remove()
            }
        }
    }

    private fun getChatUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            val users=dbRepository.getChatUserList()
            withContext(Dispatchers.Main){
                addSnapShotListener(users)
            }
        }
    }

    private fun addSnapShotListener(users: List<ChatUser>) {
        val myUserId=preference.getUid().toString()
        for (user in users){
            if (user.id==myUserId)
                continue
            val listener=  userCollectionRef.document(user.id).addSnapshotListener { profile, error ->
                if (error!=null) {
                    Timber.v(error)
                    return@addSnapshotListener
                }
                val userProfile = profile?.toObject(UserProfile::class.java)
                userProfile?.let { pro->
                    val chatUser=users.firstOrNull { it.id== pro.uId }
                    if (chatUser!=null){
                        chatUser.user=pro
                        checkForContactSaved(chatUser,pro.email!!)
                        updateInLocal(chatUser)
                    }
                }
            }
            listOfListeners.add(listener)
        }
    }

    private fun updateInLocal(chatUser: ChatUser) {
        val chatUserId=chatUser.id
        dbRepository.insertUser(chatUser)
        //updating in groups
        CoroutineScope(Dispatchers.IO).launch {
            val groups=dbRepository.getGroupList()
            val containingList= mutableListOf<Group>()
            for (group in groups){
                val members=group.members
                val isContains= members?.any { it.id == chatUserId } ?: false
                if (isContains){
                    val index=members?.indexOfFirst { it.id==chatUserId }
                    members!![index!!]=chatUser
                    containingList.add(group)
                }
            }
            dbRepository.insertMultipleGroup(containingList)
        }
    }

    private fun checkForContactSaved(chatUser: ChatUser, mobileNo: String) {
        if (Utils.isContactPermissionOk(context)) {
            val contacts = UserUtils.fetchContacts(context)
            val savedContact=contacts.firstOrNull { it.email.contains(mobileNo) }
            if (savedContact!=null){
                chatUser.localName=savedContact.name
                chatUser.locallySaved=true
            }else{
                //contact deleted
                val profile=chatUser.user
                val mobile = profile.email
                chatUser.localName=mobile!!
                chatUser.locallySaved=false
            }
        }
    }

    fun initListener() {
        if (!instanceCreated) {
            getChatUsers()
            instanceCreated=true
        }
    }

}