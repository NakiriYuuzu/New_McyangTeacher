package tw.edu.teachermcyang.activity.main.course.view_model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import tw.edu.teachermcyang.activity.main.course.model.Course

class CourseViewModel: ViewModel() {
    var courseObserver: MutableLiveData<ArrayList<Course>> = MutableLiveData()
    fun setCourseList(courseList: ArrayList<Course>) {
        courseObserver.value = courseList
    }
}