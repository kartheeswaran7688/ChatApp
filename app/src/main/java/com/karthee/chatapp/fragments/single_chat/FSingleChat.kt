package com.karthee.chatapp.fragments.single_chat

import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.karthee.chatapp.databinding.FSingleChatBinding
import com.karthee.chatapp.db.data.ChatUser
import com.karthee.chatapp.db.data.Message
import com.karthee.chatapp.db.data.TextMessage
import com.karthee.chatapp.models.UserProfile
import com.karthee.chatapp.utils.*
import com.karthee.chatapp.utils.Utils.edtValue
import com.karthee.chatapp.views.CustomEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class FSingleChat : Fragment(), ItemClickListener,CustomEditText.KeyBoardInputCallbackListener {

    private lateinit var binding: FSingleChatBinding

    @Inject
    lateinit var preference: MPreference

    private lateinit var chatUser: ChatUser

    private lateinit var fromUser: UserProfile

    private lateinit var toUser: UserProfile

    private var messageList = mutableListOf<Message>()

    private val viewModel: SingleChatViewModel by viewModels()

    private lateinit var localUserId: String

    val args by navArgs<FSingleChatArgs>()

    private lateinit var manager: LinearLayoutManager

    private lateinit var chatUserId: String


    private var msgPostponed=false

    private val adChat: AdChat by lazy {
        AdChat(requireContext(), this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = FSingleChatBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewmodel=viewModel
        chatUser= args.chatUserProfile!!
        viewModel.setUnReadCountZero(chatUser)
        setListeners()
        if(!chatUser.locallySaved && !chatUser.isSearchedUser)
            binding.viewChatHeader.imageAddContact.show()
        viewModel.canScroll(false)
        binding.viewChatBtm.edtMsg.setKeyBoardInputCallbackListener(this)
        setDataInView()
        subscribeObservers()

        lifecycleScope.launch {
            viewModel.getMessagesByChatUserId(chatUserId).collect { mMessagesList ->
                if(mMessagesList.isEmpty())
                    return@collect
                messageList = mMessagesList as MutableList<Message>
                AdChat.messageList = messageList
                adChat.submitList(mMessagesList)
                //scroll to last items in recycler (recent messages)
                if (messageList.isNotEmpty()) {
                    if (viewModel.getCanScroll())  //scroll only if new message arrived
                        binding.listMessage.smoothScrollToPos(messageList.lastIndex)
                    else
                        viewModel.canScroll(true)
                }
            }
        }
    }

    private fun setListeners() {
        binding.viewChatBtm.lottieSend.setOnClickListener {
            sendMessage()
        }
        binding.viewChatHeader.viewBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.viewChatHeader.imageAddContact.setOnClickListener {
            if (Utils.askContactPermission(this))
                openSaveIntent()
        }

    }

    private fun setDataInView() {
        try {
            fromUser = preference.getUserProfile()!!
            localUserId=fromUser.uId!!
            manager= LinearLayoutManager(context)
            binding.listMessage.apply {
                manager.stackFromEnd=true
                layoutManager=manager
                setHasFixedSize(true)
                isNestedScrollingEnabled=false
                itemAnimator = null
            }
            binding.listMessage.adapter = adChat
            adChat.addRestorePolicy()
            viewModel.setChatUser(chatUser)
            toUser=chatUser.user
            chatUserId=toUser.uId!!
            binding.chatUser = chatUser
            binding.viewChatBtm.edtMsg.addTextChangedListener(msgTxtChangeListener)
            binding.viewChatBtm.lottieSend.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {


                }

                override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                    super.onAnimationEnd(animation, isReverse)
                }

                override fun onAnimationEnd(p0: Animator?) {
                    if (edtValue(binding.viewChatBtm.edtMsg).isEmpty()) {
                        binding.viewChatBtm.lottieSend.show()
                    }
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationRepeat(p0: Animator?) {


                }
            })


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun subscribeObservers() {
        //pass messages list for recycler to show
        viewModel.chatUserOnlineStatus.observe(viewLifecycleOwner, {
            Utils.setOnlineStatus(binding.viewChatHeader.txtLastSeen, it, localUserId)
        })
    }

    private fun openSaveIntent() {
        val contactIntent = Intent(ContactsContract.Intents.Insert.ACTION)
        contactIntent.type = ContactsContract.RawContacts.CONTENT_TYPE
        contactIntent
            .putExtra(ContactsContract.Intents.Insert.NAME, chatUser.user.userName)
            .putExtra(ContactsContract.Intents.Insert.PHONE, chatUser.user.mobile?.number.toString())
        startActivityForResult(contactIntent, REQ_ADD_CONTACT)
    }

    private fun sendMessage() {
        val msg = edtValue(binding.viewChatBtm.edtMsg)
        if (msg.isEmpty())
            return
        binding.viewChatBtm.lottieSend.playAnimation()
        val message = createMessage().apply {
            textMessage=TextMessage(msg)
            chatUsers= ArrayList()
        }
        viewModel.sendMessage(message)
        binding.viewChatBtm.edtMsg.setText("")
    }

    private fun createMessage(): Message {
        return Message(
            System.currentTimeMillis(),
            from = preference.getUid().toString(),
            chatUserId=chatUserId,
            to = toUser.uId!!, senderName = fromUser.userName,
            senderImage = fromUser.image
        )
    }

    private val msgTxtChangeListener=object : TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            viewModel.sendTyping(binding.viewChatBtm.edtMsg.trim())
            if(binding.viewChatBtm.lottieSend.isAnimating)
                return
            if(s.isNullOrBlank()) {
                binding.viewChatBtm.imgRecord.show()
                binding.viewChatBtm.lottieSend.hide()
            }
            else{
                binding.viewChatBtm.lottieSend.show()
                binding.viewChatBtm.imgRecord.hide()
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }

    override fun onItemClicked(v: View, position: Int) {
        val message=messageList.get(position)
        if (message.type=="image" && message.imageMessage!!.imageType=="image") {
            binding.fullSizeImageView.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_ADD_CONTACT){
            if (resultCode == Activity.RESULT_OK) {
                binding.viewChatHeader.imageAddContact.gone()
                val contacts=UserUtils.fetchContacts(requireContext())
                val savedName=contacts.firstOrNull { it.email==chatUser.user.email }
                savedName?.let {
                    binding.viewChatHeader.txtLocalName.text=it.name
                    chatUser.localName=it.name
                    chatUser.locallySaved=true
                    viewModel.insertUser(chatUser)
                }
            }else if (resultCode == Activity.RESULT_CANCELED) {
                Timber.v("Cancelled Added Contact")
            }
        }
    }

    override fun onCommitContent(inputContentInfo: InputContentInfoCompat?,
        flags: Int,
        opts: Bundle?) {
    }

    override fun onResume() {
        viewModel.setSeenAllMessage()
        preference.setCurrentUser(chatUserId)
        viewModel.sendCachedTxtMesssages()
        Utils.removeNotification(requireContext())
        super.onResume()
    }

    override fun onDestroy() {
        Utils.closeKeyBoard(requireActivity())
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        preference.clearCurrentUser()
    }


    companion object{
        private const val REQ_ADD_CONTACT=22
    }
}

