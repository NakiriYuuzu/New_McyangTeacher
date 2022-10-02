package tw.edu.teachermcyang.activity.main.group_chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import tw.edu.teachermcyang.databinding.RvlayoutType01Binding

class TeamDescAdapter(val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<TeamDescAdapter.TeamDescViewHolder>() {

    class TeamDescViewHolder(val binding: RvlayoutType01Binding, onItemClickListener: OnItemClickListener) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.rvlayoutBtnSubmit.setOnClickListener {
                onItemClickListener.onItemClick(adapterPosition)
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<TeamDescDto>() {
        override fun areItemsTheSame(oldItem: TeamDescDto, newItem: TeamDescDto): Boolean {
            return oldItem.teamDesc_id == newItem.teamDesc_id
        }

        override fun areContentsTheSame(oldItem: TeamDescDto, newItem: TeamDescDto): Boolean {
            if (oldItem.teamDesc_id != newItem.teamDesc_id) return false
            if (oldItem.teamDesc_doc != newItem.teamDesc_doc) return false
            if (oldItem.time != newItem.time) return false
            return true
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamDescViewHolder {
        return TeamDescViewHolder(
            RvlayoutType01Binding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onItemClickListener
        )
    }

    override fun onBindViewHolder(holder: TeamDescViewHolder, position: Int) {
        holder.binding.apply {
            val teamDesc = differ.currentList[position]
            rvlayoutTextMain.text = teamDesc.teamDesc_doc
            rvlayoutTextSub.text = teamDesc.time
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}