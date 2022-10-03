package tw.edu.teachermcyang.activity.main.group_create

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import tw.edu.teachermcyang.databinding.RylayoutListStatusBinding

class LeaderAdapter(private val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<LeaderAdapter.LeaderViewHolder>() {

    inner class LeaderViewHolder(
        val binding: RylayoutListStatusBinding,
        private val onItemClickListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.eventButton.setOnClickListener {
                onItemClickListener.onItemClick(adapterPosition)
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<LeaderDto>() {
        override fun areItemsTheSame(oldItem: LeaderDto, newItem: LeaderDto): Boolean {
            return oldItem.leaderID == newItem.leaderID
        }

        override fun areContentsTheSame(oldItem: LeaderDto, newItem: LeaderDto): Boolean {
            if (oldItem.leaderID != newItem.leaderID) return false
            if (oldItem.studentID != newItem.studentID) return false
            if (oldItem.studentName != newItem.studentName) return false
            if (oldItem.isPicked != newItem.isPicked) return false
            if (oldItem.sID != newItem.sID) return false
            return true
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderViewHolder {
        return LeaderViewHolder(
            RylayoutListStatusBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onItemClickListener
        )
    }

    override fun onBindViewHolder(holder: LeaderViewHolder, position: Int) {
        val leaderList = differ.currentList[position]
        holder.binding.apply {
            if (leaderList.studentName.length < 2) iconText.text = leaderList.studentName
            else iconText.text = leaderList.studentName.substring(leaderList.studentName.length - 2)

            when (leaderList.isPicked) {
                "0" -> {
                    eventText.text = "未選擇"
                }

                "1" -> {
                    eventText.text = "已選擇"
                    eventText.setTextColor(Color.parseColor("#44d56c"))
                    eventButton.strokeColor = Color.parseColor("#44d56c")
                }
            }

            textName.text = leaderList.studentName
            textId.text = leaderList.studentID
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}