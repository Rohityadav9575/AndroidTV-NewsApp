package com.example.newsheadlinestv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.material3.MaterialTheme
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

    val apiService = RetrofitInstance.api

    LaunchedEffect(Unit) {
        try {
            val response = apiService.getTopHeadlines("us", "54d15f106e084f638375f08435fd444d")
            headlines = response.articles.map { it.title }
            images = response.articles.map { it.urlToImage ?: "" }  // Assuming the API returns image URLs
        } catch (e: Exception) {
            headlines = listOf("Error loading news")
            images = listOf("")  // Handle error case for images
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp) // Adds margin on both sides of the TV screen
    ) {
        itemsIndexed(headlines) { index, headline ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),  // Add vertical padding between items
                    elevation =CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Image
                    val imageUrl = images.getOrElse(index) { "" }
                    if (imageUrl.isNotEmpty()) {
                        // Load image using Coil
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp) // Adjust the height as needed
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
    }
}
