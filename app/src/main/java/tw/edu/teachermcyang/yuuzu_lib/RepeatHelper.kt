package tw.edu.teachermcyang.yuuzu_lib

import android.os.Handler
import android.os.Looper
import java.lang.Exception

class RepeatHelper(newRunner: NewRunner) {
    private var started = false
    private var timer = 0
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val runnable: Runnable
    fun stop() {
        started = false
        handler.removeCallbacks(runnable)
    }

    fun start(time: Int) {
        timer = time
        started = true
        handler.postDelayed(runnable, timer.toLong())
    }

    fun isRepeating(): Boolean {
        return started
    }

    interface NewRunner {
        fun newRunner()
    }

    init {
        runnable = Runnable {
            try {
                newRunner.newRunner()
                if (started) start(timer)
            } catch (e: ExceptionInInitializerError) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}