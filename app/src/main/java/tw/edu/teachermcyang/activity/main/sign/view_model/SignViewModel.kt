package tw.edu.teachermcyang.activity.main.sign.view_model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import tw.edu.teachermcyang.activity.main.sign.model.SignDto

class SignViewModel: ViewModel() {
    var signObserver: MutableLiveData<ArrayList<SignDto>> = MutableLiveData()
    fun setSignList(signList: ArrayList<SignDto>) {
        signObserver.value = signList

    }

    fun getSignList(): ArrayList<SignDto>? {
        return signObserver.value
    }
}