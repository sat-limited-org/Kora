package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Contact
import com.example.model.ThemeMode
import com.example.ui.auth.AuthManager
import com.example.ui.components.CallSimulationDialog
import com.example.ui.components.KoraAvatar
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatsScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.RegistrationScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.KoraGreenPrimary
import com.example.ui.theme.KoraTheme
import com.example.ui.theme.UnreadBadge
import com.example.viewmodel.CallType
import com.example.viewmodel.KoraViewModel
import com.example.viewmodel.NavigationTab
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    private val viewModel: KoraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (uiState.userProfile.themeMode) {
                ThemeMode.SYSTEM -> isSystemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            KoraTheme(darkTheme = isDarkTheme) {
                var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
                val context = LocalContext.current
                val credentialManager = remember { CredentialManager.create(context) }
                val scope = rememberCoroutineScope()

                DisposableEffect(Unit) {
                    val listener = FirebaseAuth.AuthStateListener { auth ->
                        currentUser = auth.currentUser
                        if (auth.currentUser != null) {
                            viewModel.syncAuthenticatedUser(auth.currentUser!!)
                        } else {
                            viewModel.resetOnSignOut()
                        }
                    }
                    Firebase.auth.addAuthStateListener(listener)
                    onDispose {
                        Firebase.auth.removeAuthStateListener(listener)
                    }
                }

                LaunchedEffect(Unit) {
                    AuthManager.attemptAutoSignIn(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = {
                            currentUser = Firebase.auth.currentUser
                            currentUser?.let { viewModel.syncAuthenticatedUser(it) }
                        },
                        onUnauthenticated = { },
                        scope = scope
                    )
                }

                if (currentUser == null) {
                    // Registration & Google Sign-In with SAT Limited linking
                    RegistrationScreen(
                        onAuthSuccess = {
                            currentUser = Firebase.auth.currentUser
                            currentUser?.let { viewModel.syncAuthenticatedUser(it) }
                        }
                    )
                } else {
                    // Main App Content for Authenticated SAT Member
                    KoraApp(
                        viewModel = viewModel,
                        onSignOut = {
                            AuthManager.signOut(
                                context = context,
                                credentialManager = credentialManager,
                                onSignOutComplete = {
                                    currentUser = null
                                    viewModel.resetOnSignOut()
                                },
                                scope = scope
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun KoraApp(
    viewModel: KoraViewModel,
    onSignOut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showContactPickerForNewChat by remember { mutableStateOf(false) }

    // Active call simulation dialog
    if (uiState.activeCallContact != null) {
        CallSimulationDialog(
            contact = uiState.activeCallContact!!,
            isVideo = uiState.activeCallType == CallType.VIDEO,
            onEndCall = { viewModel.endCall() }
        )
    }

    // Modal contact selector when tapping FAB in chats
    if (showContactPickerForNewChat) {
        AlertDialog(
            onDismissRequest = { showContactPickerForNewChat = false },
            title = {
                Text(
                    text = "Select Contact",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        count = uiState.contacts.size,
                        key = { uiState.contacts[it].id }
                    ) { index ->
                        val contact = uiState.contacts[index]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showContactPickerForNewChat = false
                                    viewModel.openChatWithContact(contact)
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            KoraAvatar(contact = contact, size = 42.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = contact.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = contact.statusMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showContactPickerForNewChat = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val activeChat = uiState.activeChat

    if (activeChat != null) {
        // Individual Chat Conversation Screen
        ChatDetailScreen(
            chat = activeChat,
            messages = uiState.activeMessages,
            isContactTyping = uiState.isContactTyping,
            onBack = { viewModel.closeChat() },
            onSendMessage = { text -> viewModel.sendMessage(activeChat.id, text) },
            onStartVoiceCall = { viewModel.startCall(activeChat.contact, CallType.VOICE) },
            onStartVideoCall = { viewModel.startCall(activeChat.contact, CallType.VIDEO) }
        )
    } else {
        // Main Screen with 3-tab Bottom Navigation Bar
        BackHandler(enabled = uiState.selectedTab != NavigationTab.CHATS) {
            viewModel.selectTab(NavigationTab.CHATS)
        }

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    // Chats Tab
                    NavigationBarItem(
                        selected = uiState.selectedTab == NavigationTab.CHATS,
                        onClick = { viewModel.selectTab(NavigationTab.CHATS) },
                        icon = {
                            if (uiState.totalUnreadCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = UnreadBadge,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = uiState.totalUnreadCount.toString(),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (uiState.selectedTab == NavigationTab.CHATS) Icons.AutoMirrored.Filled.Chat else Icons.AutoMirrored.Outlined.Chat,
                                        contentDescription = "Chats"
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (uiState.selectedTab == NavigationTab.CHATS) Icons.AutoMirrored.Filled.Chat else Icons.AutoMirrored.Outlined.Chat,
                                    contentDescription = "Chats"
                                )
                            }
                        },
                        label = { Text("Chats") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KoraGreenPrimary,
                            selectedTextColor = KoraGreenPrimary,
                            indicatorColor = KoraGreenPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_chats_tab")
                    )

                    // Contacts Tab
                    NavigationBarItem(
                        selected = uiState.selectedTab == NavigationTab.CONTACTS,
                        onClick = { viewModel.selectTab(NavigationTab.CONTACTS) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.selectedTab == NavigationTab.CONTACTS) Icons.Filled.People else Icons.Outlined.People,
                                contentDescription = "Contacts"
                            )
                        },
                        label = { Text("Contacts") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KoraGreenPrimary,
                            selectedTextColor = KoraGreenPrimary,
                            indicatorColor = KoraGreenPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_contacts_tab")
                    )

                    // Settings Tab
                    NavigationBarItem(
                        selected = uiState.selectedTab == NavigationTab.SETTINGS,
                        onClick = { viewModel.selectTab(NavigationTab.SETTINGS) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.selectedTab == NavigationTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KoraGreenPrimary,
                            selectedTextColor = KoraGreenPrimary,
                            indicatorColor = KoraGreenPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_settings_tab")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = uiState.selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_animation"
                ) { tab ->
                    when (tab) {
                        NavigationTab.CHATS -> {
                            ChatsScreen(
                                chats = uiState.chats,
                                searchQuery = uiState.searchQuery,
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onChatClick = { chatId -> viewModel.openChat(chatId) },
                                onNewChatClick = { showContactPickerForNewChat = true },
                                onTogglePin = { chatId -> viewModel.togglePinned(chatId) }
                            )
                        }

                        NavigationTab.CONTACTS -> {
                            ContactsScreen(
                                contacts = uiState.contacts,
                                searchQuery = uiState.searchQuery,
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onContactClick = { contact -> viewModel.openChatWithContact(contact) },
                                onAddContact = { name, phone, status ->
                                    viewModel.addNewContact(name, phone, status)
                                }
                            )
                        }

                        NavigationTab.SETTINGS -> {
                            SettingsScreen(
                                userProfile = uiState.userProfile,
                                onUpdateProfile = { name, status, phone, username ->
                                    viewModel.updateUserProfile(name, status, phone, username)
                                },
                                onThemeSelect = { mode -> viewModel.setThemeMode(mode) },
                                onToggleNotifications = { enabled -> viewModel.toggleNotifications(enabled) },
                                onToggleReadReceipts = { enabled -> viewModel.toggleReadReceipts(enabled) },
                                onToggleEnterIsSend = { enabled -> viewModel.toggleEnterIsSend(enabled) },
                                onSignOut = onSignOut
                            )
                        }
                    }
                }
            }
        }
    }
}
