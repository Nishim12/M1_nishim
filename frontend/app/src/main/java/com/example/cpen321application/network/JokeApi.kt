package com.example.cpen321application.network

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

data class Joke(val setup: String, val punchline: String)

private const val JOKE_API_URL = "https://official-joke-api.appspot.com/random_joke"

// Shown when the joke API is unreachable so the timer's surprise never fails.
private val FALLBACK_JOKES = listOf(
    Joke("Why do programmers prefer dark mode?", "Because light attracts bugs."),
    Joke("Why did the timer go to therapy?", "It had too many unresolved issues."),
    Joke("What's a computer's favourite snack?", "Microchips."),
    Joke("Why was the JavaScript developer sad?", "Because they didn't Node how to Express themselves.")
)

fun randomFallbackJoke(): Joke = FALLBACK_JOKES.random()

suspend fun fetchRandomJoke(): Joke = withContext(Dispatchers.IO) {
    val connection = (URL(JOKE_API_URL).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 5_000
        readTimeout = 5_000
    }
    try {
        val code = connection.responseCode
        if (code != HttpURLConnection.HTTP_OK) {
            throw IOException("Joke request failed with HTTP $code")
        }
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        try {
            val json = JSONObject(body)
            Joke(setup = json.getString("setup"), punchline = json.getString("punchline"))
        } catch (e: JSONException) {
            throw IOException("Unexpected joke response", e)
        }
    } finally {
        connection.disconnect()
    }
}
