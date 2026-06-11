package com.safepath.app.ui.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safepath.app.data.model.Route
import com.safepath.app.data.repository.RouteRepository
import com.safepath.app.data.repository.RouteResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RoutesUiState {
    object Loading : RoutesUiState()
    data class Success(
        val routes: List<Route>,
        val selectedRouteId: Int = 0
    ) : RoutesUiState()
    data class Error(val message: String) : RoutesUiState()
}

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RoutesUiState>(RoutesUiState.Loading)
    val uiState: StateFlow<RoutesUiState> = _uiState.asStateFlow()

    // Cache routes so NavigationScreen can look them up by ID
    private val _cachedRoutes = MutableStateFlow<List<Route>>(emptyList())
    val cachedRoutes: StateFlow<List<Route>> = _cachedRoutes.asStateFlow()

    fun loadRoutes(source: String, destination: String) {
        if (_cachedRoutes.value.isNotEmpty()) return  // avoid re-fetch on recompose
        viewModelScope.launch {
            _uiState.value = RoutesUiState.Loading
            when (val result = routeRepository.getRoutes(source, destination)) {
                is RouteResult.Success -> {
                    _cachedRoutes.value = result.routes
                    _uiState.value = RoutesUiState.Success(
                        routes          = result.routes,
                        selectedRouteId = result.routes.firstOrNull()?.id ?: 0
                    )
                }
                is RouteResult.Error -> {
                    _uiState.value = RoutesUiState.Error(result.message)
                }
            }
        }
    }

    fun selectRoute(routeId: Int) {
        val current = _uiState.value as? RoutesUiState.Success ?: return
        _uiState.update { current.copy(selectedRouteId = routeId) }
    }

    fun getRouteById(id: Int): Route? = _cachedRoutes.value.find { it.id == id }
}
