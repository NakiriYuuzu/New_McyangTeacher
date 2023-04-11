package tw.edu.teachermcyang.activity.main.sign.model

data class SignDto(
    val S_id: String,
    val S_name: String,
    val StudentID: String,
    var status: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SignDto

        if (S_id != other) return false
        if (S_name != other) return false
        if (status != other) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + S_id.hashCode()
        result = 31 * result + S_name.hashCode()
        return result
    }
}
