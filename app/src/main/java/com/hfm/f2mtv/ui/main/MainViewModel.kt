package com.hfm.f2mtv.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfm.f2mtv.data.model.Movie
import com.hfm.f2mtv.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadMovies(page = 1)
    }

    fun loadMovies(page: Int = 1, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (page == 1) {
                _uiState.update {
                    it.copy(
                        isLoading = !isRefresh,
                        isRefreshing = isRefresh,
                        error = null
                    )
                }
            } else {
                if (_uiState.value.isLoadingMore) return@launch
                _uiState.update { it.copy(isLoadingMore = true, error = null) }
            }

            repository.fetchMovies(page)
                .onSuccess { result ->
                    _uiState.update { state ->
                        val updatedList = if (page == 1) {
                            result.movies
                        } else {
                            (state.movies + result.movies).distinctBy { it.id }
                        }

                        state.copy(
                            movies = updatedList,
                            isLoading = false,
                            isLoadingMore = false,
                            isRefreshing = false,
                            currentPage = result.currentPage,
                            totalPages = result.totalPages,
                            hasNextPage = result.hasNextPage,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            isRefreshing = false,
                            error = exception.localizedMessage ?: "Failed to load movies"
                        )
                    }
                }
        }
    }

    fun loadNextPage() {
        val currentState = _uiState.value
        if (!currentState.isLoading && !currentState.isLoadingMore && currentState.hasNextPage) {
            loadMovies(page = currentState.currentPage + 1)
        }
    }

    fun refresh() {
        loadMovies(page = 1, isRefresh = true)
    }

    fun selectMovie(movie: Movie?) {
        _uiState.update { it.copy(selectedMovie = movie) }
    }

    fun filterByGenre(genre: String?) {
        _uiState.update {
            val newGenre = if (it.selectedGenreFilter == genre) null else genre
            it.copy(selectedGenreFilter = newGenre)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
