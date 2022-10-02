package tw.edu.teachermcyang.activity.main.group_create

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.VolleyError
import kotlinx.coroutines.launch
import org.altbeacon.beacon.Region
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import tw.edu.teachermcyang.AppConfig
import tw.edu.teachermcyang.R
import tw.edu.teachermcyang.databinding.ActivityGroupCreateBinding
import tw.edu.teachermcyang.yuuzu_lib.*
import tw.edu.teachermcyang.yuuzu_lib.anim.fadeIn
import tw.edu.teachermcyang.yuuzu_lib.anim.slideRightOut
import tw.edu.teachermcyang.yuuzu_lib.model.MessageListener
import kotlin.concurrent.thread

class GroupCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupCreateBinding

    private lateinit var yuuzuApi: YuuzuApi
    private lateinit var sharedData: SharedData
    private lateinit var dialogHelper: DialogHelper
    private lateinit var beaconController: BeaconController

    private lateinit var leaderAdapter: LeaderAdapter
    private lateinit var memberAdapter: MemberAdapter

    private lateinit var leaderList: ArrayList<LeaderDto>
    private lateinit var memberList: ArrayList<MemberDto>

    private var webSocking = false

    // private var groupName = ""
    private var groupDesc = ""
    private var groupTotal = 0
    private var groupLimit = 0
    private var teamDescID = ""

    private val viewModel: LeaderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initButton()
    }

    private fun finishGroup() {
        yuuzuApi.api(Request.Method.POST, AppConfig.URL_CREATE_TEAMMEMBER, object :
            YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                try {
                    Toast.makeText(this@GroupCreateActivity, "建立成功", Toast.LENGTH_SHORT).show()
                } catch (e : Exception) {
                    e.printStackTrace()
                }
            }

            override fun onError(error: VolleyError) {
                try {
                    Toast.makeText(this@GroupCreateActivity, "建立失敗", Toast.LENGTH_SHORT).show()
                } catch (e : Exception) {
                    theEnd()
                    finish()
                }
            }

            override val params: Map<String, String>
                get() = mapOf(
                    AppConfig.API_TEAMDESC_ID to teamDescID,
                    "User" to "1",
                )
        })
    }

    private fun finishLeader() {
        yuuzuApi.api(Request.Method.POST, AppConfig.URL_CREATE_TEAMLEADER, object :
            YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                try {
                    binding.groupCreateCardViewScene2.slideRightOut(1000L, 0L)
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.groupCreateCardViewScene2.visibility = View.GONE
                        binding.groupCreateCardViewScene3.visibility = View.VISIBLE
                        binding.groupCreateCardViewScene3.fadeIn(1000L, 0L)
                    }, 900L)

                    // setup scene 3 UI
                    initMemberView()

                    // change beacon uuid
                    if (beaconController.isBeaconCasting()) beaconController.stopBeaconCasting()
                    beaconController.broadcastBeacon(AppConfig.BEACON_UUID_MEMBER, sharedData.getSignID(), teamDescID)
                    Handler(Looper.getMainLooper()).postDelayed({
                        beaconController.startBeaconCasting()
                    }, 3000)

                    dialogHelper.sweetDialog("隊長已選擇完成，前往組員分配。", SweetAlertDialog.SUCCESS_TYPE, null)

                } catch (e : Exception) {
                    theEnd()
                    finish()
                }
            }

            override fun onError(error: VolleyError) {
                try {
                    if (error.networkResponse != null) {
                        when (error.networkResponse.statusCode) {
                            400 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_400), SweetAlertDialog.ERROR_TYPE, null) }
                            404 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_404), SweetAlertDialog.ERROR_TYPE, null) }
                            406 -> {dialogHelper.sweetDialog("隊長人數已滿！", SweetAlertDialog.ERROR_TYPE, null) }
                            417 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_417), SweetAlertDialog.ERROR_TYPE, null) }
                            500 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_500), SweetAlertDialog.ERROR_TYPE, null) }
                        }
                    }

                    theEnd()
                    finish()
                } catch (e : Exception) {
                    theEnd()
                    finish()
                }
            }

            override val params: Map<String, String>
                get() = mapOf(
                    AppConfig.API_TEAMDESC_ID to teamDescID,
                    "User" to "1"
                )
        })
    }

    private fun memberDetailData(memberDto: MemberDto) {
        yuuzuApi.api(Request.Method.GET, AppConfig.URL_LIST_TEAMMEMBER + "?TeamLeader_id=${memberDto.leaderID}", object :
            YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                try {
                    val count = 1
                    var studentName = "$count. ${memberDto.studentName}\n"
                    val jsonArray = JSONArray(data)
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        studentName += "${count + i + 1}. ${jsonObject.getString(AppConfig.API_SNAME)}\n"
                    }

                    dialogHelper.showDialog("隊員資料", studentName)

                } catch (e: JSONException) {
                    dialogHelper.sweetDialog(getString(R.string.alert_error_title_json), SweetAlertDialog.ERROR_TYPE, null)
                }
            }

            override fun onError(error: VolleyError) {
                if (error.networkResponse != null) {
                    when (error.networkResponse.statusCode) {
                        400 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_400), SweetAlertDialog.ERROR_TYPE, null) }
                        404 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_404), SweetAlertDialog.ERROR_TYPE, null) }
                        500 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_500), SweetAlertDialog.ERROR_TYPE, null) }
                    }
                }
            }

            override val params: Map<String, String>
                get() = mapOf()
        })
    }

    private fun initMemberView() {
        binding.cardMemberEventText.text = getString(R.string.groupCreate_text_Broadcasting)

        memberAdapter = MemberAdapter(object : MemberAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val memberDto = memberList[position]
                memberDetailData(memberDto)
            }
        })

        binding.groupCreateMemberRecyclerView.apply {
            val linearLayoutManager = LinearLayoutManager(this@GroupCreateActivity)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager = linearLayoutManager
            hasFixedSize()
            adapter = this@GroupCreateActivity.memberAdapter
        }

        for (i in 0 until leaderList.size) {
            memberList.add(
                MemberDto(
                    leaderID = leaderList[i].leaderID,
                    studentName = leaderList[i].studentName,
                    studentID = leaderList[i].studentID,
                    peopleCount = "1/$groupLimit",
                )
            )
        }

        memberAdapter.differ.submitList(memberList)
    }

    private fun syncMemberData(teamDescID: String) {
        Log.e(AppConfig.TAG, "syncMemberData: ")
        yuuzuApi.api(Request.Method.GET, AppConfig.URL_LIST_TEAMMEMBER + "?TeamDesc_id=$teamDescID", object :
            YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                try {
                    memberList = ArrayList()

                    val jsonArray = JSONArray(data)
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val memberDto = MemberDto(
                            leaderID = jsonObject.getString(AppConfig.API_TEAMLEADER_ID),
                            studentName = jsonObject.getString(AppConfig.API_SNAME),
                            studentID = jsonObject.getString("S_email"),
                            peopleCount = jsonObject.getString("PeopleCount"),
                        )

                        memberList.add(memberDto)
                        memberAdapter.differ.submitList(memberList)
                    }

                } catch (e: JSONException) {
                    dialogHelper.sweetDialog(getString(R.string.alert_error_title_json), SweetAlertDialog.ERROR_TYPE, null)
                }
            }

            override fun onError(error: VolleyError) {
                when (error.networkResponse.statusCode) {
                    400 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_400), SweetAlertDialog.ERROR_TYPE, null) }
                    404 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_404), SweetAlertDialog.ERROR_TYPE, null) }
                    500 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_500), SweetAlertDialog.ERROR_TYPE, null) }
                }
            }

            override val params: Map<String, String>
                get() = mapOf()
        })
    }

    private fun pickLeader(leaderDto: LeaderDto, isPicked: String) {
        yuuzuApi.api(Request.Method.POST, AppConfig.URL_CREATE_TEAMLEADER, object : YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                dialogHelper.sweetDialog(getString(R.string.alert_sweetAlert_Success), SweetAlertDialog.SUCCESS_TYPE, null)
            }

            override fun onError(error: VolleyError) {
                if (error.networkResponse != null) {
                    when (error.networkResponse.statusCode) {
                        400 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_400), SweetAlertDialog.ERROR_TYPE, null) }
                        404 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_404), SweetAlertDialog.ERROR_TYPE, null) }
                        406 -> {dialogHelper.sweetDialog("隊長人數已滿！", SweetAlertDialog.ERROR_TYPE, null) }
                        417 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_417), SweetAlertDialog.ERROR_TYPE, null) }
                        500 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_500), SweetAlertDialog.ERROR_TYPE, null) }
                    }
                }
            }

            override val params: Map<String, String>
                get() = mapOf(
                    AppConfig.API_TEAMLEADER_ID to leaderDto.leaderID,
                    AppConfig.API_TEAMDESC_ID to teamDescID,
                    "Group_number" to isPicked, // 批改狀態
                    "User" to "0", // 是誰 0：老師 1:學生
                )
        })
    }

    private fun initLeaderView() {
        binding.cardLeaderEventText.text = getString(R.string.groupCreate_text_Broadcasting)

        leaderAdapter = LeaderAdapter(object : LeaderAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val leaderDto = leaderList[position]
                when (leaderDto.isPicked) {
                    "0" -> {
                        dialogHelper.sweetBtnDialog(
                            title = "確定要選擇${leaderList[position].studentName}同學爲隊長嗎？",
                            message = "",
                            positiveText = getString(R.string.alert_positive),
                            negativeText = getString(R.string.alert_negative),
                            cancelable = false,
                            status = SweetAlertDialog.WARNING_TYPE,
                            object : DialogHelper.SweetDialogListener {
                                override fun onPositiveClick(dialog: SweetAlertDialog) {
                                    dialog.dismiss()
                                    pickLeader(leaderDto, "1")
                                }

                                override fun onNegativeClick(dialog: SweetAlertDialog) {
                                    dialog.dismiss()
                                }
                            }
                        )
                    }
                    "1" -> {
                        dialogHelper.sweetBtnDialog(
                            title = "確定要取消${leaderList[position].studentName}同學爲隊長嗎？",
                            message = "",
                            positiveText = getString(R.string.alert_positive),
                            negativeText = getString(R.string.alert_negative),
                            cancelable = false,
                            status = SweetAlertDialog.WARNING_TYPE,
                            object : DialogHelper.SweetDialogListener {
                                override fun onPositiveClick(dialog: SweetAlertDialog) {
                                    dialog.dismiss()
                                    pickLeader(leaderDto, "0")
                                }

                                override fun onNegativeClick(dialog: SweetAlertDialog) {
                                    dialog.dismiss()
                                }
                            }
                        )
                    }
                }
            }
        })

        binding.groupCreateLeaderRecyclerView.apply {
            val linearLayoutManager = LinearLayoutManager(this@GroupCreateActivity)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager = linearLayoutManager
            hasFixedSize()
            adapter = this@GroupCreateActivity.leaderAdapter
        }
    }

    private fun syncLeaderData() {
        yuuzuApi.api(Request.Method.GET, AppConfig.URL_LIST_TEAMLEADER + "?TeamDesc_id=$teamDescID", object : YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                try {
                    leaderList = ArrayList()

                    val jsonArray = JSONArray(data)
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val leaderDto = LeaderDto(
                            leaderID = jsonObject.getString(AppConfig.API_TEAMLEADER_ID),
                            studentName = jsonObject.getString(AppConfig.API_SNAME),
                            studentID = jsonObject.getString("S_email"),
                            sID = jsonObject.getString(AppConfig.API_SID),
                            isPicked = jsonObject.getString("IsPicked")
                        )

                        leaderList.add(leaderDto)
                        viewModel.leader(leaderList)

                        lifecycleScope.launch {
                            viewModel.leaderUiState.collect {
                                when (it) {
                                    is LeaderViewModel.LeaderUiState.Success -> {
                                        if (dialogHelper.dialogIsLoading()) dialogHelper.dismissLoadingDialog()
                                        leaderAdapter.differ.submitList(leaderList)
                                    }
                                    is LeaderViewModel.LeaderUiState.Loading -> {
                                        if (!dialogHelper.dialogIsLoading()) dialogHelper.loadingDialog()
                                    }
                                    is LeaderViewModel.LeaderUiState.Error -> {

                                    }
                                    else -> Unit
                                }
                            }
                        }
                    }

                } catch (e: JSONException) {
                    dialogHelper.sweetDialog(getString(R.string.alert_error_title_json), SweetAlertDialog.ERROR_TYPE, null)
                }
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

    private fun initWebSocket() {
        WebSocketManager.init(AppConfig.WS_GROUPLIST, object : MessageListener {
            override fun onConnectSuccess() {
                Log.e(AppConfig.TAG, "onConnectSuccess: ")
            }

            override fun onConnectFailed() {
                Log.e(AppConfig.TAG, "onConnectFailed: ")
            }

            override fun onClose() {
                Log.e(AppConfig.TAG, "onClose: ")
            }

            override fun onMessage(text: String?) {
                try {
                    if (text.isNullOrBlank()) return
                    val jsonObject = JSONObject(text)
                    val teamDescId = jsonObject.getString(AppConfig.API_TEAMDESC_ID)
                    // val identity = jsonObject.getString("Identity")
                    val leader = jsonObject.getString("Leader")
                    val member = jsonObject.getString("Member")

                    if (teamDescId != teamDescID) return
                    // if (identity == "0") return

                    if (leader != "0") syncLeaderData()
                    if (member != "0") syncMemberData(teamDescId)

                } catch (e: JSONException) {
                    dialogHelper.sweetDialog(getString(R.string.alert_error_title_json), SweetAlertDialog.ERROR_TYPE, null)
                }
            }
        })

        thread {
            kotlin.run {
                if (!webSocking) return@thread
                WebSocketManager.connect()
            }
        }
    }

    private fun createGroupDesc() {
        yuuzuApi.api(Request.Method.POST, AppConfig.URL_CREATE_TEAMDESC, object :
            YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                try {
                    val jsonObject = JSONObject(data)
                    teamDescID = jsonObject.getString(AppConfig.API_TEAMDESC_ID)
                    Log.e(AppConfig.TAG, "onSuccess: $teamDescID")

                    // GOTO: Scene 2
                    dialogHelper.sweetDialog("建立成功，即將跳轉至選擇隊長！", SweetAlertDialog.SUCCESS_TYPE, object :
                        DialogHelper.SweetDialogPositiveListener {
                        override fun onPositiveClick(dialog: SweetAlertDialog) {
                            dialog.dismiss()

                            binding.groupCreateCardViewScene1.slideRightOut(1000L, 0L)
                            Handler(Looper.getMainLooper()).postDelayed({
                                binding.groupCreateCardViewScene1.visibility = View.GONE
                                binding.groupCreateCardViewScene2.visibility = View.VISIBLE
                                binding.groupCreateCardViewScene2.fadeIn(1000L, 0L)
                            }, 900L)
                        }
                    })

                    // TODO: setup scene 2 UI
                    initLeaderView()

                    // turn on beacon
                    beaconController.broadcastBeacon(AppConfig.BEACON_UUID_LEADER, sharedData.getSignID(), teamDescID)
                    Handler(Looper.getMainLooper()).postDelayed({
                        beaconController.startBeaconCasting()
                    }, 3000)

                    // turn on some setup
                    webSocking = true
                    initWebSocket()

                } catch (e: JSONException) {
                    dialogHelper.sweetDialog(getString(R.string.alert_error_title_json), SweetAlertDialog.ERROR_TYPE, null)
                }
            }

            override fun onError(error: VolleyError) {
                if (error.networkResponse != null) {
                    when (error.networkResponse.statusCode) {
                        400 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_400), SweetAlertDialog.ERROR_TYPE, null) }
                        404 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_404), SweetAlertDialog.ERROR_TYPE, null) }
                        406 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_406), SweetAlertDialog.ERROR_TYPE, null) }
                        417 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_417), SweetAlertDialog.ERROR_TYPE, null) }
                        500 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_500), SweetAlertDialog.ERROR_TYPE, null) }
                    }
                }
            }

            override val params: Map<String, String>
                get() = mapOf(
                    AppConfig.API_CID to sharedData.getCID(),
                    "Doc" to groupDesc,
                    "Total" to groupTotal.toString(),
                    "Limit" to groupLimit.toString()
                )
        })
    }

    private fun initButton() {
        binding.groupCreateBtnBack.setOnClickListener {
            dialogHelper.sweetBtnDialog(
                title = getString(R.string.alert_quit),
                message = "離開的話將會終止建立隊伍的動作！",
                positiveText = getString(R.string.alert_positive),
                negativeText = getString(R.string.alert_negative),
                cancelable = false,
                status = SweetAlertDialog.WARNING_TYPE,
                object : DialogHelper.SweetDialogListener {
                    override fun onPositiveClick(dialog: SweetAlertDialog) {
                        dialog.dismiss()
                        theEnd()
                        finish()
                    }

                    override fun onNegativeClick(dialog: SweetAlertDialog) {
                        dialog.dismiss()
                    }
                }
            )
        }

        // Scene 1
        binding.groupCreateButtonCreate.setOnClickListener {

            binding.groupCreateEditTextGroupDescription.text.toString().let {
                if (it.isEmpty()) {
                    binding.groupCreateEditTextGroupDescription.error = getString(R.string.groupCreate_alert_NullDesc)
                    return@let
                }
                groupDesc = it
            }

            binding.groupCreateEditTextGroupTotalTeam.text.toString().let {
                if (it.isEmpty()) {
                    binding.groupCreateEditTextGroupTotalTeam.error = getString(R.string.groupCreate_alert_NullTotal)
                    return@let
                }
                groupTotal = it.toInt()
            }

            binding.groupCreateEditTextGroupTotalMember.text.toString().let {
                if (it.isEmpty()) {
                    binding.groupCreateEditTextGroupTotalMember.error = getString(R.string.groupCreate_alert_NullLimit)
                    return@let
                }
                groupLimit = it.toInt()
            }

            if (groupDesc.isBlank()) {
                dialogHelper.sweetDialog(getString(R.string.alert_inputNull), SweetAlertDialog.WARNING_TYPE, null)
                return@setOnClickListener
            }

            if (groupTotal < 1 || groupLimit < 1) {
                dialogHelper.sweetDialog(getString(R.string.groupCreate_alert_TotalAndLimit), SweetAlertDialog.WARNING_TYPE, null)
                return@setOnClickListener
            }

            createGroupDesc()
            leaderList = ArrayList()
            memberList = ArrayList()
        }

        // Scene 2
        binding.groupCreateLeaderButton.setOnClickListener {
            var totalPick = 0
            leaderList.forEach {
                if (it.isPicked == "1") totalPick ++
            }

            dialogHelper.sweetBtnDialog(
                title = "前往組員匹配",
                message = "已選擇隊長人數$totalPick/$groupLimit",
                positiveText = getString(R.string.alert_positive),
                negativeText = getString(R.string.alert_negative),
                cancelable = false,
                status = SweetAlertDialog.WARNING_TYPE,
                object : DialogHelper.SweetDialogListener {
                    override fun onPositiveClick(dialog: SweetAlertDialog) {
                        dialog.dismiss()
                        // send finish Leader
                        finishLeader()
                    }

                    override fun onNegativeClick(dialog: SweetAlertDialog) {
                        dialog.dismiss()
                    }
                }
            )
        }

        // Scene 3
        binding.groupCreateMemberButton.setOnClickListener {
            // TODO: Finish this
            dialogHelper.sweetBtnDialog(
                title = "確定要完成組隊嗎？",
                message = "",
                positiveText = getString(R.string.alert_positive),
                negativeText = getString(R.string.alert_negative),
                cancelable = false,
                status = SweetAlertDialog.WARNING_TYPE,
                object : DialogHelper.SweetDialogListener {
                    override fun onPositiveClick(dialog: SweetAlertDialog) {
                        dialog.dismiss()
                        dialogHelper.sweetDialog("組隊完成，正在返回主畫面。", SweetAlertDialog.SUCCESS_TYPE, object :
                            DialogHelper.SweetDialogPositiveListener {
                            override fun onPositiveClick(dialog: SweetAlertDialog) {
                                dialog.dismiss()
                                finishGroup()
                                theEnd()
                                finish()
                            }
                        })
                    }

                    override fun onNegativeClick(dialog: SweetAlertDialog) {
                        dialog.dismiss()
                    }
                }
            )
        }
    }

    private fun initView() {
        ViewHelper(this).setupUI(binding.groupCreateCardViewScene1)

        yuuzuApi = YuuzuApi(this)
        sharedData = SharedData(this)
        dialogHelper = DialogHelper(this)
        beaconController = BeaconController(this, Region("group", null, null, null))

        memberList = ArrayList()
    }

    private fun theEnd() {
        if (WebSocketManager.isConnect()) WebSocketManager.close()
        if (dialogHelper.dialogIsLoading()) dialogHelper.dismissLoadingDialog()
        if (beaconController.isBeaconCasting()) beaconController.stopBeaconCasting()
    }

    override fun onDestroy() {
        super.onDestroy()
        theEnd()
    }
}