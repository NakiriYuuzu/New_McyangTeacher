package tw.edu.teachermcyang.activity.main.group_chat

import android.annotation.SuppressLint
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
import com.google.firebase.database.*
import org.json.JSONArray
import org.json.JSONException
import tw.edu.teachermcyang.AppConfig
import tw.edu.teachermcyang.R
import tw.edu.teachermcyang.databinding.ActivityChatBinding
import tw.edu.teachermcyang.yuuzu_lib.DialogHelper
import tw.edu.teachermcyang.yuuzu_lib.SharedData
import tw.edu.teachermcyang.yuuzu_lib.ViewHelper
import tw.edu.teachermcyang.yuuzu_lib.YuuzuApi
import tw.edu.teachermcyang.yuuzu_lib.anim.fadeIn
import tw.edu.teachermcyang.yuuzu_lib.anim.fadeOut
import java.util.Calendar

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding

    private lateinit var yuuzuApi: YuuzuApi
    private lateinit var sharedData: SharedData
    private lateinit var viewHelper: ViewHelper
    private lateinit var dialogHelper: DialogHelper

    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference

    private lateinit var teamDescAdapter: TeamDescAdapter
    private lateinit var chatRoomAdapter: ChatRoomAdapter
    private lateinit var chatLeaderAdapter: ChatLeaderAdapter
    private lateinit var chatAdapter: ChatAdapter

    private lateinit var teamDescList: ArrayList<TeamDescDto>
    private lateinit var chatRoomList: ArrayList<ChatRoomDto>
    private lateinit var currentChatRoom: ArrayList<ChatRoomDto>
    private lateinit var chatLeaderList: ArrayList<ChatLeaderDto>
    private lateinit var chatList: ArrayList<ChatDto>

    private var teamDescId = ""
    private var currentTitle = ""
    private var currentLeader = ""
    private var chatID = ""
    private var chatRoomName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initButton()
        initTeamDescList()
    }

    private fun syncChat() {
        ref = database.getReference(chatID)
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatData = ArrayList<String>()
                for (messageData in snapshot.children) chatData.add(messageData.value.toString())

                chatList.add(
                    ChatDto(
                        time = chatData[0],
                        message = chatData[1],
                        user = chatData[2],
                        current = snapshot.key.toString()
                    )
                )

                chatAdapter.differ.submitList(chatList)
                binding.groupChatScene4RecyclerView.scrollToPosition(chatList.size - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.e(AppConfig.TAG, "onChildChanged: ")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.e(AppConfig.TAG, "onChildRemoved: ")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.e(AppConfig.TAG, "onChildMoved: ")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(AppConfig.TAG, "onCancelled: ")
            }
        })
    }

    private fun getChatLeader() {
        yuuzuApi.api(Request.Method.GET, "${AppConfig.URL_LIST_TEAMCHAT}?${AppConfig.API_TEAMDESC_ID}=$teamDescId&ChatTitle=$currentTitle", object :
            YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                try {
                    val jsonArray = JSONArray(data)
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        chatLeaderList.add(
                            ChatLeaderDto(
                                jsonObject.getString("GroupChat_id"),
                                jsonObject.getString("S_name"),
                                jsonObject.getString("ChatTitle")
                            )
                        )

                        chatLeaderAdapter.differ.submitList(chatLeaderList)

                        if (dialogHelper.dialogIsLoading()) dialogHelper.dismissLoadingDialog()
                    }
                } catch (e : Exception) {
                    dialogHelper.sweetDialog(getString(R.string.alert_error_title_json), SweetAlertDialog.ERROR_TYPE, null)
                }
            }

            override fun onError(error: VolleyError) {
                if (dialogHelper.dialogIsLoading()) dialogHelper.dismissLoadingDialog()
                if (error.networkResponse != null) {
                    when (error.networkResponse.statusCode) {
                        400 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_400), SweetAlertDialog.ERROR_TYPE, null)}
                        500 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_500), SweetAlertDialog.ERROR_TYPE, null)}
                    }
                }
            }

            override val params: Map<String, String>
                get() = mapOf()
        })
    }

    private fun createDatabase() {
        if (currentChatRoom.size > 0) {
            Log.e(AppConfig.TAG, "createDatabase: $currentChatRoom")
            currentChatRoom.forEach {
                ref = database.reference.root
                val message = HashMap<String, String>()
                val datetime = "${Calendar.getInstance().get(Calendar.MONTH) + 1}/${Calendar.getInstance().get(Calendar.DAY_OF_MONTH)} ${Calendar.getInstance().get(Calendar.HOUR_OF_DAY)}:${Calendar.getInstance().get(Calendar.MINUTE)}"
                message["user"] = sharedData.getCourseName()
                message["message"] = "歡迎加入群組聊天室"
                message["datetime"] = datetime
                ref.child(it.groupChat_id).child(Calendar.getInstance().timeInMillis.toString()).setValue(message)
            }
        }
    }

    private fun chatRoomCreate(title: String) {
        yuuzuApi.api(Request.Method.POST, AppConfig.URL_CREATE_TEAMCHAT, object :
            YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                initChatRoomList(AppConfig.URL_LIST_TEAMCHAT + "?${AppConfig.API_TEAMDESC_ID}=$teamDescId&${AppConfig.API_CID}=${sharedData.getCID()}")
                currentChatRoom = ArrayList()
                try {
                    val jsonArray = JSONArray(data)
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        currentChatRoom.add(ChatRoomDto(
                            groupChat_id = jsonObject.getString("GroupChat_id"),
                            teamLeader_id = jsonObject.getString("TeamLeader_id"),
                            teamDesc_id = teamDescId,
                            c_id = sharedData.getCID(),
                            s_name = jsonObject.getString("S_name"),
                            chatTitle = jsonObject.getString("ChatTitle"),
                            crtTime = jsonObject.getString("CrtTime"),
                        ))
                    }

                    createDatabase()

                } catch (e: JSONException) {
                    Log.e(AppConfig.TAG, "onSuccess: ${e.message}")
                    dialogHelper.sweetDialog(getString(R.string.alert_error_title_json), SweetAlertDialog.ERROR_TYPE, null)
                }
            }

            override fun onError(error: VolleyError) {
                if (dialogHelper.dialogIsLoading()) dialogHelper.dismissLoadingDialog()
                if (error.networkResponse != null) {
                    when (error.networkResponse.statusCode) {
                        400 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_400), SweetAlertDialog.ERROR_TYPE, null)}
                        406 -> {dialogHelper.sweetDialog("聊天室名稱重複！", SweetAlertDialog.ERROR_TYPE, null)}
                        417 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_417), SweetAlertDialog.ERROR_TYPE, null)}
                        500 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_500), SweetAlertDialog.ERROR_TYPE, null)}
                    }
                }
            }

            override val params: Map<String, String>
                get() = mapOf(
                    AppConfig.API_TEAMDESC_ID to teamDescId,
                    "Chat_title" to title
                )
        })
    }

    private fun initChatRoomList(url: String) {
        yuuzuApi.api(Request.Method.GET, url, object : YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                try {
                    val jsonArray = JSONArray(data)
                    chatRoomList = ArrayList()

                    when (url) {
                        AppConfig.URL_LIST_TEAMCHAT + "?${AppConfig.API_TEAMDESC_ID}=$teamDescId&${AppConfig.API_CID}=${sharedData.getCID()}" -> {
                            if (jsonArray.length() == 0) {
                                dialogHelper.sweetBtnDialog(
                                    title = "目前沒有任何聊天室，請問是否要爲學生創建聊天室？",
                                    message = "",
                                    positiveText = getString(R.string.alert_positive),
                                    negativeText = getString(R.string.alert_negative),
                                    cancelable = false,
                                    status = SweetAlertDialog.WARNING_TYPE,
                                    object : DialogHelper.SweetDialogListener {
                                        override fun onPositiveClick(dialog: SweetAlertDialog) {
                                            dialog.dismiss()
                                            dialogHelper.inputDialog("創建聊天室", "輸入聊天室主題", object :
                                                DialogHelper.CustomTextListener {
                                                override fun onPositiveTextClick(
                                                    var1: View,
                                                    dialog: AlertDialog,
                                                    inputText: TextInputEditText
                                                ) {
                                                    dialog.dismiss()
                                                    if (inputText.text.toString().isNotBlank()) {
                                                        dialog.dismiss()
                                                        if (!dialogHelper.dialogIsLoading()) dialogHelper.loadingDialog()
                                                        chatRoomCreate(inputText.text.toString())
                                                    } else {
                                                        dialogHelper.sweetDialog(getString(R.string.toast_NOT_EMPTY), SweetAlertDialog.ERROR_TYPE, null)
                                                    }
                                                }

                                                override fun onNegativeTextClick(
                                                    var1: View,
                                                    dialog: AlertDialog
                                                ) {
                                                    dialog.dismiss()
                                                }
                                            })
                                        }

                                        override fun onNegativeClick(dialog: SweetAlertDialog) {
                                            dialog.dismiss()
                                        }
                                    }
                                )
                            }

                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                chatRoomList.add(
                                    ChatRoomDto(
                                        groupChat_id = "",
                                        teamDesc_id = teamDescId,
                                        c_id = sharedData.getCID(),
                                        teamLeader_id = "",
                                        chatTitle = jsonObject.getString("ChatTitle"),
                                        s_name = "",
                                        crtTime = jsonObject.getString(AppConfig.API_TIME),
                                    )
                                )
                            }
                        }

                        "${AppConfig.URL_LIST_TEAMCHAT}?${AppConfig.API_TEAMDESC_ID}=$teamDescId&ChatTitle=$currentTitle" -> {
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                chatRoomList.add(
                                    ChatRoomDto(
                                        groupChat_id = jsonObject.getString("GroupChat_id"),
                                        teamDesc_id = teamDescId,
                                        c_id = sharedData.getCID(),
                                        teamLeader_id = jsonObject.getString(AppConfig.API_TEAMLEADER_ID),
                                        chatTitle = jsonObject.getString("ChatTitle"),
                                        s_name = jsonObject.getString(AppConfig.API_SNAME),
                                        crtTime = jsonObject.getString(AppConfig.API_TIME),
                                    )
                                )
                            }
                        }
                    }

                    chatRoomAdapter.differ.submitList(chatRoomList)

                    Handler(Looper.getMainLooper()).postDelayed({
                        chatRoomAdapter.differ.submitList(chatRoomList)
                        if (dialogHelper.dialogIsLoading()) dialogHelper.dismissLoadingDialog()
                    }, 1000)

                } catch (e: JSONException) {
                    if (dialogHelper.dialogIsLoading()) dialogHelper.dismissLoadingDialog()
                    dialogHelper.sweetDialog(getString(R.string.alert_error_title_json), SweetAlertDialog.ERROR_TYPE, null)
                } catch (e: Exception) {
                    if (dialogHelper.dialogIsLoading()) dialogHelper.dismissLoadingDialog()
                    dialogHelper.sweetDialog(getString(R.string.alert_error_title_json), SweetAlertDialog.ERROR_TYPE, null)
                }
            }

            override fun onError(error: VolleyError) {
                if (dialogHelper.dialogIsLoading()) dialogHelper.dismissLoadingDialog()
                if (error.networkResponse != null) {
                    when (error.networkResponse.statusCode) {
                        400 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_400), SweetAlertDialog.ERROR_TYPE, null)}
                        500 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_500), SweetAlertDialog.ERROR_TYPE, null)}
                    }
                }
            }

            override val params: Map<String, String>
                get() = mapOf()
        })
    }

    private fun initScene04() {
        syncChat()
        chatAdapter = ChatAdapter(this@ChatActivity)

        binding.groupChatScene4RecyclerView.apply {
            val linearLayoutManager = LinearLayoutManager(this@ChatActivity)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager = linearLayoutManager
            hasFixedSize()
            adapter = chatAdapter
        }

        val chatRoom = currentLeader + "聊天室"
        binding.groupChatTitle.text = chatRoom

        binding.groupChatScene3.fadeOut(500L, 0L)
        Handler(Looper.getMainLooper()).postDelayed({
            binding.groupChatScene3.visibility = View.GONE
            binding.groupChatScene4.fadeIn(500L, 0L)
            binding.groupChatScene4.visibility = View.VISIBLE
        }, 500L)

        viewHelper.setupUI(binding.groupChatScene3RecyclerView)
    }

    @SuppressLint("SetTextI18n")
    private fun initScene03() {
        if (!dialogHelper.dialogIsLoading()) dialogHelper.loadingDialog()

        getChatLeader()

        chatLeaderAdapter = ChatLeaderAdapter(object : ChatLeaderAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val chatLeader = chatLeaderAdapter.differ.currentList[position]

                dialogHelper.sweetBtnDialog(
                    title = "確認選擇此隊長${chatLeader.leader_name}的聊天室嗎？",
                    message = "",
                    positiveText = getString(R.string.alert_positive),
                    negativeText = getString(R.string.alert_negative),
                    cancelable = false,
                    status = SweetAlertDialog.WARNING_TYPE,
                    object : DialogHelper.SweetDialogListener {
                        override fun onPositiveClick(dialog: SweetAlertDialog) {
                            dialog.dismiss()
                            currentLeader = chatLeader.leader_name
                            chatID = chatLeader.groupChat_id
                            chatRoomName = chatLeader.chat_Title
                            initScene04()
                        }

                        override fun onNegativeClick(dialog: SweetAlertDialog) {
                            dialog.dismiss()
                        }
                    }
                )
            }
        })

        binding.groupChatScene3RecyclerView.apply {
            val linearLayoutManager = LinearLayoutManager(this@ChatActivity)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager = linearLayoutManager
            hasFixedSize()
            adapter = chatLeaderAdapter
        }

        binding.groupChatTitle.text = "聊天室列表"

        binding.groupChatScene3.fadeOut(500L, 0L)
        Handler(Looper.getMainLooper()).postDelayed({
            binding.groupChatScene2.visibility = View.GONE
            binding.groupChatScene3.fadeIn(500L, 0L)
            binding.groupChatScene3.visibility = View.VISIBLE
        }, 500L)
    }

    private fun initScene02() {
        chatRoomAdapter = ChatRoomAdapter(object : ChatRoomAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                currentTitle = chatRoomList[position].chatTitle
                dialogHelper.sweetBtnDialog(
                    title = "確認查看此標題聊天室${currentTitle}嗎？",
                    message = "",
                    positiveText = getString(R.string.alert_positive),
                    negativeText = getString(R.string.alert_negative),
                    cancelable = false,
                    status = SweetAlertDialog.WARNING_TYPE,
                    object : DialogHelper.SweetDialogListener {
                        override fun onPositiveClick(dialog: SweetAlertDialog) {
                            dialog.dismiss()
                            if (!dialogHelper.dialogIsLoading()) dialogHelper.loadingDialog()
                            initScene03()
                        }

                        override fun onNegativeClick(dialog: SweetAlertDialog) {
                            dialog.dismiss()
                        }
                    }
                )
            }
        })

        binding.groupChatTitle.text = "群組聊天室"

        binding.groupChatScene1.fadeOut(500L, 0L)
        Handler(Looper.getMainLooper()).postDelayed({
            binding.groupChatScene1.visibility = View.GONE
            binding.groupChatScene2.fadeIn(500L, 0L)
            binding.groupChatScene2.visibility = View.VISIBLE
        }, 500L)

        binding.groupChatScene2RecyclerView.apply {
            val linearLayoutManager = LinearLayoutManager(this@ChatActivity)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager = linearLayoutManager
            hasFixedSize()
            adapter = chatRoomAdapter
        }

        initChatRoomList(AppConfig.URL_LIST_TEAMCHAT + "?${AppConfig.API_TEAMDESC_ID}=$teamDescId&${AppConfig.API_CID}=${sharedData.getCID()}")
        chatRoomAdapter.differ.submitList(chatRoomList)
    }

    private fun initScene01() {
        teamDescAdapter = TeamDescAdapter(object : TeamDescAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                dialogHelper.sweetBtnDialog(
                    title = "確認選擇此群組${teamDescList[position].teamDesc_doc}嗎？",
                    message = "",
                    positiveText = getString(R.string.alert_positive),
                    negativeText = getString(R.string.alert_negative),
                    cancelable = false,
                    status = SweetAlertDialog.WARNING_TYPE,
                    object : DialogHelper.SweetDialogListener {
                        override fun onPositiveClick(dialog: SweetAlertDialog) {
                            dialog.dismiss()
                            if (!dialogHelper.dialogIsLoading()) dialogHelper.loadingDialog()
                            teamDescId = teamDescList[position].teamDesc_id
                            initScene02()
                        }

                        override fun onNegativeClick(dialog: SweetAlertDialog) {
                            dialog.dismiss()
                        }
                    }
                )
            }
        })

        binding.groupChatScene1RecyclerView.apply {
            val linearLayoutManager = LinearLayoutManager(this@ChatActivity)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager = linearLayoutManager
            hasFixedSize()
            adapter = teamDescAdapter
        }

        teamDescAdapter.differ.submitList(teamDescList)
    }

    private fun initTeamDescList() {
        yuuzuApi.api(Request.Method.GET, AppConfig.URL_LIST_TEAMDESC + "?${AppConfig.API_CID}=${sharedData.getCID()}", object :
            YuuzuApi.YuuzuApiListener {
            override fun onSuccess(data: String) {
                if (dialogHelper.dialogIsLoading()) dialogHelper.dismissLoadingDialog()

                try {
                    val jsonArray = JSONArray(data)
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        teamDescList.add(TeamDescDto(
                            teamDesc_id = jsonObject.getString("TD_id"),
                            teamDesc_doc = jsonObject.getString("TD_doc"),
                            time = jsonObject.getString(AppConfig.API_TIME)
                        ))
                    }

                    if (teamDescList.size == 0) {
                        dialogHelper.sweetDialog("目前沒有群組，請先創建群組后在操作此畫面！", SweetAlertDialog.ERROR_TYPE, object :
                            DialogHelper.SweetDialogPositiveListener {
                            override fun onPositiveClick(dialog: SweetAlertDialog) {
                                dialog.dismiss()
                                theEnd()
                                finish()
                                return
                            }
                        })
                    }

                    initScene01()

                } catch (e : JSONException) {
                    dialogHelper.sweetDialog(getString(R.string.alert_error_title_json), SweetAlertDialog.ERROR_TYPE, null)
                }
            }

            override fun onError(error: VolleyError) {
                if (dialogHelper.dialogIsLoading()) dialogHelper.dismissLoadingDialog()
                if (error.networkResponse != null) {
                    when (error.networkResponse.statusCode) {
                        400 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_400), SweetAlertDialog.ERROR_TYPE, null)}
                        500 -> {dialogHelper.sweetDialog(getString(R.string.alert_error_500), SweetAlertDialog.ERROR_TYPE, null)}
                    }
                }
            }

            override val params: Map<String, String>
                get() = mapOf()
        })
    }

    private fun initButton() {
        binding.groupChatBtnBack.setOnClickListener {
            dialogHelper.sweetBtnDialog(
                title = getString(R.string.alert_quit),
                message = "",
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

        binding.groupChatScene2CreateBtn.setOnClickListener {
            dialogHelper.sweetBtnDialog(
                title = "請問是否要爲學生創建聊天室？",
                message = "",
                positiveText = getString(R.string.alert_positive),
                negativeText = getString(R.string.alert_negative),
                cancelable = false,
                status = SweetAlertDialog.WARNING_TYPE,
                object : DialogHelper.SweetDialogListener {
                    override fun onPositiveClick(dialog: SweetAlertDialog) {
                        dialog.dismiss()
                        dialogHelper.inputDialog("創建聊天室", "輸入聊天室主題", object :
                            DialogHelper.CustomTextListener {
                            override fun onPositiveTextClick(
                                var1: View,
                                dialog: AlertDialog,
                                inputText: TextInputEditText
                            ) {
                                dialog.dismiss()
                                if (inputText.text.toString().isNotBlank()) {
                                    dialog.dismiss()
                                    if (!dialogHelper.dialogIsLoading()) dialogHelper.loadingDialog()
                                    chatRoomCreate(inputText.text.toString())
                                } else {
                                    dialogHelper.sweetDialog(getString(R.string.toast_NOT_EMPTY), SweetAlertDialog.ERROR_TYPE, null)
                                }
                            }

                            override fun onNegativeTextClick(
                                var1: View,
                                dialog: AlertDialog
                            ) {
                                dialog.dismiss()
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
        binding.groupChatTitle.text = "選擇群組"

        yuuzuApi = YuuzuApi(this)
        sharedData = SharedData(this)
        viewHelper = ViewHelper(this)
        dialogHelper = DialogHelper(this)
        database = FirebaseDatabase.getInstance(AppConfig.FIREBASE_URL)

        if (!dialogHelper.dialogIsLoading()) dialogHelper.loadingDialog()

        teamDescList = ArrayList()
        chatRoomList = ArrayList()
        chatLeaderList = ArrayList()
        chatList = ArrayList()
    }

    private fun theEnd() {
        if (dialogHelper.dialogIsLoading()) dialogHelper.dismissLoadingDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        theEnd()
    }
}