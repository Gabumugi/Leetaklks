package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallRecord
import com.example.ui.components.UserAvatar
import com.example.ui.theme.LeeBlue
import com.example.ui.theme.LeeGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen(
    calls: List<CallRecord>,
    onStartCall: (contactName: String, callType: String) -> Unit,
    onClearCalls: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Calls",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear call history") },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onClearCalls()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (calls.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recent calls",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(calls, key = { it.id }) { call ->
                    CallHistoryItem(
                        call = call,
                        onCallAgain = { onStartCall(call.contactName, call.callType) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 76.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CallHistoryItem(
    call: CallRecord,
    onCallAgain: () -> Unit
) {
    val dateString = remember(call.timestamp) {
        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(call.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCallAgain() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(name = call.contactName, size = 48.dp)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.contactName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (call.direction) {
                    "INCOMING" -> Icon(
                        imageVector = Icons.AutoMirrored.Filled.CallReceived,
                        contentDescription = "Incoming",
                        tint = LeeGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    "OUTGOING" -> Icon(
                        imageVector = Icons.AutoMirrored.Filled.CallMade,
                        contentDescription = "Outgoing",
                        tint = LeeBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    else -> Icon(
                        imageVector = Icons.AutoMirrored.Filled.CallMissed,
                        contentDescription = "Missed",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                val desc = if (call.durationSeconds > 0) {
                    val m = call.durationSeconds / 60
                    val s = call.durationSeconds % 60
                    "$dateString (${m}m ${s}s)"
                } else {
                    "$dateString (Missed)"
                }
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(onClick = onCallAgain) {
            Icon(
                imageVector = if (call.callType == "VIDEO") Icons.Default.Videocam else Icons.Default.Call,
                contentDescription = "Call",
                tint = LeeBlue
            )
        }
    }
}

@Composable
fun ActiveCallScreen(
    contactName: String,
    callType: String,
    durationSeconds: Int,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    isVideoDisabled: Boolean,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleVideo: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onEndCall() }

    Surface(
        color = Color(0xFF0F172A),
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text(
                    text = "LeeTalk WebRTC Call",
                    style = MaterialTheme.typography.labelMedium,
                    color = LeeBlue
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = contactName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                val min = durationSeconds / 60
                val sec = durationSeconds % 60
                Text(
                    text = String.format("%02d:%02d", min, sec),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "● Connected • End-to-End Media Stream",
                    style = MaterialTheme.typography.labelSmall,
                    color = LeeGreen
                )
            }

            // Center Call Avatar / Video View
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                if (callType == "VIDEO" && !isVideoDisabled) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = LeeBlue,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Live Video",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                } else {
                    UserAvatar(name = contactName, size = 160.dp, showOnlineBadge = false)
                }
            }

            // Bottom Controls Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute button
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) Color.White else Color(0xFF334155))
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = if (isMuted) Color.Black else Color.White
                        )
                    }

                    // Video toggle
                    if (callType == "VIDEO") {
                        IconButton(
                            onClick = onToggleVideo,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isVideoDisabled) Color.White else Color(0xFF334155))
                        ) {
                            Icon(
                                imageVector = if (isVideoDisabled) Icons.Default.VideocamOff else Icons.Default.Videocam,
                                contentDescription = "Video",
                                tint = if (isVideoDisabled) Color.Black else Color.White
                            )
                        }
                    }

                    // Speaker toggle
                    IconButton(
                        onClick = onToggleSpeaker,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (isSpeakerOn) LeeBlue else Color(0xFF334155))
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speaker",
                            tint = Color.White
                        )
                    }

                    // Hang up button
                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    }
}
