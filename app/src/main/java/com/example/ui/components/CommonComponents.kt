package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LeeGreen

// Color generator based on name hash for distinct avatars
fun getAvatarGradient(name: String): Brush {
    val hash = name.hashCode()
    val colors = listOf(
        listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)),
        listOf(Color(0xFF0D9488), Color(0xFF0F766E)),
        listOf(Color(0xFF7C3AED), Color(0xFF6D28D9)),
        listOf(Color(0xFFEA580C), Color(0xFFC2410C)),
        listOf(Color(0xFFE11D48), Color(0xFFBE123C)),
        listOf(Color(0xFF0284C7), Color(0xFF0369A1))
    )
    val pair = colors[kotlin.math.abs(hash) % colors.size]
    return Brush.linearGradient(pair)
}

@Composable
fun UserAvatar(
    name: String,
    size: Dp = 48.dp,
    isGroup: Boolean = false,
    isOnline: Boolean = false,
    showOnlineBadge: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(getAvatarGradient(name)),
            contentAlignment = Alignment.Center
        ) {
            if (isGroup) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = name,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            } else {
                val initials = name.split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .map { it.first().uppercaseChar() }
                    .joinToString("")
                if (initials.isNotEmpty()) {
                    Text(
                        text = initials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.38f).sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = name,
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.55f)
                    )
                }
            }
        }

        // Online status dot
        if (showOnlineBadge && isOnline && !isGroup) {
            Box(
                modifier = Modifier
                    .size(size * 0.3f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(LeeGreen)
            )
        }
    }
}

@Composable
fun OfflineBanner(
    isOffline: Boolean,
    onReconnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isOffline) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Offline",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Offline Mode — Messages queued locally",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                        .clickable { onReconnectClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sync",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
