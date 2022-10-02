package tw.edu.teachermcyang

object AppConfig {
    // TODO: APP version
    const val APP_VERSION = "1.0.0"
    const val DEBUG = false

    const val TAG = "=====MCYANG_DEBUG===== : "

    // TODO: App Settings
    const val TIMEOUT = 7
    const val FIREBASE_URL = "https://newmcyang-default-rtdb.asia-southeast1.firebasedatabase.app/"
    const val FIREBASE_USER = "user"
    const val FIREBASE_MESSAGE = "message"
    const val FIREBASE_DATETIME = "datetime"

    // TODO: Api URL
    private const val URL_API = "http://yuuzu.net:7111"

    const val URL_LOGIN = "$URL_API/api/Login/"
    const val URL_LIST_COURSE = "$URL_API/api/CourseList/"
    const val URL_LIST_SIGN_COURSE = "$URL_API/api/listSignCourse/"
    const val URL_PUSH_SIGN_COURSE = "$URL_API/api/courseSigned/"
    const val URL_CREATE_SIGN = "$URL_API/api/SignCreate/"
    const val URL_LIST_SIGN_RECORD = "$URL_API/api/SignRecordList/"
    const val URL_CREATE_RACE = "$URL_API/api/RaceCreate/"
    const val URL_LIST_RACELIST = "$URL_API/api/RaceListList/"
    const val URL_CREATE_RACELIST = "$URL_API/api/RaceListCreate/"
    const val URL_CREATE_TEAMDESC = "$URL_API/api/TeamDescCreate/"
    const val URL_LIST_TEAMLEADER = "$URL_API/api/TeamLeaderList/"
    const val URL_CREATE_TEAMLEADER = "$URL_API/api/TeamLeaderCreate/"
    const val URL_LIST_TEAMMEMBER = "$URL_API/api/TeamMemberList/"
    const val URL_CREATE_TEAMMEMBER = "$URL_API/api/TeamMemberCreate/"
    const val URL_LIST_TEAMDESC = "$URL_API/api/TeamDescList/"
    const val URL_LIST_TEAMCHAT = "$URL_API/api/TeamChatList/"
    const val URL_CREATE_TEAMCHAT = "$URL_API/api/TeamChatCreate/"

    // TODO: Api Param
    const val API_EMAIL = "T_email"
    const val API_PASSWORD = "T_password"

    const val API_SID = "S_id"
    const val API_SNAME = "S_name"

    const val API_TID = "T_id"
    const val API_TNAME = "T_name"

    const val API_CID = "C_id"
    const val API_CNAME = "C_name"

    const val API_SIGN_ID = "Sign_id"
    const val API_SIGN_DATE = "Date"

    const val API_RACE_ID = "Race_id"
    const val API_RACE_DOC = "Race_doc"
    const val API_STATUS = "Status"
    const val API_TIME = "CrtTime"

    const val API_TEAMDESC_ID = "TeamDesc_id"
    const val API_TEAMLEADER_ID = "TeamLeader_id"
    const val API_TEAMMEMBER_ID = "TeamMember_id"

    // TODO: WebSocket Url
    const val WS_RACELIST = "ws://yuuzu.net:7111/ws/race-server/"
    const val WS_SIGNLIST = "ws://yuuzu.net:7111/ws/sign-server/"
    const val WS_GROUPLIST = "ws://yuuzu.net:7111/ws/group-server/"

    // TODO: Beacon Identifier
    const val BEACON_UUID_SIGN = "2f234454-cf6d-4a0f-adf2-f4911ba9ffa6"
    const val BEACON_UUID_RACE = "2f234454-cf6d-4a0f-adf2-f4911ba9ffa5"
    const val BEACON_UUID_MAIN = "2f234454-cf6d-4a0f-adf2-f4911ba9ffa4"
    const val BEACON_UUID_ANSWER = "2f234454-cf6d-4a0f-adf2-f4911ba9ffa3"
    const val BEACON_UUID_LEADER = "2f234454-cf6d-4a0f-adf2-f4911ba9ffa2"
    const val BEACON_UUID_MEMBER = "2f234454-cf6d-4a0f-adf2-f4911ba9ffa1"
}