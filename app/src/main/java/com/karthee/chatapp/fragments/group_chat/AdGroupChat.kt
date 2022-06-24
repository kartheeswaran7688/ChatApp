package com.karthee.chatapp.fragments.group_chat

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.karthee.chatapp.R
import com.karthee.chatapp.databinding.*
import com.karthee.chatapp.db.data.ChatUser
import com.karthee.chatapp.db.data.GroupMessage
import com.karthee.chatapp.fragments.single_chat.AdChat
import com.karthee.chatapp.utils.Events.EventAudioMsg
import com.karthee.chatapp.utils.ItemClickListener
import com.karthee.chatapp.utils.MPreference
import com.karthee.chatapp.utils.gone
import com.karthee.chatapp.utils.show
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.io.IOException

class AdGroupChat(
    private val context: Context,
    private val msgClickListener: ItemClickListener
) :
    ListAdapter<GroupMessage, RecyclerView.ViewHolder>(DiffCallbackMessages()) {

    private val preference = MPreference(context)

    companion object {
        private const val TYPE_TXT_SENT = 0
        private const val TYPE_TXT_RECEIVED = 1

        lateinit var messageList: MutableList<GroupMessage>
        lateinit var chatUserList: MutableList<ChatUser>

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TXT_SENT -> {
                val binding = RowGroupTxtSentBinding.inflate(layoutInflater, parent, false)
                TxtSentMsgHolder(binding)
            }
            else -> {
                val binding = RowGrpTxtReceiveBinding.inflate(layoutInflater, parent, false)
                TxtReceivedMsgHolder(binding)
            }

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TxtSentMsgHolder ->
                holder.bind(getItem(position))
            is TxtReceivedMsgHolder ->
                holder.bind(getItem(position))

        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        val fromMe = message.from == preference.getUid()
        if (fromMe && message.type == "text")
            return TYPE_TXT_SENT
        else if (!fromMe && message.type == "text")
            return TYPE_TXT_RECEIVED

        return super.getItemViewType(position)
    }


    class TxtSentMsgHolder(val binding: RowGroupTxtSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GroupMessage) {
            binding.message = item
            if (bindingAdapterPosition > 0) {
                val message = messageList[bindingAdapterPosition - 1]
                if (message.from == item.from) {
                    binding.txtMsg.setBackgroundResource(R.drawable.shape_send_msg_corned)
                }
            }
            binding.executePendingBindings()
        }
    }

    class TxtReceivedMsgHolder(val binding: RowGrpTxtReceiveBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GroupMessage) {
            binding.message = item
            binding.chatUsers = chatUserList.toTypedArray()
            if (bindingAdapterPosition > 0) {
                val lastMsg = messageList[bindingAdapterPosition - 1]
                if (lastMsg.from == item.from) {
                    binding.apply {
                        viewDetail.gone()
                    }
                    binding.viewMsgHolder.setBackgroundResource(R.drawable.shape_receive_msg_corned)
                }
            }
            binding.executePendingBindings()
        }
    }


    class DiffCallbackMessages : DiffUtil.ItemCallback<GroupMessage>() {
        override fun areItemsTheSame(oldItem: GroupMessage, newItem: GroupMessage): Boolean {
            return oldItem.createdAt == newItem.createdAt
        }

        override fun areContentsTheSame(oldItem: GroupMessage, newItem: GroupMessage): Boolean {
            return oldItem == newItem
        }
    }
}
