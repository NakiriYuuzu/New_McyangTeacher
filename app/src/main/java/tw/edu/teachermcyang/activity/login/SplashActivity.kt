package tw.edu.teachermcyang.activity.login

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import tw.edu.teachermcyang.R
import tw.edu.teachermcyang.databinding.ActivitySplashBinding
import tw.edu.teachermcyang.yuuzu_lib.SharedData

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private var totalPage = 0
    private var currentPage = 0
    private val pageList = ArrayList<Int>()

    private lateinit var sharedData: SharedData

    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initPageList()
        initButton()
    }

    private fun initView() {
        sharedData = SharedData(this)
    }

    private fun initButton() {
        binding.splashBtnNext.setOnClickListener {
            when (currentPage) {
                totalPage -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    sharedData.saveSplashStatus(true)
                    finish()

                }
                else -> {
                    currentPage++
                    binding.splashBackgroundImg.setImageResource(pageList[currentPage])
                    binding.splashProgressBar.progress = currentPage

                    if (currentPage > 0) {
                        binding.splashBtnPrev.visibility = android.view.View.VISIBLE
                    }
                }
            }

            Log.e("initButton: ", "$currentPage | $totalPage")
        }

        binding.splashBtnPrev.setOnClickListener {
            if (currentPage != 0) {
                currentPage--

                if (currentPage == 0) {
                    binding.splashBtnPrev.visibility = android.view.View.INVISIBLE
                }

                binding.splashBackgroundImg.setImageResource(pageList[currentPage])
                binding.splashProgressBar.progress = currentPage
            }
        }
    }

    private fun initPageList() {
        pageList.add(R.drawable.teacher1)
        pageList.add(R.drawable.teacher2)
        pageList.add(R.drawable.teacher3)
        pageList.add(R.drawable.teacher4)
        pageList.add(R.drawable.teacher5)
        pageList.add(R.drawable.teacher6)
        pageList.add(R.drawable.teacher7)

        totalPage = pageList.size - 1
    }
}