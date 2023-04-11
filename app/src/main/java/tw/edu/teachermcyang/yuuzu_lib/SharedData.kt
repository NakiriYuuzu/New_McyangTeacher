package tw.edu.teachermcyang.yuuzu_lib

import android.app.Activity
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.GsonBuilder
import tw.edu.teachermcyang.activity.main.home.model.HomeDto
import tw.edu.teachermcyang.activity.main.sign.model.SignDto

class SharedData(activity: Activity) {

    val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

    val keySignList = "SignList"
    val askingList = "AskingList"

    fun <T> put(`object`: T, key: String) {
        val jsonString = GsonBuilder().create().toJson(`object`)
        pref.edit().putString(key, jsonString).apply()
    }

    inline fun <reified T> get(key: String): T? {
        val value = pref.getString(key, null)
        return GsonBuilder().create().fromJson(value, T::class.java)
    }

    fun logout() {
        saveID("")
        saveName("")
        quitCourse()
    }

    fun quitCourse() {
        saveSignID("")
        saveCID("")
        saveCourseName("")
        saveSignPeople("")
        saveSignDate("")
        put(ArrayList<SignDto>(), keySignList)
        put(ArrayList<String>(), askingList)
    }

    fun saveSplashStatus(status: Boolean) {
        val editor = pref.edit()
        editor.putBoolean("splash_status", status)
        editor.apply()
    }

    fun getSplashStatus(): Boolean {
        return pref.getBoolean("splash_status", false)
    }

    fun saveLoginAcc(account: String) {
        val editor = pref.edit()
        editor.putString("login_acc", account)
        editor.apply()
    }

    fun getLoginAcc(): String {
        return pref.getString("login_acc", null).toString()
    }

    fun saveLoginPwd(password: String) {
        val editor = pref.edit()
        editor.putString("login_pwd", password)
        editor.apply()
    }

    fun getLoginPwd(): String {
        return pref.getString("login_pwd", null).toString()
    }

    fun saveID(id: String) {
        val editor = pref.edit()
        editor.putString("id", id)
        editor.apply()
    }

    fun getID(): String {
        return pref.getString("id", null).toString()
    }

    fun saveName(name: String) {
        val editor = pref.edit()
        editor.putString("name", name)
        editor.apply()
    }

    fun getName(): String {
        return pref.getString("name", null).toString()
    }

    fun saveSignID(signId: String) {
        val editor = pref.edit()
        editor.putString("signId", signId)
        editor.apply()
    }

    fun getSignID(): String {
        return pref.getString("signId", null).toString()
    }

    fun saveCID(courseId: String) {
        val editor = pref.edit()
        editor.putString("C_id", courseId)
        editor.apply()
    }

    fun getCID(): String {
        return pref.getString("C_id", null).toString()
    }

    fun saveSignDate(signDate: String) {
        val editor = pref.edit()
        editor.putString("signDate", signDate)
        editor.apply()
    }

    fun getSignDate(): String {
        return pref.getString("signDate", null).toString()
    }

    fun saveCourseName(courseName: String) {
        val editor = pref.edit()
        editor.putString("C_name", courseName)
        editor.apply()
    }

    fun getCourseName(): String {
        return pref.getString("C_name", null).toString()
    }

    fun saveSignPeople(people: String) {
        val editor = pref.edit()
        editor.putString("people", people)
        editor.apply()
    }

    fun getSignPeople(): String {
        return pref.getString("people", null).toString()
    }

    fun saveRaceId(raceId: String) {
        val editor = pref.edit()
        editor.putString("Race_id", raceId)
        editor.apply()
    }

    fun getRaceId(): String {
        return pref.getString("Race_id", null).toString()
    }
}