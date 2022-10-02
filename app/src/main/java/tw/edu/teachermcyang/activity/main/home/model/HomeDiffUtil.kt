package tw.edu.teachermcyang.activity.main.home.model

import androidx.recyclerview.widget.DiffUtil

class HomeDiffUtil(
    private val oldList: ArrayList<HomeDto>,
    private val newList: ArrayList<HomeDto>
): DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].S_id == newList[newItemPosition].S_id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList[oldItemPosition].S_id != newList[newItemPosition].S_id -> false
            oldList[oldItemPosition].S_name != newList[newItemPosition].S_name -> false
            oldList[oldItemPosition].StudentID != newList[newItemPosition].StudentID -> false
            else -> true
        }
    }

}