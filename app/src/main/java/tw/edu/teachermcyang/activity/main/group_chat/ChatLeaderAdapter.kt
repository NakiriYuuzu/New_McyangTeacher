package tw.edu.teachermcyang.activity.main.group_chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import tw.edu.teachermcyang.databinding.RvlayoutType01Binding

class ChatLeaderAdapter(val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<ChatLeaderAdapter.ChatLeaderViewHolder>() {

    class ChatLeaderViewHolder(val binding: RvlayoutType01Binding, onItemClickListener: OnItemClickListener) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.rvlayoutBtnSubmit.setOnClickListener {
                onItemClickListener.onItemClick(adapterPosition)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private val differCallback = object : DiffUtil.ItemCallback<ChatLeaderDto>() {
        override fun areItemsTheSame(oldItem: ChatLeaderDto, newItem: ChatLeaderDto): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ChatLeaderDto, newItem: ChatLeaderDto): Boolean {
            if (oldItem.groupChat_id != newItem.groupChat_id) return false
            if (oldItem.leader_name != newItem.leader_name) return false
            if (oldItem.chat_Title != newItem.chat_Title) return false
            return true
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatLeaderViewHolder {
        return ChatLeaderViewHolder(
            RvlayoutType01Binding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onItemClickListener
        )
    }

    override fun onBindViewHolder(holder: ChatLeaderViewHolder, position: Int) {
        holder.binding.apply {
            val chatLeader = differ.currentList[position]
            val leader = "[隊長] " + chatLeader.leader_name
            rvlayoutTextMain.text = leader
            rvlayoutTextSub.text = chatLeader.chat_Title
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}