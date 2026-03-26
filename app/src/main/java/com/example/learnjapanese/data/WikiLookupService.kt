package com.example.learnjapanese.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class WikiLookupResult(
    val title: String,
    val extract: String,
    val sourceUrl: String
)

object WikiLookupService {
    private const val WIKTIONARY_API = "https://en.wiktionary.org/w/api.php"
    private const val USER_AGENT = "LearnJapaneseApp/1.0"

    suspend fun lookup(query: String): Result<WikiLookupResult> = withContext(Dispatchers.IO) {
        runCatching {
            val normalizedQuery = query.trim()
            require(normalizedQuery.isNotEmpty()) { "Enter a word or phrase first." }

            val pageTitle = resolveBestTitle(normalizedQuery)
            val extract = fetchExtract(pageTitle)

            WikiLookupResult(
                title = pageTitle,
                extract = extract.ifBlank { "No short extract was returned for this entry." },
                sourceUrl = "https://en.wiktionary.org/wiki/${encode(pageTitle)}"
            )
        }
    }

    private fun resolveBestTitle(query: String): String {
        val url = buildString {
            append(WIKTIONARY_API)
            append("?action=opensearch")
            append("&search=${encode(query)}")
            append("&limit=1")
            append("&namespace=0")
            append("&format=json")
        }

        val responseText = fetchText(url)
        val response = JSONArray(responseText)
        val titles = response.getJSONArray(1)
        return if (titles.length() > 0) {
            titles.getString(0)
        } else {
            query
        }
    }

    private fun fetchExtract(title: String): String {
        val url = buildString {
            append(WIKTIONARY_API)
            append("?action=query")
            append("&prop=extracts")
            append("&explaintext=1")
            append("&exintro=1")
            append("&redirects=1")
            append("&titles=${encode(title)}")
            append("&format=json")
        }

        val responseText = fetchText(url)
        val response = JSONObject(responseText)
        val pages = response.getJSONObject("query").getJSONObject("pages")
        val keys = pages.keys()
        val pageKey = if (keys.hasNext()) {
            keys.next()
        } else {
            error("No Wiktionary page was returned.")
        }
        val page = pages.getJSONObject(pageKey)
        return page.optString("extract")
    }

    private fun fetchText(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val stream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: error("Wiki request failed with HTTP ${connection.responseCode}")
            }

            stream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(value, Charsets.UTF_8.name())
    }
}
