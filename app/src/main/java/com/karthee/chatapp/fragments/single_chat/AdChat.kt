package com.karthee.chatapp.fragments.single_chat

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.karthee.chatapp.R
import com.karthee.chatapp.databinding.*
import com.karthee.chatapp.db.data.Message
import com.karthee.chatapp.utils.*
import com.karthee.chatapp.utils.Events.EventAudioMsg
import com.karthee.chatapp.utils.Events.EventUpdateRecycleItem
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.io.IOException
import java.util.ArrayList
import kotlin.properties.Delegates

class AdChat(private val context: Context, private val msgClickListener: ItemClickListener) :
    ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallbackMessages()) {

    private val preference = MPreference(context)

    companion object {
        private const val TYPE_TXT_SENT = 0
        private const val TYPE_TXT_RECEIVED = 1
        lateinit var messageList: MutableList<Message>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TXT_SENT -> {
                val binding = RowSentMessageBinding.inflate(layoutInflater, parent, false)
                TxtSentVHolder(binding)
            }
            else -> {
                val binding = RowReceiveMessageBinding.inflate(layoutInflater, parent, false)
                TxtReceiveVHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TxtSentVHolder ->
                holder.bind(context, getItem(position))
            is TxtReceiveVHolder ->
                holder.bind(context, getItem(position))

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

    class TxtSentVHolder(val binding: RowSentMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context, item: Message) {
            binding.message = item
            binding.messageList = messageList as ArrayList<Message>
            if (bindingAdapterPosition > 0) {
                val message = messageList[bindingAdapterPosition - 1]
                if (message.from == item.from)
                    binding.txtMsg.setBackgroundResource(R.drawable.shape_send_msg_corned)
            }
            binding.executePendingBindings()
        }
    }

    class TxtReceiveVHolder(val binding: RowReceiveMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context, item: Message) {
            binding.message = item
            if (bindingAdapterPosition > 0) {
                val message = messageList[bindingAdapterPosition - 1]
                if (message.from == item.from)
                    binding.txtMsg.setBackgroundResource(R.drawable.shape_receive_msg_corned)
            }
            binding.executePendingBindings()
        }
    }


}

class DiffCallbackMessages : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.createdAt == newItem.createdAt
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }
}
