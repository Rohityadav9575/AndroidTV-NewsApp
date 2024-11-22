package com.example.newsheadlinestv

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
            containerColor = Color.Black,
            contentColor = Color.White
        )
        var focusedIndex by remember { mutableIntStateOf(-1) }
        // Initialize focusRequesters list after data is fetched
        var focusRequesters by remember { mutableStateOf<List<FocusRequester>>(emptyList()) }

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
        // Request focus for the first item when data is loaded
        LaunchedEffect(Unit) {
            if (focusRequesters.isNotEmpty()) {
                focusRequesters[0].requestFocus()
            }
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

                val isFirstItem = index == 0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusable()
                        .padding(vertical = 8.dp)

                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                focusedIndex = index
                                Log.d("FocusIndex","focus card index: $focusedIndex")
                            }else{
                                Log.d("FocusIndex","focus card is not focused")
                            }
                        }

                        .border(
                            width = if (focusedIndex == index) 4.dp else 0.dp, // Highlight focused card
                            color = if (focusedIndex == index) Color.Yellow else Color.Transparent,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ),
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
