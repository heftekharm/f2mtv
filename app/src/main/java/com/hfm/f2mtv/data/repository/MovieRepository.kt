package com.hfm.f2mtv.data.repository

import com.hfm.f2mtv.data.model.DownloadLink
import com.hfm.f2mtv.data.model.Movie
import com.hfm.f2mtv.data.model.MoviePageResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class MovieRepository {

    private val baseUrl = "https://www.f2mb.top/movies/"
    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    suspend fun fetchMovies(page: Int = 1): Result<MoviePageResult> = withContext(Dispatchers.IO) {
        runCatching {
            val targetUrl = if (page <= 1) {
                baseUrl
            } else {
                "${baseUrl}page/$page/"
            }

            val doc = Jsoup.connect(targetUrl)
                .userAgent(userAgent)
                .timeout(15000)
                .followRedirects(true)
                .get()

            val articles = doc.select("article.entry")
            val movies = articles.mapNotNull { parseArticle(it) }

            // Parse pagination info
            val pagination = doc.select("div.pagination")
            var totalPages = page
            var hasNext = false

            if (pagination.isNotEmpty()) {
                val pageLinks = pagination.select(".page-numbers")
                for (p in pageLinks) {
                    val pageNum = p.text().trim().toIntOrNull()
                    if (pageNum != null && pageNum > totalPages) {
                        totalPages = pageNum
                    }
                }
                val nextButton = pagination.select("a.next, a.page-numbers[href*='/page/${page + 1}/']")
                hasNext = nextButton.isNotEmpty() || page < totalPages
            }

            MoviePageResult(
                movies = movies,
                currentPage = page,
                totalPages = totalPages,
                hasNextPage = hasNext
            )
        }
    }

    suspend fun fetchDownloadLinks(movieUrl: String): Result<List<DownloadLink>> = withContext(Dispatchers.IO) {
        runCatching {
            val doc = Jsoup.connect(movieUrl)
                .userAgent(userAgent)
                .timeout(15000)
                .followRedirects(true)
                .get()

            doc.select("a[download]").mapNotNull { a ->
                val url = a.attr("href").trim()
                if (url.isEmpty()) return@mapNotNull null

                val quality = a.closest("div.row")
                    ?.selectFirst("span.text")
                    ?.text()
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }

                DownloadLink(url = url, quality = quality)
            }
        }
    }

    private fun parseArticle(article: Element): Movie? {
        val titleElement = article.select("a.stretched-link").first() ?: return null
        val link = titleElement.attr("href")
        val id = link.trimEnd('/').substringAfterLast('/')

        val title = article.select("h2.entry-title").text().ifBlank {
            titleElement.attr("title").ifBlank { id }
        }

        val farsiTitle = article.select("figure.entry-cover figcaption").text()

        val imgElement = article.select("figure.entry-cover img").first()
        val imageUrl = imgElement?.attr("src")?.takeIf { it.isNotBlank() }
            ?: imgElement?.attr("data-src")?.takeIf { it.isNotBlank() }
            ?: ""

        val isDubbed = article.select("i.icon[title*=دوبله]").isNotEmpty() ||
            article.select("use[xlink:href*=#icon-dubbled]").isNotEmpty()

        val hasSubtitle = article.select("i.icon[title*=زیرنویس]").isNotEmpty() ||
            article.select("use[xlink:href*=#icon-subtitled]").isNotEmpty()

        val genres = article.select("div.entry-ganers a").map { it.text().trim() }

        val updateInfo = article.select("p.text-muted").text().trim().ifBlank { null }

        return Movie(
            id = id,
            title = title,
            farsiTitle = farsiTitle,
            link = link,
            imageUrl = imageUrl,
            genres = genres,
            isDubbed = isDubbed,
            hasSubtitle = hasSubtitle,
            updateInfo = updateInfo
        )
    }
}
