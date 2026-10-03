package com.hfm.f2mtv.data.model

data class DownloadLink(
    val url: String,
    val quality: String? = null,
    val category:String? = null
)
