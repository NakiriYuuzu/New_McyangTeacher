package tw.edu.teachermcyang.activity.main.sign.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tw.edu.teachermcyang.activity.main.sign.model.SignDto
import tw.edu.teachermcyang.databinding.RylayoutListStatusBinding
import kotlin.collections.ArrayList

class SignAdapter(var signList: ArrayList<SignDto>) : RecyclerView.Adapter<SignAdapter.SignViewHolder>() {

    class SignViewHolder(val binding: RylayoutListStatusBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SignViewHolder {
        return SignViewHolder(
            RylayoutListStatusBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    override fun onBindViewHolder(holder: SignViewHolder, position: Int) {
        val sign = signList[position]

        holder.binding.textName.text = sign.S_name

        if (sign.S_name.length > 2) {
            holder.binding.iconText.text = sign.S_name.substring(2)
        } else {
            holder.binding.iconText.text = sign.S_name
        }

        holder.binding.textId.text = sign.StudentID

        if (sign.status) {
            holder.binding.eventText.text = "已簽到"
            holder.binding.eventText.setTextColor(Color.parseColor("#44d56c"))
            holder.binding.eventButton.strokeColor = Color.parseColor("#44d56c")
        } else {
            holder.binding.eventText.text = "未簽到"
        }
    }

    override fun getItemCount(): Int {
        return signList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(signList: ArrayList<SignDto>) {
        this.signList = signList
        notifyDataSetChanged()
    }
}