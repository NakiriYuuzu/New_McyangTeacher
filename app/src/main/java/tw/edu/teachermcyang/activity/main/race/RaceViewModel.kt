package tw.edu.teachermcyang.activity.main.race

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import tw.edu.teachermcyang.AppConfig

class RaceViewModel : ViewModel() {
    private val _raceUiState = MutableStateFlow<RaceUiState>(RaceUiState.Empty)
    val raceUiState: StateFlow<RaceUiState> = _raceUiState

    fun race(raceList: ArrayList<RaceDto>) = viewModelScope.launch {
        _raceUiState.value = RaceUiState.Loading

        if (raceList.size > 0) {
            _raceUiState.value = RaceUiState.Success
        } else {
            _raceUiState.value = RaceUiState.Error("錯誤")
        }
    }

    sealed class RaceUiState {
        object Success : RaceUiState()
        data class Error(val message: String) : RaceUiState()
        object Loading : RaceUiState()
        object Empty : RaceUiState()
    }

    fun startRepeatingJob(): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                // add your task here
                Log.e(AppConfig.TAG, "startRepeatingJob: ")
                delay(1000L)
            }
        }
    }
}