package tw.edu.teachermcyang.activity.main.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import tw.edu.teachermcyang.activity.main.home.model.HomeDiffUtil
import tw.edu.teachermcyang.activity.main.home.model.HomeDto
import tw.edu.teachermcyang.activity.main.sign.model.SignDto
import tw.edu.teachermcyang.databinding.RylayoutTypeListBinding

class HomeAdapter(
    private val itemClickListener:OnItemClickListener
    ): RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    private var oldHomeList: ArrayList<HomeDto> = ArrayList()

    class HomeViewHolder(val binding: RylayoutTypeListBinding, private val itemClickListener: OnItemClickListener): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.listStatusBtn.setOnClickListener {
                itemClickListener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(
            RylayoutTypeListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            itemClickListener
        )
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val home = oldHomeList[position]

        holder.binding.iconText.text = "提問"
        holder.binding.textName.text = home.S_name
        holder.binding.textId.text = home.StudentID
    }

    override fun getItemCount(): Int {
        return oldHomeList.size
    }

    fun setData(newHomeList: ArrayList<HomeDto>) {
        val diffUtil = HomeDiffUtil(oldHomeList, newHomeList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldHomeList = newHomeList
        diffResult.dispatchUpdatesTo(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(homeList: ArrayList<HomeDto>) {
        this.oldHomeList = homeList
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}