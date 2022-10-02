package tw.edu.teachermcyang.activity.main.sign

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.VolleyError
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.Region
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import tw.edu.teachermcyang.AppConfig
import tw.edu.teachermcyang.R
import tw.edu.teachermcyang.activity.main.sign.adapter.SignAdapter
import tw.edu.teachermcyang.activity.main.sign.model.SignDto
import tw.edu.teachermcyang.activity.main.sign.view_model.SignViewModel
import tw.edu.teachermcyang.databinding.ActivitySignBinding
import tw.edu.teachermcyang.yuuzu_lib.*
import tw.edu.teachermcyang.yuuzu_lib.model.MessageListener
import kotlin.concurrent.thread

class SignActivity : AppCompatActivity() {

    // dropDown
    private lateinit var dropdownList: ArrayList<String>
    private lateinit var arrayAdapter: ArrayAdapter<String>

    // recyclerView
    private lateinit var signList: ArrayList<SignDto>
    private lateinit var signAdapter: SignAdapter
    private lateinit var signViewModel: SignViewModel
    private var unSign = 0
    private var sign = 0

    private var webFailed = 0
    private var isWebSocket = false

    private lateinit var binding: ActivitySignBinding

    private lateinit var yuuzuApi: YuuzuApi
    private lateinit var sharedData: SharedData
    private lateinit var dialogHelper: DialogHelper
    private lateinit var beaconController: BeaconController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initButton()
        if (sharedData.getSignID() != "null" && sharedData.getSignID().isNotBlank()) {
            resumeSign()
        } else {
            initDialogData()
        }
    }

    private fun resumeSign() {
        yuuzuApi.api(Request.Method.POST, AppConfig.URL_CREATE_SIGN, object :
            YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                try {
                    val jsonObject = JSONObject(data)
                    val jsonArray = jsonObject.getJSONArray("StudentList")

                    for (i in 0 until jsonArray.length()) {
                        val result = jsonArray.getJSONObject(i)
                        signList.add(
                            SignDto(
                                S_id = result.getString(AppConfig.API_SID),
                                S_name = result.getString(AppConfig.API_SNAME),
                                StudentID = result.getString("StudentID"),
                                status = false
                            )
                        )

                        // 設定人數
                        unSign = signList.size
                        val signPeople = getString(R.string.sign_text_CoursePeople) + sign + "/" + unSign
                        binding.signTextPeople.text = signPeople

                        initRecyclerView()
                        initWebSocket()
                        initBeaconCasting()

                        sharedData.saveSignPeople(getString(R.string.sign_text_CoursePeople) + sign + "/" + unSign)
                        binding.signTextPeople.text = sharedData.getSignPeople()
                    }
                } catch (e: JSONException) {
                    dialogHelper.sweetDialog(getString(R.string.alert_error_json), SweetAlertDialog.ERROR_TYPE, null)
                }
            }

            override fun onError(error: VolleyError) {
                if (error.networkResponse != null) {
                    when (error.networkResponse.statusCode) {
                        400 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_400), SweetAlertDialog.ERROR_TYPE, null) }
                        404 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_404), SweetAlertDialog.ERROR_TYPE, null) }
                        417 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_417), SweetAlertDialog.ERROR_TYPE, null) }
                        500 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_500), SweetAlertDialog.ERROR_TYPE, null) }
                    }
                }
            }

            override val params: Map<String, String>
                get() = mapOf(
                    AppConfig.API_SIGN_ID to sharedData.getSignID()
                )
        })
    }

    private fun initWebSocket() {
        WebSocketManager.init(AppConfig.WS_SIGNLIST, object : MessageListener {
            override fun onConnectSuccess() {
                Log.e(AppConfig.TAG, "onConnectSuccess: ")
            }

            override fun onConnectFailed() {
                Log.e(AppConfig.TAG, "onConnectFailed: ")
                if (!isWebSocket) { WebSocketManager.reconnect() }
                if (webFailed > 3) dialogHelper.sweetDialog(getString(R.string.alert_error_title_noServer), SweetAlertDialog.ERROR_TYPE, object :
                    DialogHelper.SweetDialogPositiveListener {
                    override fun onPositiveClick(dialog: SweetAlertDialog) {
                        dialog.dismiss()
                        finish()
                    }
                })
            }

            override fun onClose() {
                Log.e(AppConfig.TAG, "onClose: ")
                isWebSocket = true
            }

            override fun onMessage(text: String?) {
                try {
                    if (text != null) {
                        val jsonObject = JSONObject(text)
                        val signId = jsonObject.getString(AppConfig.API_SIGN_ID)

                        if (signId == sharedData.getSignID()) syncData()
                    }
                } catch (e: JSONException) {
                    dialogHelper.sweetDialog(getString(R.string.alert_error_json), SweetAlertDialog.ERROR_TYPE, null)
                }
            }
        })

        thread {
            kotlin.run {
                if (isWebSocket) return@thread
                WebSocketManager.connect()
            }
        }
    }

    private fun initBeaconCasting() {
        beaconController.broadcastBeacon(AppConfig.BEACON_UUID_SIGN, sharedData.getSignID(), sharedData.getSignID())

        Handler(Looper.getMainLooper()).postDelayed({
            beaconController.startBeaconCasting()
        }, 1000)
    }

    private fun initViewModel() {
        signViewModel = ViewModelProvider(this@SignActivity)[SignViewModel::class.java]
        signViewModel.signObserver.observe(this) {
            signAdapter.updateAdapter(signList)
        }
        signViewModel.setSignList(signList)
    }

    private fun syncData() {
        yuuzuApi.api(Request.Method.GET, AppConfig.URL_LIST_SIGN_RECORD + "?Sign_id=${sharedData.getSignID()}", object : YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                val jsonArray = JSONArray(data)

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val sid = jsonObject.getString(AppConfig.API_SID)

                    signList.forEach {
                        if (it.S_id == sid) {
                            if (!it.status) {
                                it.status = true
                                sign++
                            }
                        }
                    }
                }

                initViewModel()
                // 設定人數
                sharedData.saveSignPeople(getString(R.string.sign_text_CoursePeople) + sign + "/" + unSign)
                binding.signTextPeople.text = sharedData.getSignPeople()
            }

            override fun onError(error: VolleyError) {
                if (error.networkResponse != null) {
                    when (error.networkResponse.statusCode) {
                        500 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_500), SweetAlertDialog.ERROR_TYPE, null) }
                    }
                }
            }

            override val params: Map<String, String>
                get() = mapOf()
        })
    }

    private fun initRecyclerView() {
        signAdapter = SignAdapter(signList)
        binding.signRecyclerView.apply {
            val linearLayoutManager = LinearLayoutManager(this@SignActivity)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager = linearLayoutManager
            hasFixedSize()
            adapter = this@SignActivity.signAdapter
        }
    }

    private fun signCreate() {
        yuuzuApi.api(Request.Method.POST, AppConfig.URL_CREATE_SIGN, object : YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                try {
                    val jsonObject = JSONObject(data)
                    val jsonArray = jsonObject.getJSONArray("StudentList")
                    sharedData.saveSignID(jsonObject.getString(AppConfig.API_SIGN_ID))
                    sharedData.saveCID(jsonObject.getString(AppConfig.API_CID))
                    sharedData.saveSignDate(jsonObject.getString(AppConfig.API_SIGN_DATE))

                    for (i in 0 until jsonArray.length()) {
                        val result = jsonArray.getJSONObject(i)
                        signList.add(
                            SignDto(
                                S_id = result.getString(AppConfig.API_SID),
                                S_name = result.getString(AppConfig.API_SNAME),
                                StudentID = result.getString("StudentID"),
                                status = false
                            )
                        )
                    }

                    sharedData.put(signList, sharedData.keySignList)

                    // 設定人數
                    unSign = jsonArray.length()
                    val signPeople = getString(R.string.sign_text_CoursePeople) + sign + "/" + unSign
                    binding.signTextPeople.text = signPeople

                    initRecyclerView()

                    if (dialogHelper.dialogIsLoading())
                        dialogHelper.dismissLoadingDialog()

                    initWebSocket()
                    initBeaconCasting()

                    sharedData.saveSignPeople(getString(R.string.sign_text_CoursePeople) + sign + "/" + unSign)
                    binding.signTextPeople.text = sharedData.getSignPeople()

                } catch (jsonException: JSONException) {
                    dialogHelper.sweetDialog(getString(R.string.alert_error_title_json), SweetAlertDialog.ERROR_TYPE, null)
                }
            }

            override fun onError(error: VolleyError) {
                if (error.networkResponse != null) {
                    when (error.networkResponse.statusCode) {
                        400 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_400), SweetAlertDialog.ERROR_TYPE, null) }
                        404 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_404), SweetAlertDialog.ERROR_TYPE, null) }
                        417 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_417), SweetAlertDialog.ERROR_TYPE, null) }
                        500 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_500), SweetAlertDialog.ERROR_TYPE, null) }
                    }
                }
            }

            override val params: Map<String, String>
                get() = mapOf(
                    AppConfig.API_TID to sharedData.getID(),
                    AppConfig.API_CNAME to sharedData.getCourseName()
                )
        })
    }

    private fun initDialog() {
        var selected = ""
        arrayAdapter = ArrayAdapter(this, R.layout.custom_dialog_item, dropdownList)
        dialogHelper.dropDownDialog(getString(R.string.sign_text_Title_choose), arrayAdapter, object :
            DialogHelper.CustomPositiveListener {
            override fun onPositiveClick(var1: View, dropDownDialog: AlertDialog) {
                if (selected.isNotBlank() && selected != "") {
                    sharedData.saveCourseName(selected)
                    dialogHelper.loadingDialog()
                    val courseTitle = getString(R.string.sign_text_CourseTitle) + sharedData.getCourseName()
                    binding.signTextCourseTitle.text = courseTitle
                    dropDownDialog.cancel()
                    signCreate()

                } else {
                    Toast.makeText(this@SignActivity, R.string.toast_NOT_EMPTY, Toast.LENGTH_SHORT).show()
                }
            }
        }, object : DialogHelper.OnItemClickListener {
            override fun onItemClick(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                selected = adapterView?.getItemAtPosition(i).toString()
            }
        })
    }

    private fun initDialogData() {
        yuuzuApi.api(Request.Method.POST, AppConfig.URL_LIST_COURSE, object :
            YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                val jsonArray = JSONArray(data)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    dropdownList.add(jsonObject.getString(AppConfig.API_CNAME))
                }

                initDialog()
            }

            override fun onError(error: VolleyError) {
                if (error.networkResponse != null) {
                    when(error.networkResponse.statusCode) {
                        400 -> dialogHelper.showDialog(getString(R.string.alert_error_400), "")
                        404 -> dialogHelper.showDialog(getString(R.string.alert_error_404), "")
                        500 -> dialogHelper.showDialog(getString(R.string.alert_error_500), "")
                    }

                } else {
                    dialogHelper.showDialog(getString(R.string.alert_error_500), "")
                }
            }

            override val params: Map<String, String>
                get() = mapOf(
                    AppConfig.API_TID to sharedData.getID()
                )
        })
    }

    private fun initButton() {
        binding.signBtnDoneSign.setOnClickListener {
            dialogHelper.showFullDialog(getString(R.string.sign_text_leave_title), "${sharedData.getSignPeople()}\n確認是否結束點名？", object :
                DialogHelper.OnDialogListener {
                override fun onPositiveClick(dialog: DialogInterface?, which: Int) {
                    finish()
                    onEnd()
                    dialog?.dismiss()
                }

                override fun onNegativeClick(dialog: DialogInterface?, which: Int) {
                    dialog?.dismiss()
                }
            })
        }
    }

    private fun initView() {
        dropdownList = ArrayList()
        signList = ArrayList()

        beaconController = BeaconController(this, Region("Sign", Identifier.parse(AppConfig.BEACON_UUID_SIGN), null, null))
        dialogHelper = DialogHelper(this)
        sharedData = SharedData(this)
        yuuzuApi = YuuzuApi(this)
    }

    private fun onEnd() {
        isWebSocket = true
        if (beaconController.isBeaconCasting()) beaconController.stopBeaconCasting()
        WebSocketManager.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        onEnd()
    }
}