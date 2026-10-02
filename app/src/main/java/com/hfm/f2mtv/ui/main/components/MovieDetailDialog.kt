package com.hfm.f2mtv.ui.main.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hfm.f2mtv.data.model.Movie

@Composable
fun MovieDetailDialog(
    movie: Movie,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 620.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    val isCompact = maxWidth < 480.dp || maxHeight < 360.dp

                    if (isCompact) {
                        // Scrollable Column for small / narrow screens
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PosterBox(
                                movie = movie,
                                context = context,
                                modifier = Modifier.width(160.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            MovieInfoContent(
                                movie = movie,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                textAlignment = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            ActionButtonsRow(
                                onDismiss = onDismiss,
                                onDownloadClick = {
                                    if (movie.link.isNotBlank()) {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(movie.link))
                                        context.startActivity(intent)
                                    }
                                }
                            )
                        }
                    } else {
                        // Side-by-side Row for wider screens / TV / Landscape
                        // In RTL, 1st item (Poster) is on the RIGHT, 2nd item (Column) is on the LEFT
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Poster (Right side in RTL)
                            PosterBox(
                                movie = movie,
                                context = context,
                                modifier = Modifier.width(160.dp)
                            )

                            // Details & Actions (Left side in RTL)
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.Start
                            ) {
                                MovieInfoContent(
                                    movie = movie,
                                    horizontalAlignment = Alignment.Start,
                                    textAlignment = TextAlign.Start
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                ActionButtonsRow(
                                    onDismiss = onDismiss,
                                    onDownloadClick = {
                                        if (movie.link.isNotBlank()) {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(movie.link))
                                            context.startActivity(intent)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PosterBox(
    movie: Movie,
    context: Context,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(movie.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MovieInfoContent(
    movie: Movie,
    horizontalAlignment: Alignment.Horizontal,
    textAlignment: TextAlign
) {
    Column(
        horizontalAlignment = horizontalAlignment
    ) {
        // Title
        Text(
            text = movie.title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = textAlignment
        )

        if (movie.farsiTitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = movie.farsiTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                textAlign = textAlignment
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Badges Row
        if (movie.isDubbed || movie.hasSubtitle) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (movie.isDubbed) {
                    DetailBadge(
                        text = "دوبله فارسی",
                        color = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
                if (movie.hasSubtitle) {
                    DetailBadge(
                        text = "زیرنویس چسبیده",
                        color = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }

        if (!movie.updateInfo.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = movie.updateInfo,
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = textAlignment
            )
        }

        if (movie.genres.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ژانرها: ${movie.genres.joinToString("، ")}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = textAlignment
            )
        }
    }
}

@Composable
private fun ActionButtonsRow(
    onDismiss: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text("بستن")
        }

        Button(
            onClick = onDownloadClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("مشاهده و دانلود", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DetailBadge(
    text: String,
    color: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}