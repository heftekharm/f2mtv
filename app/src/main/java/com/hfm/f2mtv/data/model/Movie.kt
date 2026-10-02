package com.hfm.f2mtv.data.model

data class Movie(
    val id: String,
    val title: String,
    val farsiTitle: String,
    val link: String,
    val imageUrl: String,
    val genres: List<String>,
    val isDubbed: Boolean,
    val hasSubtitle: Boolean,
    val updateInfo: String? = null
)

data class MoviePageResult(
    val movies: List<Movie>,
    val currentPage: Int,
    val totalPages: Int,
    val hasNextPage: Boolean
)
