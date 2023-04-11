package tw.edu.teachermcyang.activity.main.group_chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import tw.edu.teachermcyang.databinding.RvlayoutType01Binding

class ChatRoomAdapter(val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder>(){

    class ChatRoomViewHolder(val binding: RvlayoutType01Binding, onItemClickListener: OnItemClickListener) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.rvlayoutBtnSubmit.setOnClickListener {
                onItemClickListener.onItemClick(adapterPosition)
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<ChatRoomDto>() {
        override fun areItemsTheSame(oldItem: ChatRoomDto, newItem: ChatRoomDto): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ChatRoomDto, newItem: ChatRoomDto): Boolean {
            if (oldItem.groupChat_id != newItem.groupChat_id) return false
            if (oldItem.teamDesc_id != newItem.teamDesc_id) return false
            if (oldItem.teamLeader_id != newItem.teamLeader_id) return false
            if (oldItem.chatTitle != newItem.chatTitle) return false
            if (oldItem.c_id != newItem.c_id) return false
            return true
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        return ChatRoomViewHolder(
            RvlayoutType01Binding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onItemClickListener
        )
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.binding.apply {
            val chatRoom = differ.currentList[position]
            if (chatRoom.groupChat_id.isEmpty()) {
                rvlayoutTextMain.text = chatRoom.chatTitle
                rvlayoutTextSub.text = chatRoom.crtTime
            } else {
                rvlayoutTextMain.text = chatRoom.s_name
                rvlayoutTextSub.text = chatRoom.crtTime
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}