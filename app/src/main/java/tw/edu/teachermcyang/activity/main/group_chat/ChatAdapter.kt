package tw.edu.teachermcyang.activity.main.group_chat

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import tw.edu.teachermcyang.databinding.ItemContainerReceivedMessageBinding
import tw.edu.teachermcyang.databinding.ItemContainerSendMessageBinding
import tw.edu.teachermcyang.yuuzu_lib.SharedData

class ChatAdapter(activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var sharedData: SharedData = SharedData(activity)

    inner class SendViewHolder(val binding: ItemContainerSendMessageBinding) : RecyclerView.ViewHolder(binding.root)
    inner class ReceiveViewHolder(val binding: ItemContainerReceivedMessageBinding) : RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<ChatDto>() {
        override fun areItemsTheSame(oldItem: ChatDto, newItem: ChatDto): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ChatDto, newItem: ChatDto): Boolean {
            if (oldItem.user != newItem.user) return false
            if (oldItem.message != newItem.message) return false
            if (oldItem.time != newItem.time) return false
            // if (oldItem.current != newItem.current) return false
            return true
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    private val send = 1
    private val received = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == send) {
            return SendViewHolder(
                ItemContainerSendMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            return ReceiveViewHolder(
                ItemContainerReceivedMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chat = differ.currentList[position]
        if (holder is SendViewHolder) {
            holder.binding.apply {
                chatSendMessage.text = chat.message
                chatSendMessageTime.text = chat.time
            }
        }
        if (holder is ReceiveViewHolder) {
            holder.binding.apply {
                chatReceivedMessage.text = chat.message
                chatReceivedMessageTime.text = chat.time
                chatReceivedMessageUserName.text = chat.user
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (differ.currentList[position].user == sharedData.getCourseName()) send else received
    }
}