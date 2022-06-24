package com.karthee.chatapp.fragments.group_chat

import android.animation.Animator
import android.os.Bundle
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
import com.karthee.chatapp.databinding.FGroupChatBinding
import com.karthee.chatapp.db.daos.GroupDao
import com.karthee.chatapp.db.data.Group
import com.karthee.chatapp.db.data.GroupMessage
import com.karthee.chatapp.db.data.TextMessage
import com.karthee.chatapp.models.UserProfile
import com.karthee.chatapp.utils.*
import com.karthee.chatapp.views.CustomEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FGroupChat : Fragment(), ItemClickListener, CustomEditText.KeyBoardInputCallbackListener {

    @Inject
    lateinit var groupDao: GroupDao

    @Inject
    lateinit var preference: MPreference

    private val viewModel: GroupChatViewModel by viewModels()

    private lateinit var binding: FGroupChatBinding

    val args by navArgs<FGroupChatArgs>()

    lateinit var group: Group

    private var messageList = mutableListOf<GroupMessage>()

    private lateinit var manager: LinearLayoutManager

    private lateinit var localUserId: String

    private lateinit var fromUser: UserProfile

    private var msgPostponed=false

    private val adChat: AdGroupChat by lazy {
        AdGroupChat(requireContext(), this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = FGroupChatBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        group = args.group
        binding.group = group
        setViewListeners()
        binding.viewChatBtm.edtMsg.setKeyBoardInputCallbackListener(this)
        UserUtils.setUnReadCountGroup(groupDao, group)
        setDataInView()
        subscribeObservers()

        lifecycleScope.launch {
            viewModel.getGroupMessages(group.id).collect { message ->
                if(message.isEmpty())
                    return@collect
                messageList = message as MutableList<GroupMessage>

                AdGroupChat.messageList = messageList
                adChat.submitList(messageList)
                Timber.v("Message list ${messageList.last()}")
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

    private fun setViewListeners() {
        binding.viewChatBtm.lottieSend.setOnClickListener {
            sendMessage()
        }
        binding.viewChatHeader.viewBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun sendMessage() {
        val msg = binding.viewChatBtm.edtMsg.text?.trim().toString()
        if (msg.isEmpty())
            return
        binding.viewChatBtm.lottieSend.playAnimation()
        val messageData = TextMessage(msg)
        val message = createMessage()
        message.textMessage = messageData
        viewModel.sendMessage(message)
        binding.viewChatBtm.edtMsg.setText("")
    }

    private fun createMessage(): GroupMessage {
        val toUsers = group.members?.map { it.id } as ArrayList
        val groupSize = group.members!!.size
        val statusList = ArrayList<Int>()
        val deliveryTimeList = ArrayList<Long>()
        for (index in 0 until groupSize) {
            statusList.add(0)
            deliveryTimeList.add(0L)
        }
        return GroupMessage(
            System.currentTimeMillis(), group.id, from = localUserId,
            to = toUsers, fromUser.userName, fromUser.image, statusList, deliveryTimeList,
            deliveryTimeList
        )
    }

    private fun subscribeObservers() {
        viewModel.getChatUsers().observe(viewLifecycleOwner, { chatUsers ->
            AdGroupChat.chatUserList = chatUsers.toMutableList()
        })

        viewModel.typingUsers.observe(viewLifecycleOwner, { typingUser ->
            if (typingUser.isEmpty())
                BindingAdapters.setMemberNames(binding.viewChatHeader.txtMembers, group)
            else
                binding.viewChatHeader.txtMembers.text = typingUser
        })
    }

    private fun setDataInView() {
        fromUser = preference.getUserProfile()!!
        localUserId = fromUser.uId!!
        manager = LinearLayoutManager(context)
        binding.listMessage.apply {
            manager.stackFromEnd = true
            layoutManager = manager
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            itemAnimator = null
        }
        binding.listMessage.adapter = adChat
        adChat.addRestorePolicy()
        viewModel.setGroup(group)
        binding.viewChatBtm.edtMsg.addTextChangedListener(msgTxtChangeListener)
        binding.viewChatBtm.lottieSend.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {


            }

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                super.onAnimationEnd(animation, isReverse)
            }

            override fun onAnimationEnd(p0: Animator?) {
                if (Utils.edtValue(binding.viewChatBtm.edtMsg).isEmpty()) {
                    //binding.viewChatBtm.imgRecord.show()
                    binding.viewChatBtm.lottieSend.show()
                }
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationRepeat(p0: Animator?) {


            }
        })

        binding.lottieVoice.addAnimatorListener(object : Animator.AnimatorListener{
            override fun onAnimationStart(p0: Animator?) {


            }

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                super.onAnimationEnd(animation, isReverse)
            }

            override fun onAnimationEnd(p0: Animator?) {
                binding.viewChatBtm.imgRecord.show()
                binding.lottieVoice.gone()
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationRepeat(p0: Animator?) {


            }
        })
    }

    private val msgTxtChangeListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            viewModel.sendTyping(binding.viewChatBtm.edtMsg.trim())
            if(binding.viewChatBtm.lottieSend.isAnimating)
                return
            binding.viewChatBtm.lottieSend.show()
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }

    override fun onItemClicked(v: View, position: Int) {
        binding.fullSizeImageView.show()
    }

    override fun onResume() {
        preference.setCurrentGroup(group.id)
        viewModel.sendCachedTxtMesssages()
        Utils.removeNotification(requireContext())
        super.onResume()
    }

    override fun onCommitContent(
        inputContentInfo: InputContentInfoCompat?,
        flags: Int,
        opts: Bundle?) {
    }

    override fun onStop() {
        super.onStop()
        preference.clearCurrentGroup()
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.closeKeyBoard(requireActivity())
    }

}