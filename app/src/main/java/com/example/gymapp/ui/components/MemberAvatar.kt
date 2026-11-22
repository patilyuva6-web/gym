package com.example.gymapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

@Composable
fun MemberAvatar(
    name: String,
    photoPath: String?,
    size: Dp = 52.dp,
    modifier: Modifier = Modifier
) {
    val photoFile = remember(photoPath) {
        photoPath?.takeIf { it.isNotBlank() }?.let { File(it) }?.takeIf { it.exists() }
    }

    val initials = remember(name) {
        name.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (photoFile != null) {
            AsyncImage(
                model = photoFile,
                contentDescription = if (name.isNotBlank()) "$name photo" else "Member photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (initials.isNotEmpty()) {
            Text(
                text = initials,
                style = if (size >= 80.dp) {
                    MaterialTheme.typography.headlineMedium
                } else {
                    MaterialTheme.typography.titleMedium
                },
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Default member avatar",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}
