package com.safepath.app.ui.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class HomeUiState(
    val source: String      = "",
    val destination: String = "",
    val sourceError: String?      = null,
    val destinationError: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onSourceChanged(value: String) {
        _uiState.update { it.copy(source = value, sourceError = null) }
    }

    fun onDestinationChanged(value: String) {
        _uiState.update { it.copy(destination = value, destinationError = null) }
    }

    fun swapLocations() {
        _uiState.update { state ->
            state.copy(source = state.destination, destination = state.source)
        }
    }

    /**
     * Returns true if validation passes (caller can proceed to route search).
     */
    fun validate(): Boolean {
        val state = _uiState.value
        var valid = true
        if (state.source.isBlank()) {
            _uiState.update { it.copy(sourceError = "Enter starting location") }
            valid = false
        }
        if (state.destination.isBlank()) {
            _uiState.update { it.copy(destinationError = "Enter destination") }
            valid = false
        }
        if (state.source.trim().equals(state.destination.trim(), ignoreCase = true)) {
            _uiState.update { it.copy(destinationError = "Destination must differ from source") }
            valid = false
        }
        return valid
    }
}
