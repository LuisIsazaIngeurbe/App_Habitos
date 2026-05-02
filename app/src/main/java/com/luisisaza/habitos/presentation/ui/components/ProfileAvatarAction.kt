package com.luisisaza.habitos.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.luisisaza.habitos.HabitosApp
import com.luisisaza.habitos.R
import java.io.File

/**
 * Circular profile avatar action used in TopAppBar.
 * Shows the logged user's photo, falling back to a default icon.
 */
@Composable
fun ProfileAvatarAction(onClick: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as HabitosApp
    val loggedUserId by app.sessionManager.loggedUserId.collectAsState(initial = null)

    var photoPath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(loggedUserId) {
        photoPath = loggedUserId?.let { app.userRepository.getUserById(it)?.photoPath }
    }

    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val photo = photoPath
        if (!photo.isNullOrBlank()) {
            AsyncImage(
                model = File(photo),
                contentDescription = stringResource(R.string.cd_profile_photo),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = stringResource(R.string.nav_profile),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
