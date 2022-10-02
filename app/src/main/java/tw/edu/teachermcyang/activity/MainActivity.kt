package tw.edu.teachermcyang.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.Region
import org.json.JSONArray
import tw.edu.teachermcyang.AppConfig
import tw.edu.teachermcyang.R
import tw.edu.teachermcyang.activity.main.home.model.HomeDto
import tw.edu.teachermcyang.activity.main.sign.model.SignDto
import tw.edu.teachermcyang.yuuzu_lib.BeaconController
import tw.edu.teachermcyang.yuuzu_lib.SharedData

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
        var isAsking = false
    }

    private lateinit var homeList: ArrayList<HomeDto>
    private lateinit var homeOldList: ArrayList<HomeDto>

    private lateinit var beaconController: BeaconController
    private lateinit var sharedData: SharedData

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navHostFragment: FragmentContainerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
    }

    private fun studentAsking(beacon: Beacon?) {
        if (!isAsking) {
            isAsking = true

            val jsonArray = JSONArray(sharedData.get<ArrayList<SignDto>>(sharedData.keySignList))
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val sid = jsonObject.getString(AppConfig.API_SID)
                val sName = jsonObject.getString(AppConfig.API_SNAME)
                val studentID = jsonObject.getString("StudentID")

                if (beacon?.id3.toString() == sid) {
                    val home = HomeDto(sid, sName, studentID)
                    if (homeList.any { it.S_id == sid }) return
                    if (homeOldList.any{ it.S_id == sid }) return
                    homeList.add(home)
                    break
                }
            }
            val home = homeList[0]

            homeOldList.add(home)

            Log.e(TAG, "studentAsking: ")

            Snackbar.make(navHostFragment, home.S_name + "同學，正在提問中", Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.alert_positive) {
                    homeList.remove(home)
                    try {
                        Handler(Looper.getMainLooper()).postDelayed({
                            homeOldList.remove(home)
                        }, 15000)

                    } catch (e: Exception) {
                        Log.e(TAG, e.toString())
                    }

                    isAsking = false
                }
                .show()
        }
    }

    private fun beaconScanning() {
        if (!beaconController.isScanning()) {
            if (sharedData.getSignID() == "null" || sharedData.getSignID().isEmpty()) {
                return
            }

            if (sharedData.getSignID() != "null" && sharedData.getSignID().isNotBlank()) {
                beaconController.startScanning(object : BeaconController.BeaconModify {
                    override fun modifyData(beacons: Collection<Beacon?>?, region: Region?) {
                        if (sharedData.getSignID() == "null" || sharedData.getSignID().isEmpty()) {
                            beaconController.stopScanning()
                            return
                        }

                        beacons?.forEach { it ->
                            if (it?.id2.toString() == sharedData.getSignID()) {
                                studentAsking(it)
                            }
                        }
                        Log.e(TAG, "modifyData: $beacons | ${homeList.size} | $isAsking")
                    }
                })
            }
        }
    }

    private fun initView() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        navHostFragment = findViewById(R.id.fragment)
        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())

        homeList = ArrayList()
        homeOldList = ArrayList()

        sharedData = SharedData(this)
        beaconController = BeaconController(this, Region("Main", Identifier.parse(AppConfig.BEACON_UUID_MAIN), null, null))
    }

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            beaconScanning()
        }, 1500)

        Log.e(TAG, "onResume: ${sharedData.getCID()} | ${sharedData.getSignID()}")
    }

    override fun onPause() {
        super.onPause()
        if (beaconController.isScanning())
            beaconController.stopScanning()
    }

    override fun onStop() {
        super.onStop()
        if (beaconController.isScanning())
            beaconController.stopScanning()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (beaconController.isScanning())
            beaconController.stopScanning()
    }
}