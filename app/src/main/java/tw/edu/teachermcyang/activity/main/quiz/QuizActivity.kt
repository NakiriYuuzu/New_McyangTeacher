package tw.edu.teachermcyang.activity.main.quiz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import tw.edu.teachermcyang.databinding.ActivityQuizBinding
import tw.edu.teachermcyang.yuuzu_lib.YuuzuApi

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding

    private lateinit var yuuzuApi: YuuzuApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        yuuzuApi = YuuzuApi(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }
}