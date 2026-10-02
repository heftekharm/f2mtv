package com.hfm.f2mtv.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hfm.f2mtv.ui.main.components.MovieCard
import com.hfm.f2mtv.ui.main.components.MovieDetailDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()

    // Trigger load more when scrolling near end
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
            lastVisibleItem.index >= (gridState.layoutInfo.totalItemsCount - 6)
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && uiState.hasNextPage && !uiState.isLoadingMore) {
            viewModel.loadNextPage()
        }
    }

    // Force RTL for Farsi UI
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212)
        ) {
            Scaffold(
                containerColor = Color(0xFF121212),
                topBar = {
                    TopHeaderBar(
                        uiState = uiState,
                        onRefresh = { viewModel.refresh() },
                        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                        onGenreSelected = { viewModel.filterByGenre(it) }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when {
                        uiState.isLoading -> {
                            LoadingView()
                        }

                        uiState.error != null && uiState.movies.isEmpty() -> {
                            ErrorView(
                                message = uiState.error!!,
                                onRetry = { viewModel.loadMovies(page = 1) }
                            )
                        }

                        uiState.filteredMovies.isEmpty() -> {
                            EmptyView(
                                searchQuery = uiState.searchQuery,
                                selectedGenre = uiState.selectedGenreFilter
                            )
                        }

                        else -> {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 150.dp),
                                    state = gridState,
                                    contentPadding = PaddingValues(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(
                                        items = uiState.filteredMovies,
                                        key = { it.id }
                                    ) { movie ->
                                        MovieCard(
                                            movie = movie,
                                            onClick = { viewModel.selectMovie(movie) }
                                        )
                                    }

                                    if (uiState.isLoadingMore) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    color = Color(0xFF96F207),
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Dialog
                    uiState.selectedMovie?.let { movie ->
                        MovieDetailDialog(
                            movie = movie,
                            onDismiss = { viewModel.selectMovie(null) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopHeaderBar(
    uiState: MainUiState,
    onRefresh: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onGenreSelected: (String) -> Unit
) {
    var isSearchExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E))
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF96F207)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "F2M",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "فیلم 2 مدیا",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "صفحه ${uiState.currentPage} از ${uiState.totalPages}",
                        color = Color(0xFF888888),
                        fontSize = 11.sp
                    )
                }
            }

            // Actions (Search & Refresh)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { isSearchExpanded = !isSearchExpanded },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSearchExpanded) Color(0xFF96F207) else Color(0xFF2A2A2A),
                        contentColor = if (isSearchExpanded) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isSearchExpanded) "بستن جستجو" else "جستجو",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onRefresh,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2A2A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "بروزرسانی",
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Search Bar Input Field
        AnimatedVisibility(visible = isSearchExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = { Text("جستجوی نام فیلم...", color = Color.Gray, fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF282828),
                        unfocusedContainerColor = Color(0xFF282828),
                        focusedBorderColor = Color(0xFF96F207),
                        unfocusedBorderColor = Color(0xFF444444),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )
            }
        }

        // Genre Filter Chips Row
        if (uiState.availableGenres.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedGenreFilter == null,
                        onClick = { onGenreSelected("") },
                        label = { Text("همه") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF96F207),
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF282828),
                            labelColor = Color.White
                        )
                    )
                }

                items(uiState.availableGenres) { genre ->
                    val isSelected = uiState.selectedGenreFilter == genre
                    FilterChip(
                        selected = isSelected,
                        onClick = { onGenreSelected(genre) },
                        label = { Text(genre) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF96F207),
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF282828),
                            labelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFF96F207),
                strokeWidth = 3.dp
            )
            Text(
                text = "در حال دریافت لیست فیلم‌ها...",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "خطا در دریافت اطلاعات",
                color = Color(0xFFFF5252),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                color = Color.LightGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF96F207),
                    contentColor = Color.Black
                )
            ) {
                Text("تلاش مجدد", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyView(
    searchQuery: String,
    selectedGenre: String?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "فیلمی یافت نشد!",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            val hint = when {
                searchQuery.isNotBlank() -> "نتیجه‌ای برای \"$searchQuery\" پیدا نشد."
                selectedGenre != null -> "فیلمی در ژانر \"$selectedGenre\" وجود ندارد."
                else -> "لیست خالی است."
            }
            Text(
                text = hint,
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
