package com.karthee.chatapp.fragments.contacts

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.karthee.chatapp.core.QueryCompleteListener
import com.karthee.chatapp.db.DbRepository
import com.karthee.chatapp.db.data.ChatUser
import com.karthee.chatapp.models.UserProfile
import com.karthee.chatapp.utils.LoadState
import com.karthee.chatapp.utils.LogMessage
import com.karthee.chatapp.utils.UserUtils
import com.karthee.chatapp.utils.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class ContactsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val dbRepo: DbRepository) : ViewModel() {

    val queryState = MutableLiveData<LoadState>()

    val list= MutableLiveData<ArrayList<ChatUser>>()

    val contactsCount = MutableLiveData("0 Contacts")

    private lateinit var chatUsers: List<ChatUser>

    init {
        LogMessage.v("ContactsViewModel init")
        CoroutineScope(Dispatchers.IO).launch{
            chatUsers=dbRepo.getChatUserList()
        }
    }

    fun getContacts()=dbRepo.getAllChatUser()

    fun setContactCount(size: Int) {
        contactsCount.value="$size Contacts"
    }

    fun startQuery() {
        try {
            queryState.value=LoadState.OnLoading
            val success=UserUtils.updateContactsProfiles(onQueryCompleted)
            if (!success)
                queryState.value=LoadState.OnFailure(java.lang.Exception("Recursion exception"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val onQueryCompleted=object : QueryCompleteListener {
        override fun onQueryCompleted(queriedList: ArrayList<UserProfile>) {
            try {
                LogMessage.v("Query Completed ${UserUtils.queriedList.size}")
                val localContacts=UserUtils.fetchContacts(context)
                val finalList = ArrayList<ChatUser>()
                val list=UserUtils.queriedList

                //set localsaved name to queried users
                for(doc in list){
                    val savedNumber=localContacts.firstOrNull { it.email == doc.email }
                    if(savedNumber!=null){
                        val chatUser= UserUtils.getChatUser(doc, chatUsers, savedNumber.name)
                        Timber.v("Contact ${chatUser.documentId}")
                        finalList.add(chatUser)
                    }
                }
                contactsCount.value="${finalList.size} Contacts"
                queryState.value=LoadState.OnSuccess(finalList)
                CoroutineScope(Dispatchers.IO).launch {
                    dbRepo.insertMultipleUser(finalList)
                }
                context.toast("Contacts refreshed")
                setDefaultValues()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setDefaultValues() {
        //set default values
        UserUtils.totalRecursionCount=0
        UserUtils.resultCount=0
        UserUtils.queriedList.clear()
    }

    override fun onCleared() {
        LogMessage.v("ContactsViewModel OnCleared")
        super.onCleared()
    }

    fun setUnReadCountZero(chatUser: ChatUser) {
        UserUtils.setUnReadCountZero(dbRepo,chatUser)
    }

}