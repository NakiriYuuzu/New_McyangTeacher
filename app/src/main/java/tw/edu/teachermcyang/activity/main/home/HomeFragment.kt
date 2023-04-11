package tw.edu.teachermcyang.activity.main.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.*
import tw.edu.teachermcyang.AppConfig
import tw.edu.teachermcyang.R
import tw.edu.teachermcyang.activity.main.home.adapter.HomeAdapter
import tw.edu.teachermcyang.activity.main.sign.SignActivity
import tw.edu.teachermcyang.yuuzu_lib.DialogHelper
import tw.edu.teachermcyang.yuuzu_lib.SharedData

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var sharedData: SharedData
    private lateinit var dialogHelper: DialogHelper

    private lateinit var homeAdapter: HomeAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvName: MaterialTextView
    private lateinit var tvPeople: MaterialTextView
    private lateinit var tvCourse: MaterialTextView
    private lateinit var btnSign: MaterialButton
    private lateinit var btnLogout: MaterialCardView

    private lateinit var homeList: ArrayList<String>

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: initView()
        tvName = view.findViewById(R.id.home_tvName)
        tvPeople = view.findViewById(R.id.home_tvPeople)
        tvCourse = view.findViewById(R.id.home_tvCourse)
        btnSign = view.findViewById(R.id.home_btnSign)
        btnLogout = view.findViewById(R.id.home_btnLogout)
        recyclerView = view.findViewById(R.id.home_recyclerView)

        sharedData = SharedData(requireActivity())
        dialogHelper = DialogHelper(requireActivity())

        homeList = ArrayList()

        // TODO: setView()
        tvName.text = sharedData.getName() + getString(R.string.home_text_SubName)
        refreshPage()

        //TODO: Function
        initButton()
    }

    @SuppressLint("SetTextI18n")
    private fun refreshPage() {
        Log.e(AppConfig.TAG, "refreshPage: $homeList")

        if (sharedData.getCourseName().isNotBlank() && sharedData.getCourseName() != "null") {
            tvCourse.text = getString(R.string.sign_text_CourseTitle) + sharedData.getCourseName()
            tvPeople.text = sharedData.getSignPeople()

            btnSign.text = getString(R.string.home_text_btnLeave)
            btnSign.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.dark_red_secondary_color
                )
            )
        }

        initRecyclerView()
    }

    private fun initRecyclerView() {
        homeAdapter = HomeAdapter()

        recyclerView.apply {
            val linearLayoutManager = LinearLayoutManager(requireContext())
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager = linearLayoutManager
            hasFixedSize()
            adapter = homeAdapter
        }

        homeList = sharedData.get<ArrayList<String>>(sharedData.askingList) ?: ArrayList()
        homeAdapter.differ.submitList(homeList)
    }

    private fun initButton() {
        btnSign.setOnClickListener {
            if (sharedData.getSignID().isNotBlank() && sharedData.getSignID() != "null") {
                dialogHelper.sweetBtnDialog(
                    title = getString(R.string.home_text_quitSign_Title),
                    message = getString(R.string.home_text_quitSign_Message),
                    positiveText = getString(R.string.home_text_quitSign_Quit),
                    negativeText = getString(R.string.home_text_quitSign_Continue),
                    cancelable = true,
                    status = SweetAlertDialog.WARNING_TYPE,
                    sweetDialogListener = object : DialogHelper.SweetDialogListener {
                        override fun onPositiveClick(dialog: SweetAlertDialog) {
                            sharedData.quitCourse()

                            tvCourse.text = getString(R.string.home_text_courseNowNone)
                            tvPeople.text = getString(R.string.sign_text_CoursePeople)
                            btnSign.text = getString(R.string.home_text_btnJoin)
                            btnSign.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.dark_green_secondary_color))

                            dialogHelper.sweetDialog(getString(R.string.alert_sweetAlert_Success), SweetAlertDialog.SUCCESS_TYPE, null)
                            dialog.dismiss()
                        }

                        override fun onNegativeClick(dialog: SweetAlertDialog) {
                            requireActivity().startActivity(Intent(requireActivity(), SignActivity::class.java))
                            dialog.dismiss()
                        }
                    }
                )
            } else {
                requireActivity().startActivity(Intent(requireActivity(), SignActivity::class.java))
            }
        }

        btnLogout.setOnClickListener {
            dialogHelper.sweetDialog("登出成功！", SweetAlertDialog.SUCCESS_TYPE, object :
                DialogHelper.SweetDialogPositiveListener {
                override fun onPositiveClick(dialog: SweetAlertDialog) {
                    sharedData.logout()
                    requireActivity().finish()
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPage()
    }
}