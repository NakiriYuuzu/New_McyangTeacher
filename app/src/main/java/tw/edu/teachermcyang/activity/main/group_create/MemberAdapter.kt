package tw.edu.teachermcyang.activity.main.group_create

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import tw.edu.teachermcyang.databinding.RylayoutListStatusBinding

class MemberAdapter(private val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    inner class MemberViewHolder(
        val binding: RylayoutListStatusBinding,
        private val onItemClickListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.mainButton.setOnClickListener {
                onItemClickListener.onItemClick(adapterPosition)
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<MemberDto>() {
        override fun areItemsTheSame(oldItem: MemberDto, newItem: MemberDto): Boolean {
            return oldItem.leaderID == newItem.leaderID
        }

        override fun areContentsTheSame(oldItem: MemberDto, newItem: MemberDto): Boolean {
            if (oldItem.leaderID != newItem.leaderID) return false
            if (oldItem.studentName != newItem.studentName) return false
            if (oldItem.peopleCount != newItem.peopleCount) return false
            if (oldItem.studentID != newItem.studentID) return false
            return true
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        return MemberViewHolder(
            RylayoutListStatusBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onItemClickListener
        )
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val memberList = differ.currentList[position]
        holder.binding.apply {
            if (memberList.studentName.length < 2) iconText.text = memberList.studentName
            else iconText.text = memberList.studentName.substring(memberList.studentName.length - 2)

            if (memberList.peopleCount.split("/")[0] == memberList.peopleCount.split("/")[1]) {
                eventText.setTextColor(Color.parseColor("#44d56c"))
                eventButton.strokeColor = Color.parseColor("#44d56c")
            }

            eventText.text = memberList.peopleCount
            textName.text = memberList.studentName
            textId.text = memberList.studentID
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}