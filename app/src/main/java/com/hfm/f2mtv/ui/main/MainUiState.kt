package com.hfm.f2mtv.ui.main

import com.hfm.f2mtv.data.model.DownloadLink
import com.hfm.f2mtv.data.model.Movie

data class MainUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasNextPage: Boolean = false,
    val error: String? = null,
    val selectedMovie: Movie? = null,
    val isLoadingDownloadLinks: Boolean = false,
    val downloadLinks: List<DownloadLink> = emptyList(),
    val downloadLinksError: String? = null,
    val activePlayingUrl: String? = null,
    val activePlayingTitle: String? = null,
    val selectedGenreFilter: String? = null,
    val searchQuery: String = ""
) {
    val filteredMovies: List<Movie>
        get() {
            var list = movies
            if (!selectedGenreFilter.isNullOrBlank()) {
                list = list.filter { movie ->
                    movie.genres.any { it.equals(selectedGenreFilter, ignoreCase = true) }
                }
            }
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim()
                list = list.filter { movie ->
                    movie.title.contains(q, ignoreCase = true) ||
                            movie.farsiTitle.contains(q, ignoreCase = true)
                }
            }
            return list
        }

}
