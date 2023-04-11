package tw.edu.teachermcyang.activity.main.home.model

data class HomeDto(
    val S_id: String,
    val S_name: String,
    val StudentID: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HomeDto

        if (S_id != other) return false
        if (S_name != other) return false
        if (StudentID != other) return false

        return true
    }

    override fun hashCode(): Int {
        var result = S_id.hashCode()
        result = 31 * result + S_name.hashCode()
        result = 31 * result + StudentID.hashCode()
        return result
    }
}
