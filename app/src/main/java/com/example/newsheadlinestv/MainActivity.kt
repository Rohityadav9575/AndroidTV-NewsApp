package com.example.newsheadlinestv

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import coil.compose.rememberAsyncImagePainter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setting up the UI theme and content
        setContent {
            NewsHeadlinesTVTheme {
                NewsHeadlinesScreen() // Display the main screen with news headlines
            }
        }
    }
}

@Composable
fun NewsHeadlinesScreen() {
    // State variables to hold data and control UI behavior
    var headlines by remember { mutableStateOf(listOf<String>()) } // List of news headlines
    var images by remember { mutableStateOf(listOf<String>()) } // List of image URLs
    var isLoading by remember { mutableStateOf(false) } // Track if data is being loaded
    var refreshTrigger by remember { mutableStateOf(false) } // Trigger to refresh data

    // Card UI styling options
    val cardColors = CardDefaults.cardColors(
        containerColor = Color.Black, // Card background color
        contentColor = Color.White    // Card text color
    )

    // Retrofit API service instance
    val apiService = RetrofitInstance.api

    // Function to fetch news data from the API
    suspend fun fetchNews() {
        isLoading = true // Start loading indicator
        try {
            // Call the API to fetch top headlines
            val response = apiService.getTopHeadlines("us", "e60dc1841ef4487aae78db246d05706c")
            if (response.status == "ok") {
                // Map the API response to lists of headlines and images
                headlines = response.articles.map { it.title }
                images = response.articles.map { it.urlToImage ?: "" }
            } else {
                // Handle cases where the API call fails
                val errorMessage = when (response.code) {
                    "400" -> "Bad Request: ${response.message}"
                    "401" -> "Unauthorized: ${response.message}"
                    "429" -> "Too Many Requests: ${response.message}"
                    "500" -> "Server Error: ${response.message}"
                    else -> "Error: ${response.message ?: "Unknown issue"}"
                }
                headlines = listOf(errorMessage) // Show the error message as a headline
                images = listOf("") // No images for errors
            }
        } catch (e: Exception) {
            // Handle exceptions like network errors
            Log.e("NewsHeadlinesScreen", "Error fetching news", e)
            headlines = listOf("Error loading news") // Generic error message
            images = listOf("") // No images for errors
        } finally {
            isLoading = false // Stop loading indicator
        }
    }

    // Fetch data when the screen first loads or when the refresh trigger changes
    LaunchedEffect(refreshTrigger) {
        fetchNews()
    }

    // UI layout for displaying news headlines in a scrollable list
    LazyColumn(
        modifier = Modifier
            .fillMaxSize() // Fill the entire screen
            .padding(horizontal = 16.dp) // Add padding to the left and right
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        refreshTrigger = !refreshTrigger // Refresh data on long press
                    }
                )
            }
    ) {
        // Create a card for each headline and display it with an image
        itemsIndexed(headlines) { index, headline ->
            Card(
                modifier = Modifier
                    .fillMaxWidth() // Make the card span the full width
                    .padding(vertical = 8.dp) // Add spacing between cards
                    .border(
                        width = 0.dp, // No border by default
                        color = Color.Transparent, // Transparent border
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ),
                elevation = CardDefaults.cardElevation(4.dp), // Add shadow to cards
                colors = cardColors // Apply the defined card colors
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Display the news image
                    val imageUrl = images.getOrElse(index) { "" }
                    if (imageUrl.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl), // Load image from URL
                            contentDescription = null, // No description for accessibility
                            modifier = Modifier
                                .fillMaxWidth() // Make the image span the full card width
                                .height(200.dp) // Set a fixed height for the image
                        )
                    } else {
                        // Display a default placeholder image if no URL is available
                        Image(
                            painter = painterResource(id = R.drawable.newsimage),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }

                    // Display the headline text below the image
                    Text(
                        text = headline, // The headline content
                        style = MaterialTheme.typography.bodyLarge, // Use a predefined text style
                        color = Color.White, // Set the text color to white
                        modifier = Modifier.padding(top = 8.dp) // Add space above the text
                    )
                }
            }
        }

        // Display a loading spinner at the center of the screen while data is being fetched
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize() // Fill the entire screen space
                        .wrapContentSize(Alignment.Center) // Center the spinner
                ) {
                    CircularProgressIndicator(color = Color.White) // Show a white spinner

                }
            }
        }
    }
}