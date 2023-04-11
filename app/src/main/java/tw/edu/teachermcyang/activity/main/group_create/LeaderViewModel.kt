package tw.edu.teachermcyang.activity.main.group_create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 針對LeaderViewModel使用了Flow 流程，可以前往此網站查看更詳細的内容。
 * https://developer.android.com/kotlin/flow/stateflow-and-sharedflow
 */
class LeaderViewModel : ViewModel() {
    private val _leaderUiState = MutableStateFlow<LeaderUiState>(LeaderUiState.Empty)
    val leaderUiState: StateFlow<LeaderUiState> = _leaderUiState

    fun leader(leaderList: ArrayList<LeaderDto>) = viewModelScope.launch {
        _leaderUiState.value = LeaderUiState.Loading

        if (leaderList.size > 0) {
            _leaderUiState.value = LeaderUiState.Success
        } else {
            _leaderUiState.value = LeaderUiState.Error("Error")
        }
    }

    sealed class LeaderUiState {
        object Empty : LeaderUiState()
        object Loading : LeaderUiState()
        object Success: LeaderUiState()
        data class Error(val error: String) : LeaderUiState()
    }
}