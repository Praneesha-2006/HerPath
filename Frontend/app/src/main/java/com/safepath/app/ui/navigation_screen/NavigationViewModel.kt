package com.safepath.app.ui.navigation_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safepath.app.data.model.NavigationStep
import com.safepath.app.data.model.Route
import com.safepath.app.data.repository.RouteRepository
import com.safepath.app.ui.routes.RouteViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class NavigationUiState(
    val route: Route? = null,
    val currentStepIndex: Int = 0,
    val isNavigating: Boolean = false,
    val isArrived: Boolean = false
) {
    val currentStep: NavigationStep?
        get() = route?.steps?.getOrNull(currentStepIndex)

    val nextStep: NavigationStep?
        get() = route?.steps?.getOrNull(currentStepIndex + 1)

    val progress: Float
        get() {
            val total = route?.steps?.size ?: 1
            return (currentStepIndex.toFloat() / total.toFloat()).coerceIn(0f, 1f)
        }

    val stepsRemaining: Int
        get() = ((route?.steps?.size ?: 0) - currentStepIndex).coerceAtLeast(0)
}

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NavigationUiState())
    val uiState: StateFlow<NavigationUiState> = _uiState.asStateFlow()

    fun loadRoute(routeId: Int, routeViewModel: RouteViewModel) {
        // First try cached routes from RouteViewModel
        val cached = routeViewModel.getRouteById(routeId)
        if (cached != null) {
            _uiState.update { it.copy(route = cached, isNavigating = true) }
        } else {
            // Fallback: load mock directly
            val mockRoute = routeRepository.getMockRoutes("", "").find { it.id == routeId }
                ?: routeRepository.getMockRoutes("", "").first()
            _uiState.update { it.copy(route = mockRoute, isNavigating = true) }
        }
    }

    fun nextStep() {
        val state = _uiState.value
        val maxIndex = (state.route?.steps?.size ?: 1) - 1
        if (state.currentStepIndex >= maxIndex) {
            _uiState.update { it.copy(isArrived = true) }
        } else {
            _uiState.update { it.copy(currentStepIndex = it.currentStepIndex + 1) }
        }
    }

    fun previousStep() {
        _uiState.update { it.copy(currentStepIndex = (it.currentStepIndex - 1).coerceAtLeast(0)) }
    }

    fun dismissArrival() {
        _uiState.update { it.copy(isArrived = false) }
    }
}
