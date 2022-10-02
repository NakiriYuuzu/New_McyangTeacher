package tw.edu.teachermcyang.activity.main.group_chat

data class ChatRoomDto(
    val groupChat_id: String,
    val teamLeader_id: String,
    val teamDesc_id: String,
    val c_id: String,
    val chatTitle: String,
    val s_name: String,
    val crtTime: String,
)