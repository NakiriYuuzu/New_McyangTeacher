package tw.edu.teachermcyang.activity.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.android.volley.Request
import com.android.volley.VolleyError
import org.json.JSONObject
import tw.edu.teachermcyang.yuuzu_lib.permission.PermissionHelper
import tw.edu.teachermcyang.AppConfig
import tw.edu.teachermcyang.R
import tw.edu.teachermcyang.activity.MainActivity
import tw.edu.teachermcyang.databinding.ActivityLoginBinding
import tw.edu.teachermcyang.yuuzu_lib.DialogHelper
import tw.edu.teachermcyang.yuuzu_lib.SharedData
import tw.edu.teachermcyang.yuuzu_lib.ViewHelper
import tw.edu.teachermcyang.yuuzu_lib.YuuzuApi

class LoginActivity : AppCompatActivity() {

    companion object {
        const val TAG = "LoginActivity"
    }

    lateinit var yuuzuApi: YuuzuApi
    lateinit var sharedData: SharedData
    lateinit var viewHelper: ViewHelper
    lateinit var dialogHelper: DialogHelper
    lateinit var permissionHelper: PermissionHelper

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initLottie()
        buttonController()
    }

    private fun initView() {
        yuuzuApi = YuuzuApi(this)
        sharedData = SharedData(this)
        viewHelper = ViewHelper(this)
        dialogHelper = DialogHelper(this)
        permissionHelper = PermissionHelper(this)

        if (!sharedData.getSplashStatus()) {
            binding.loginMainLayout.visibility = View.GONE
            startActivity(Intent(this, SplashActivity::class.java))
        }

        viewHelper.setupUI(findViewById(R.id.loginHeader_CardView1))
        viewHelper.setupUI(findViewById(R.id.loginHeader_CardView2))

        permissionHelper.checkALL()

        if (sharedData.getLoginAcc() != "null" && sharedData.getLoginPwd() != "null") {
            binding.loginInputAcc.setText(sharedData.getLoginAcc())
            binding.loginInputPass.setText(sharedData.getLoginPwd())
            binding.loginCheckBoxRememberMe.isChecked = true
            // FIXME : REMOVE THIS AFTER FINISH DEV
            if (AppConfig.DEBUG) login()
        }
    }

    private fun initLottie() {
        binding.loginAnimation.setAnimation(R.raw.welcome)
        binding.loginAnimation.playAnimation()
    }

    private fun buttonController() {
        binding.loginBtnSignIn.setOnClickListener {
            login()
        }
    }

    fun login() {
        dialogHelper.loadingDialog()

        val acc = binding.loginInputAcc.text.toString()
        val pass = binding.loginInputPass.text.toString()

        if (acc.isNotBlank() && acc != "" && pass.isNotBlank() && pass != "") {
            yuuzuApi.api(Request.Method.POST, AppConfig.URL_LOGIN, object :
                YuuzuApi.YuuzuApiListener {
                override fun onSuccess(data: String) {
                    val jsonObject = JSONObject(data)

                    val id = jsonObject.get(AppConfig.API_TID).toString()
                    val name = jsonObject.get(AppConfig.API_TNAME).toString()

                    if (binding.loginCheckBoxRememberMe.isChecked) {
                        sharedData.saveLoginAcc(acc)
                        sharedData.saveLoginPwd(pass)
                    }

                    sharedData.saveID(id)
                    sharedData.saveName(name)

                    Handler(Looper.getMainLooper()).postDelayed({
                        if (dialogHelper.dialogIsLoading())
                            dialogHelper.dismissLoadingDialog()

                        Intent(this@LoginActivity, MainActivity::class.java).apply {
                            startActivity(this)
                        }
                    }, 1000)
                }

                override fun onError(error: VolleyError) {
                    Log.e(TAG, "onError: $error")
                    if (dialogHelper.dialogIsLoading())
                        dialogHelper.dismissLoadingDialog()

                    if (error.networkResponse != null) {
                        if (error.networkResponse.statusCode == 400) {
                            dialogHelper.showDialog(
                                getString(R.string.login_error_wrongInput),
                                error.networkResponse.statusCode.toString()
                            )
                        } else {
                            dialogHelper.showDialog(
                                getString(R.string.alert_error_json),
                                error.networkResponse.statusCode.toString()
                            )
                        }
                    } else {
                        dialogHelper.showDialog(getString(R.string.login_error_noInternet), "")
                    }
                }

                override val params: Map<String, String>
                    get() = mapOf(
                        AppConfig.API_EMAIL to acc,
                        AppConfig.API_PASSWORD to pass
                    )
            })
        } else {
            if (dialogHelper.dialogIsLoading())
                dialogHelper.dismissLoadingDialog()

            dialogHelper.showDialog(getString(R.string.login_error_empty), "")
        }
    }
}