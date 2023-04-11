package tw.edu.teachermcyang.activity.main.course

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.VolleyError
import com.facebook.shimmer.ShimmerFrameLayout
import org.json.JSONArray
import tw.edu.teachermcyang.AppConfig
import tw.edu.teachermcyang.activity.main.course.adapter.CourseAdapter
import tw.edu.teachermcyang.activity.main.course.model.Course
import tw.edu.teachermcyang.R
import tw.edu.teachermcyang.yuuzu_lib.DialogHelper
import tw.edu.teachermcyang.yuuzu_lib.SharedData
import tw.edu.teachermcyang.yuuzu_lib.YuuzuApi

class CourseFragment : Fragment(R.layout.fragment_course) {

    private var shimmerStatus = ""

    private lateinit var recyclerView: RecyclerView
    private lateinit var shimmer: ShimmerFrameLayout

    private lateinit var courseList: ArrayList<Course>
    private lateinit var courseAdapter: CourseAdapter

    private lateinit var sharedData: SharedData
    private lateinit var yuuzuApi: YuuzuApi
    private lateinit var dialogHelper: DialogHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: InitView()
        recyclerView = view.findViewById(R.id.course_recyclerView)
        shimmer = view.findViewById(R.id.course_ShimmerLayout)

        courseList = ArrayList()

        dialogHelper = DialogHelper(requireActivity())
        sharedData = SharedData(requireActivity())
        yuuzuApi = YuuzuApi(requireActivity())

        // TODO: Function()
        getCourse()
        showShimmer()
        initRecyclerView()
        Handler(Looper.getMainLooper()).postDelayed({
            if (shimmerStatus.isNotBlank()) {
                dialogHelper.showDialog(getString(R.string.course_text_fail2Load), "")

            } else {
                if (courseList.size > 0) {
                    hideShimmer()
                } else {
                    dialogHelper.showDialog(getString(R.string.course_text_fail2Load), "")
                }
            }
        }, 1500)
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        courseAdapter = CourseAdapter(courseList)
        recyclerView.adapter = courseAdapter
    }

    private fun getCourse() {
        yuuzuApi.api(Request.Method.POST, AppConfig.URL_LIST_COURSE, object :
            YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                val jsonArray = JSONArray(data)
                for (i in 0 until jsonArray.length()) {
                    val course = jsonArray.getJSONObject(i)
                    courseList.add(
                        Course(
                            course.getString("C_name"),
                            course.getInt("C_id"),
                            course.getString("T_name")
                        )
                    )
                }
            }

            override fun onError(error: VolleyError) {
                if (error.networkResponse != null) {
                    if (error.networkResponse.statusCode == 400) {
                        shimmerStatus = "400"
                        dialogHelper.showDialog(getString(R.string.course_text_fail2Load404), "")
                    }
                } else {
                    shimmerStatus = "error"
                }
            }

            override val params: Map<String, String>
                get() = mapOf(
                    AppConfig.API_TID to sharedData.getID()
                )
        })
    }

    private fun showShimmer() {
        if (!shimmer.isShimmerStarted) {
            shimmer.startShimmer()
            shimmer.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
    }

    private fun hideShimmer() {
        if (shimmer.isShimmerStarted) {
            shimmer.stopShimmer()
            shimmer.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
        showShimmer()
    }

    override fun onPause() {
        super.onPause()
        hideShimmer()
    }
}