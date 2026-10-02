package com.hfm.f2mtv.data.model

data class DownloadLink(
    val title: String,
    val url: String,
    val quality: String? = null,
    val size: String? = null
)
