package com.example.newsheadlinestv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.example.newsheadlinestv.api.RetrofitInstance
import com.example.newsheadlinestv.ui.theme.NewsHeadlinesTVTheme
import androidx.compose.foundation.lazy.items


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
    val apiService = RetrofitInstance.api

    LaunchedEffect(Unit) {
        try {
            val response = apiService.getTopHeadlines("us", "54d15f106e084f638375f08435fd444d")
            headlines = response.articles.map { it.title }
        } catch (e: Exception) {
            headlines = listOf("Error loading news")
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(headlines) { headline ->  // Pass the headlines list directly
            Text(
                text = headline,
                modifier = Modifier.padding(8.dp),
                color = Color.White // `Text` supports this directly
            )
        }
    }
}
