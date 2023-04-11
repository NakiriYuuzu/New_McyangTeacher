package tw.edu.teachermcyang.activity.main.home.view_model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import tw.edu.teachermcyang.activity.main.home.model.HomeDto

class HomeViewModel: ViewModel() {
    var homeObserver: MutableLiveData<ArrayList<HomeDto>> = MutableLiveData()

    fun setHomeList(homeList: ArrayList<HomeDto>) {
        homeObserver.value = homeList
    }

    fun getHomeList(): ArrayList<HomeDto>? {
        return homeObserver.value
    }
}