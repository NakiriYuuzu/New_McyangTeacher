package tw.edu.teachermcyang.activity.main.race

data class RaceDto(
    val rlId: String,
    val answer: String, // 0 未批改 1 正確 99 錯誤
    val sName: String,
    val studentID: String
)