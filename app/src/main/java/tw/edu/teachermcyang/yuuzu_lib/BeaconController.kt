package tw.edu.teachermcyang.yuuzu_lib

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import org.altbeacon.beacon.*
import tw.edu.teachermcyang.R

class BeaconController(
    val activity: Activity,
    var region: Region
) {
    companion object {
        private const val DEFAULT_FOREGROUND_SCAN_PERIOD = 1000L
    }

    private lateinit var beacon: Beacon
    private var beaconManager: BeaconManager = BeaconManager.getInstanceForApplication(activity)
    private var beaconTransmitter: BeaconTransmitter? = null
    private var beaconParser: BeaconParser? = null

    private var beaconIsScanning = false
    private var beaconIsCasting = false

    private var dialogHelper: DialogHelper = DialogHelper(activity)

    init {
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"))
        beaconManager.foregroundScanPeriod = DEFAULT_FOREGROUND_SCAN_PERIOD
        beaconParser = BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25")
    }

    fun fixLollipop() {
        val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        bluetoothManager.adapter.disable()
        Handler(Looper.getMainLooper()).postDelayed({
            bluetoothManager.adapter.enable()
        }, 2500)
    }

    fun startScanning(beaconModify: BeaconModify) {
        beaconManager.removeAllRangeNotifiers()
        beaconManager.addRangeNotifier(beaconModify::modifyData)
        beaconManager.startRangingBeacons(region)
        beaconIsScanning = true
    }

    fun stopScanning() {
        beaconManager.removeAllMonitorNotifiers()
        beaconManager.stopRangingBeacons(region)
        beaconManager.removeAllRangeNotifiers()
        beaconIsScanning = false
    }

    fun isScanning() :Boolean {
        return beaconIsScanning
    }

//    fun beaconInit(url: String?) {
//        beaconManager = BeaconManager.getInstanceForApplication(ctx)
//        region = Region("UniqueID", Identifier.parse(url), null, null)
//        //beacon AddStone m:0-3=4c000215 or alt beacon = m:2-3=0215
//        beaconManager!!.beaconParsers.add(beaconParser)
//        beaconManager!!.foregroundScanPeriod = DEFAULT_FOREGROUND_SCAN_PERIOD
//    }
//
//    fun startScanning(beaconModify: BeaconModify) {
//        beaconManager!!.addRangeNotifier { beacons: Collection<Beacon?>?, region: Region? ->
//            beaconModify.modifyData(
//                beacons,
//                region
//            )
//        }
//        beaconManager!!.startRangingBeacons(region!!)
//    }

    fun broadcastBeacon(uuid: String, major: String, minor: String) {
        try {
            if (major.isNotBlank() && major != "null" && minor.isNotBlank() && minor != "null") {
                beacon = Beacon.Builder()
                    .setId1(uuid)
                    .setId2(major)
                    .setId3(minor)
                    .setManufacturer(0x0118)
                    .setTxPower(-69)
                    .setDataFields(listOf(0L))
                    .build()

                beaconTransmitter = BeaconTransmitter(activity, beaconParser)
                beaconTransmitter!!.advertiseTxPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH
                beaconTransmitter!!.advertiseMode = AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
            } else {
                dialogHelper.showDialog(activity.getString(R.string.alert_beacon_NoMajorMinor), "")
            }
        } catch (e: Exception) {
            dialogHelper.showDialog(activity.getString(R.string.alert_beacon_Exception), e.message.toString())
        }
    }

    fun startBeaconCasting() {
        beaconTransmitter?.startAdvertising(beacon)
        beaconIsCasting = true
    }

    fun stopBeaconCasting() {
        beaconTransmitter?.stopAdvertising()
        beaconIsCasting = false
    }

    fun isBeaconCasting(): Boolean {
        return beaconIsCasting
    }

    interface BeaconModify {
        fun modifyData(beacons: Collection<Beacon?>?, region: Region?)
    }
}