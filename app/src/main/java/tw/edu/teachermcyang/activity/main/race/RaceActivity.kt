package tw.edu.teachermcyang.activity.main.race

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.VolleyError
import com.google.android.material.textfield.TextInputEditText
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.Region
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import tw.edu.teachermcyang.AppConfig
import tw.edu.teachermcyang.R
import tw.edu.teachermcyang.databinding.ActivityRaceBinding
import tw.edu.teachermcyang.yuuzu_lib.*
import tw.edu.teachermcyang.yuuzu_lib.model.MessageListener
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class RaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRaceBinding

    private var failed = 0
    private var isRacing = false
    private lateinit var raceList: ArrayList<RaceDto>
    private lateinit var raceAdapter: RaceAdapter

    private lateinit var yuuzuApi: YuuzuApi
    private lateinit var sharedData: SharedData
    private lateinit var dialogHelper: DialogHelper
    private lateinit var beaconController: BeaconController

    // private val viewModel: RaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initButton()
        initDialog()

    }

    private fun initWebSocket() {
        WebSocketManager.init(AppConfig.WS_RACELIST, object : MessageListener {
            override fun onConnectSuccess() {
                Log.e(AppConfig.TAG, "onConnectSuccess: ")
            }

            override fun onConnectFailed() {
                Log.e(AppConfig.TAG, "onConnectFailed: ")
                if (isRacing) WebSocketManager.reconnect()
                if (failed == 3) dialogHelper.sweetDialog(getString(R.string.alert_error_title_noServer), SweetAlertDialog.ERROR_TYPE, object :
                    DialogHelper.SweetDialogPositiveListener {
                    override fun onPositiveClick(dialog: SweetAlertDialog) {
                        dialog.dismiss()
                        finish()
                    }
                })
                failed++
            }

            override fun onClose() {
                Log.e(AppConfig.TAG, "onClose: ")
            }

            override fun onMessage(text: String?) {
                Log.e(AppConfig.TAG, "onMessage: $text")
                try {
                    if (text != null) {
                        val jsonObject = JSONObject(text)
                        val raceId = jsonObject.getString(AppConfig.API_RACE_ID)

                        if (raceId == sharedData.getRaceId()) syncData()
                    }
                } catch (e: JSONException) {
                    dialogHelper.sweetDialog(getString(R.string.alert_error_json), SweetAlertDialog.ERROR_TYPE, null)
                }
            }
        })
        thread {
            kotlin.run {
                if (!isRacing) return@thread
                WebSocketManager.connect()
            }
        }
    }

    private fun updateAnswer(S_name: String, answer: String) {
        yuuzuApi.api(Request.Method.POST, AppConfig.URL_CREATE_RACELIST, object :
            YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                dialogHelper.sweetDialog(getString(R.string.alert_sweetAlert_Success), SweetAlertDialog.SUCCESS_TYPE, null)
            }

            override fun onError(error: VolleyError) {
                dialogHelper.sweetDialog(getString(R.string.alert_sweetAlert_Failed), SweetAlertDialog.ERROR_TYPE, null)
            }

            override val params: Map<String, String>
                get() = mapOf(
                    AppConfig.API_SNAME to S_name,
                    AppConfig.API_RACE_ID to sharedData.getRaceId(),
                    "Answer" to answer
                )
        })
    }

    private fun stopRace() {
        yuuzuApi.api(Request.Method.POST, AppConfig.URL_CREATE_RACE, object :
            YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) { isRacing = false }

            override fun onError(error: VolleyError) {}

            override val params: Map<String, String>
                get() = mapOf(
                    AppConfig.API_RACE_ID to sharedData.getRaceId(),
                    "Status" to "1"
                )
        })
    }

    private fun initRecyclerView() {
        raceAdapter = RaceAdapter(object : RaceAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (isRacing) {
                    dialogHelper.showCustomButtonDialog(
                        title = getString(R.string.race_text_Title_confirm) + raceList[position].sName,
                        message = getString(R.string.race_text_Message_confirm),
                        positiveText = getString(R.string.race_text_btn_confirm_Correct),
                        negativeText = getString(R.string.race_text_btn_confirm_Error),
                        object : DialogHelper.OnDialogListener {
                            override fun onPositiveClick(dialog: DialogInterface?, which: Int) {
                                updateAnswer(raceList[position].sName, "1")
                                dialog?.dismiss()
                            }

                            override fun onNegativeClick(dialog: DialogInterface?, which: Int) {
                                updateAnswer(raceList[position].sName, "99")
                                dialog?.dismiss()
                            }
                        }
                    )
                }
            }
        })

        binding.raceRecyclerView.apply {
            val linearLayoutManager = LinearLayoutManager(this@RaceActivity)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager = linearLayoutManager
            hasFixedSize()
            adapter = this@RaceActivity.raceAdapter
        }
    }

    private fun syncData() {
        yuuzuApi.api(Request.Method.GET, AppConfig.URL_LIST_RACELIST + "?Race_id=${sharedData.getRaceId()}", object :
            YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                val jsonArray = JSONArray(data)

                raceList = ArrayList()

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val rlId = jsonObject.getString("RL_id")
                    val answer = jsonObject.getString("Answer")
                    val sName = jsonObject.getString(AppConfig.API_SNAME)
                    val studentID = jsonObject.getString("StudentID")

                    raceList.add(RaceDto(rlId, answer, sName, studentID))
                    Log.e(AppConfig.TAG, "onSuccess: $raceList")
                }

                raceList.reverse()
                raceAdapter.differ.submitList(raceList)
//                viewModel.race(raceList)

//                lifecycleScope.launch {
//                    viewModel.raceUiState.collect{
//                        when (it) {
//                            is RaceViewModel.RaceUiState.Success -> {
//                                if (dialogHelper.dialogIsLoading()) dialogHelper.dismissLoadingDialog()
//                                Log.e(AppConfig.TAG, "LifeCycle SUCCESS!")
//                                raceAdapter.differ.submitList(raceList)
//                            }
//
//                            is RaceViewModel.RaceUiState.Error -> {
//                                Log.e(AppConfig.TAG, "LifeCycle Error!")
//                            }
//
//                            is RaceViewModel.RaceUiState.Loading -> {
//                                Log.e(AppConfig.TAG, "LifeCycle Loading!")
//                                if (!dialogHelper.dialogIsLoading()) dialogHelper.dialogIsLoading()
//                            }
//
//                            else -> Unit
//                        }
//                    }
//                }
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
                get() = mapOf()
        })
    }

    private fun initBeaconCasting() {
        beaconController.broadcastBeacon(AppConfig.BEACON_UUID_RACE, sharedData.getSignID(), sharedData.getRaceId())

        Handler(Looper.getMainLooper()).postDelayed({
            beaconController.startBeaconCasting()
        }, 1000)
    }

    private fun startRace(doc: String) {
        yuuzuApi.api(Request.Method.POST, AppConfig.URL_CREATE_RACE, object : YuuzuApi.YuuzuApiListener {
            @SuppressLint("SetTextI18n")
            override fun onSuccess(data: String) {
                val jsonObject = JSONObject(data)
                val raceId = jsonObject.getString(AppConfig.API_RACE_ID)
                val raceDoc = jsonObject.getString(AppConfig.API_RACE_DOC)

                // setup UI
                binding.raceTitle.text = getString(R.string.race_text_raceTitle) + raceDoc
                binding.eventText.text = getString(R.string.race_text_raceStart)
                binding.eventText.setTextColor(Color.parseColor("#44d56c"))
                binding.eventButton.strokeColor = Color.parseColor("#44d56c")

                // save ID
                sharedData.saveRaceId(raceId)

                // Prepare for race
                if (dialogHelper.dialogIsLoading()) dialogHelper.dismissLoadingDialog()
                isRacing = true

                // start Sync Api
                initBeaconCasting()
                initWebSocket()
                initRecyclerView()
            }

            override fun onError(error: VolleyError) {
                if (error.networkResponse != null) {
                    if (dialogHelper.dialogIsLoading()) dialogHelper.dismissLoadingDialog()
                    when(error.networkResponse.statusCode) {
                        400 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_400), SweetAlertDialog.ERROR_TYPE, null) }
                        404 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_404), SweetAlertDialog.ERROR_TYPE, null) }
                        417 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_417), SweetAlertDialog.ERROR_TYPE, null) }
                        500 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_500), SweetAlertDialog.ERROR_TYPE, null) }
                    }
                }
            }

            override val params: Map<String, String>
                get() = mapOf(
                    AppConfig.API_CID to sharedData.getCID(),
                    AppConfig.API_RACE_DOC to doc
                )
        })
    }

    private fun initDialog() {
        dialogHelper.textInputDialog(getString(R.string.race_text_raceInput), object : DialogHelper.CustomTextPositiveListener {
            override fun onPositiveTextClick(
                var1: View,
                dropDownDialog: AlertDialog,
                inputText: TextInputEditText
            ) {
                if (inputText.text.toString().isNotBlank()) {
                    startRace(inputText.text.toString())
                    dialogHelper.loadingDialog()
                    dropDownDialog.cancel()
                } else {
                    dialogHelper.sweetDialog(getString(R.string.toast_NOT_EMPTY), SweetAlertDialog.ERROR_TYPE, null)
                }
            }
        })
    }

    private fun initButton() {
        binding.raceBtnBack.setOnClickListener {
            dialogHelper.sweetBtnDialog(
                title = getString(R.string.alert_quit),
                message = "",
                positiveText = getString(R.string.alert_positive),
                negativeText = getString(R.string.alert_negative),
                cancelable = false,
                status = SweetAlertDialog.WARNING_TYPE,
                object : DialogHelper.SweetDialogListener {
                    override fun onPositiveClick(dialog: SweetAlertDialog) {
                        if (isRacing) stopRace()
                        if (WebSocketManager.isConnect()) WebSocketManager.close()
                        if (beaconController.isBeaconCasting()) beaconController.stopBeaconCasting()
                        finish()
                        dialog.dismiss()
                    }

                    override fun onNegativeClick(dialog: SweetAlertDialog) {
                        dialog.dismiss()
                    }
                }
            )
        }

        binding.raceBtnEnd.setOnClickListener {
            dialogHelper.sweetBtnDialog(
                title = getString(R.string.race_alert_EndTitle),
                message = getString(R.string.race_alert_EndMessage),
                positiveText = getString(R.string.alert_positive),
                negativeText = getString(R.string.alert_negative),
                cancelable = false,
                status = SweetAlertDialog.WARNING_TYPE,
                object : DialogHelper.SweetDialogListener {
                    override fun onPositiveClick(dialog: SweetAlertDialog) {
                        dialog.dismiss()
                        beaconController.stopBeaconCasting()
                        stopRace()

                        dialogHelper.sweetDialog(getString(R.string.alert_sweetAlert_Success), SweetAlertDialog.SUCCESS_TYPE, null)
                        binding.eventText.text = getString(R.string.race_text_raceStop)
                        binding.eventText.setTextColor(Color.parseColor("#f96767"))
                        binding.eventButton.strokeColor = Color.parseColor("#f96767")
                        binding.eventButton.isEnabled = false
                        binding.raceBtnEnd.isEnabled = false
                    }

                    override fun onNegativeClick(dialog: SweetAlertDialog) {
                        dialog.dismiss()
                    }
                }
            )
        }
    }

    private fun initView() {
        raceList = ArrayList()

        yuuzuApi = YuuzuApi(this)
        sharedData = SharedData(this)
        dialogHelper = DialogHelper(this)
        beaconController = BeaconController(this, Region("", Identifier.parse(AppConfig.BEACON_UUID_RACE), null, null))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRacing) stopRace()
        if (WebSocketManager.isConnect()) WebSocketManager.close()
        if (beaconController.isBeaconCasting()) beaconController.stopBeaconCasting()

        // sharedData.saveRaceId("")
        finish()
    }
}