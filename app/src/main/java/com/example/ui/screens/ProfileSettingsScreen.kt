package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceSession
import com.example.data.model.User
import com.example.ui.components.UserAvatar
import com.example.ui.theme.LeeBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    user: User?,
    deviceSessions: List<DeviceSession>,
    isOffline: Boolean,
    readReceipts: Boolean,
    typingIndicators: Boolean,
    notifications: Boolean,
    onToggleOffline: () -> Unit,
    onToggleReadReceipts: () -> Unit,
    onToggleTypingIndicators: () -> Unit,
    onToggleNotifications: () -> Unit,
    onRevokeSession: (String) -> Unit,
    onUpdateProfile: (name: String, username: String, bio: String, status: String) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSessionsDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Me & Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        name = user?.displayName ?: "Gabriel Mugi",
                        size = 64.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.displayName ?: "Gabriel Mugi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user?.username ?: "@gabriel",
                            style = MaterialTheme.typography.bodySmall,
                            color = LeeBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = user?.email ?: "gabriel.mugi66@gmail.com",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user?.bio ?: "Talk. Share. Connect.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showEditProfileDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = LeeBlue
                        )
                    }
                }
            }

            // Connectivity / PWA Offline Simulator Section
            SettingsSectionHeader(title = "Network & Offline Sync (PWA)")
            SettingsToggleItem(
                icon = Icons.Default.WifiOff,
                title = "Offline Mode Simulation",
                subtitle = if (isOffline) "Disconnected • Messages enter local queue" else "Connected • Instant WebSocket sync",
                checked = isOffline,
                onCheckedChange = { onToggleOffline() }
            )

            // Privacy Section
            SettingsSectionHeader(title = "Privacy & Messaging")
            SettingsToggleItem(
                icon = Icons.Default.Security,
                title = "Read Receipts",
                subtitle = "Let contacts see when you have read their messages (✓✓)",
                checked = readReceipts,
                onCheckedChange = { onToggleReadReceipts() }
            )
            SettingsToggleItem(
                icon = Icons.Default.Info,
                title = "Typing Indicators",
                subtitle = "Display when you are actively composing a message",
                checked = typingIndicators,
                onCheckedChange = { onToggleTypingIndicators() }
            )
            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "Push Notifications",
                subtitle = "Notify for incoming calls and messages",
                checked = notifications,
                onCheckedChange = { onToggleNotifications() }
            )

            // Storage & Sessions
            SettingsSectionHeader(title = "Storage & Devices")
            SettingsNavigationItem(
                icon = Icons.Default.Storage,
                title = "Storage Management",
                subtitle = "Manage media, cache & offline downloads (1.85 GB)",
                onClick = { showStorageDialog = true }
            )
            SettingsNavigationItem(
                icon = Icons.Default.Devices,
                title = "Active Sessions",
                subtitle = "${deviceSessions.size} devices authenticated via passwordless link",
                onClick = { showSessionsDialog = true }
            )
            SettingsNavigationItem(
                icon = Icons.Default.Lock,
                title = "Security & Encryption",
                subtitle = "Zero-knowledge auth tokens, WebRTC media encryption",
                onClick = { showSecurityDialog = true }
            )

            // Danger Zone
            SettingsSectionHeader(title = "Account")
            SettingsNavigationItem(
                icon = Icons.Default.Logout,
                title = "Sign Out",
                subtitle = "Revoke current device token and exit",
                onClick = onLogout
            )
            SettingsNavigationItem(
                icon = Icons.Default.DeleteForever,
                title = "Delete Account",
                subtitle = "Permanently remove your account and all data",
                titleColor = MaterialTheme.colorScheme.error,
                onClick = { showDeleteConfirmDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Version info footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LeeTalk for Android v1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Talk. Share. Connect.",
                    style = MaterialTheme.typography.labelSmall,
                    color = LeeBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(user?.displayName ?: "") }
        var editUsername by remember { mutableStateOf(user?.username?.replace("@", "") ?: "") }
        var editBio by remember { mutableStateOf(user?.bio ?: "") }
        var editStatus by remember { mutableStateOf(user?.status ?: "Available") }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Username") },
                        prefix = { Text("@") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio / About") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editStatus,
                        onValueChange = { editStatus = it },
                        label = { Text("Status") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(editName, editUsername, editBio, editStatus)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LeeBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Active Sessions Dialog
    if (showSessionsDialog) {
        AlertDialog(
            onDismissRequest = { showSessionsDialog = false },
            title = { Text("Active Sessions", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Authenticated devices with active passwordless sessions:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    deviceSessions.forEach { session ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (session.deviceName.contains("Mobile") || session.deviceName.contains("Android")) Icons.Default.PhoneAndroid else Icons.Default.Computer,
                                contentDescription = null,
                                tint = if (session.isCurrent) LeeBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = session.deviceName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${session.browser} • ${session.lastActive}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (session.isCurrent) {
                                Text(
                                    text = "Current",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LeeBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                TextButton(onClick = { onRevokeSession(session.id) }) {
                                    Text("Revoke", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                }
                            }
                        }
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSessionsDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Storage Management Dialog
    if (showStorageDialog) {
        var cacheCleared by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showStorageDialog = false },
            title = { Text("Storage Usage", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Media & Cache Breakdown", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    StorageRow(label = "Images & Photos", size = "245 MB")
                    StorageRow(label = "Videos", size = "1.2 GB")
                    StorageRow(label = "Documents & PDFs", size = "320 MB")
                    StorageRow(label = "Audio & Voice Notes", size = "86 MB")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    StorageRow(label = "Total Storage", size = if (cacheCleared) "1.60 GB" else "1.85 GB", isBold = true)

                    if (cacheCleared) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Local cache cleared successfully! Cloud history remains safe.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { cacheCleared = true },
                    colors = ButtonDefaults.buttonColors(containerColor = LeeBlue)
                ) {
                    Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear Local Cache")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStorageDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Security Info Dialog
    if (showSecurityDialog) {
        AlertDialog(
            onDismissRequest = { showSecurityDialog = false },
            title = { Text("Security Architecture", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "LeeTalk Production Security Model:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Passwordless authentication using cryptographic magic tokens\n" +
                                "• Server-side token verification with automatic rotation\n" +
                                "• Multi-session device isolation with instant remote revocation\n" +
                                "• WebRTC peer-to-peer DTLS/SRTP encryption for audio & video calls\n" +
                                "• SQLite/Room local persistence with idempotent sync queue",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSecurityDialog = false }) {
                    Text("Understood")
                }
            }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Account", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
            text = {
                Text("This action will permanently delete your LeeTalk account, messages, and contacts. This cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StorageRow(label: String, size: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(text = size, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = LeeBlue,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = LeeBlue)
        )
    }
}

@Composable
private fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (titleColor != Color.Unspecified) titleColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (titleColor != Color.Unspecified) titleColor else Color.Unspecified
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}
