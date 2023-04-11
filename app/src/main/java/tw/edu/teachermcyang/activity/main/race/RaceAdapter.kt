package tw.edu.teachermcyang.activity.main.race

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import tw.edu.teachermcyang.R
import tw.edu.teachermcyang.databinding.RylayoutListStatusBinding

class RaceAdapter(
    private val onItemClickListener: OnItemClickListener
    ) : RecyclerView.Adapter<RaceAdapter.RaceViewHolder>() {

    class RaceViewHolder(
        val binding: RylayoutListStatusBinding,
        private val onItemClickListener: OnItemClickListener
        ) : RecyclerView.ViewHolder(binding.root) {
            init {
                binding.eventButton.setOnClickListener {
                    onItemClickListener.onItemClick(adapterPosition)
                }
            }
        }

    private val differCallback = object : DiffUtil.ItemCallback<RaceDto>() {
        override fun areItemsTheSame(oldItem: RaceDto, newItem: RaceDto): Boolean {
            return oldItem.rlId == newItem.rlId
        }

        override fun areContentsTheSame(oldItem: RaceDto, newItem: RaceDto): Boolean {
            if (oldItem.rlId != newItem.rlId) return false
            if (oldItem.sName != newItem.sName) return false
            if (oldItem.answer != newItem.answer) return false
            if (oldItem.studentID != newItem.studentID) return false
            return true
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RaceViewHolder {
        return RaceViewHolder(
            RylayoutListStatusBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onItemClickListener
        )
    }

    override fun onBindViewHolder(holder: RaceViewHolder, position: Int) {
        val raceList = differ.currentList[position]
        holder.binding.apply {
            if (raceList.sName.length < 2) iconText.text = raceList.sName
            else iconText.text = raceList.sName.substring(raceList.sName.length - 2)
            when (raceList.answer) {
                "0" -> {
                    eventText.text = holder.itemView.context.getString(R.string.race_text_Answer_0)
                }
                "1" -> {
                    eventText.text = holder.itemView.context.getString(R.string.race_text_Answer_1)
                    eventText.setTextColor(Color.parseColor("#44d56c"))
                    eventButton.strokeColor = Color.parseColor("#44d56c")
                }
                "99" -> {
                    eventText.text = holder.itemView.context.getString(R.string.race_text_Answer_99)
                    eventText.setTextColor(Color.parseColor("#f96767"))
                    eventButton.strokeColor = Color.parseColor("#f96767")
                }
            }
            textName.text = raceList.sName
            textId.text = raceList.studentID
        }
    }


    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}