package com.karthee.chatapp.core

import android.content.Context
import com.google.firebase.firestore.CollectionReference
import com.karthee.chatapp.FirebasePush
import com.karthee.chatapp.db.DbRepository
import com.karthee.chatapp.db.data.ChatUser
import com.karthee.chatapp.db.daos.ChatUserDao
import com.karthee.chatapp.models.UserProfile
import com.karthee.chatapp.utils.OnSuccessListener
import com.karthee.chatapp.utils.UserUtils
import com.karthee.chatapp.utils.Utils

class ChatUserUtil(private val dbRepository: DbRepository,
                   private val usersCollection: CollectionReference,
                   private val listener: OnSuccessListener?) {

    fun queryNewUserProfile(context: Context,chatUserId: String,docId: String?, unReadCount: Int=1,
                            showNotification: Boolean=false) {
        try {
            usersCollection.document(chatUserId)
                .get().addOnSuccessListener { profile ->
                    if (profile.exists()) {
                        val userProfile = profile.toObject(UserProfile::class.java)
                        val mobile =userProfile?.email!!
                        val chatUser = ChatUser(userProfile?.uId!!, mobile, userProfile)
                        chatUser.unRead=unReadCount
                        if(docId!=null)
                            chatUser.documentId=docId
                        if (Utils.isContactPermissionOk(context)) {
                            val contacts = UserUtils.fetchContacts(context)
                            val savedContact=contacts.firstOrNull { it.email.contains(userProfile.email!!) }
                            savedContact?.let {
                                chatUser.localName=it.name
                                chatUser.locallySaved=true
                            }
                        }
                        listener?.onResult(true,chatUser)
                        dbRepository.insertUser(chatUser)
                        if(showNotification)
                        FirebasePush.showNotification(context,dbRepository)
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}