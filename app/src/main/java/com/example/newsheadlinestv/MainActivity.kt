package com.example.newsheadlinestv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.example.newsheadlinestv.api.RetrofitInstance
import com.example.newsheadlinestv.ui.theme.NewsHeadlinesTVTheme
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.tv.material3.CardColors
import coil.compose.rememberAsyncImagePainter




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewsHeadlinesTVTheme {
                NewsHeadlinesScreen()
            }
        }
    }
}



    @Composable
    fun NewsHeadlinesScreen() {
        var headlines by remember { mutableStateOf(listOf<String>()) }
        var images by remember { mutableStateOf(listOf<String>()) } // List of image URLs
        var isLoading by remember { mutableStateOf(false) } // Track loading state
        var refreshTrigger by remember { mutableStateOf(false) } // Trigger for refresh
        val cardColors = CardDefaults.cardColors(
            containerColor = Color.DarkGray,
            contentColor = Color.White
        )
        val apiService = RetrofitInstance.api

        // Function to fetch news (does not contain LaunchedEffect)
        suspend fun fetchNews() {
            isLoading = true // Set loading to true
            try {
                // Perform the network call directly
                val response = apiService.getTopHeadlines("us", "54d15f106e084f638375f08435fd444d")
                if (response.status == "ok") {
                    headlines = response.articles.map { it.title }
                    images = response.articles.map { it.urlToImage ?: "" }
                } else {
                    // Handle error response based on status and code
                    val errorMessage = when (response.code) {
                        "400" -> "Bad Request: ${response.message}"
                        "401" -> "Unauthorized: ${response.message}"
                        "429" -> "Too Many Requests: ${response.message}"
                        "500" -> "Server Error: ${response.message}"
                        else -> "Error: ${response.message ?: "Unknown issue"}"
                    }
                    headlines = listOf(errorMessage)
                    images = listOf("")
                }
            } catch (e: Exception) {
                headlines = listOf("Error loading news")
                images = listOf("")  // Handle error case for images
            } finally {
                isLoading = false // Set loading to false
            }
        }

        // LaunchedEffect is triggered when refreshTrigger changes
        LaunchedEffect(refreshTrigger) {
            fetchNews() // Fetch news when refreshTrigger is updated
        }

        // Initial fetch when the screen is first launched
        LaunchedEffect(Unit) {
            fetchNews()
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            refreshTrigger = !refreshTrigger // Trigger refresh on long press
                        }
                    )
                }
        ) {
            itemsIndexed(headlines) { index, headline ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = cardColors
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Image
                        val imageUrl = images.getOrElse(index) { "" }
                        if (imageUrl.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUrl),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.newsimage),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }

                        // Headline
                        Text(
                            text = headline,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Show loading indicator if data is being refreshed
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }
    }
