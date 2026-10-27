package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Chat
import com.example.model.Message
import com.example.model.MessageStatus
import com.example.ui.components.KoraAvatar
import com.example.ui.components.KoraBrandLogo
import com.example.ui.components.KoraSearchBar
import com.example.ui.theme.KoraGreenAccent
import com.example.ui.theme.KoraGreenPrimary
import com.example.ui.theme.UnreadBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ChatFilter {
    ALL,
    UNREAD,
    PINNED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    chats: List<Chat>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onChatClick: (String) -> Unit,
    onNewChatClick: () -> Unit,
    onTogglePin: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(ChatFilter.ALL) }
    var menuExpanded by remember { mutableStateOf(false) }

    val filteredChats = remember(chats, searchQuery, selectedFilter) {
        chats.filter { chat ->
            val matchesQuery = chat.contact.name.contains(searchQuery, ignoreCase = true) ||
                    chat.lastMessage.text.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                ChatFilter.ALL -> true
                ChatFilter.UNREAD -> chat.unreadCount > 0
                ChatFilter.PINNED -> chat.isPinned
            }
            matchesQuery && matchesFilter
        }.sortedWith(
            compareByDescending<Chat> { it.isPinned }
                .thenByDescending { it.lastMessage.timestamp }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Top Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        KoraBrandLogo()

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onNewChatClick) {
                                Icon(
                                    imageVector = Icons.Default.AddComment,
                                    contentDescription = "New chat",
                                    tint = KoraGreenPrimary
                                )
                            }

                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Menu",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("New group") },
                                        onClick = {
                                            menuExpanded = false
                                            onNewChatClick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Mark all as read") },
                                        onClick = { menuExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    KoraSearchBar(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        placeholder = "Search conversations..."
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = selectedFilter == ChatFilter.ALL,
                            onClick = { selectedFilter = ChatFilter.ALL },
                            label = { Text("All", fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KoraGreenPrimary.copy(alpha = 0.15f),
                                selectedLabelColor = KoraGreenPrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        FilterChip(
                            selected = selectedFilter == ChatFilter.UNREAD,
                            onClick = { selectedFilter = ChatFilter.UNREAD },
                            label = { Text("Unread", fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KoraGreenPrimary.copy(alpha = 0.15f),
                                selectedLabelColor = KoraGreenPrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        FilterChip(
                            selected = selectedFilter == ChatFilter.PINNED,
                            onClick = { selectedFilter = ChatFilter.PINNED },
                            label = { Text("Pinned", fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KoraGreenPrimary.copy(alpha = 0.15f),
                                selectedLabelColor = KoraGreenPrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            // Conversation list
            if (filteredChats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(KoraGreenPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = KoraGreenPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No chats found for \"$searchQuery\"" else "No conversations here",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the green button below to start a chat",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(
                        items = filteredChats,
                        key = { it.id }
                    ) { chat ->
                        ChatItemRow(
                            chat = chat,
                            onClick = { onChatClick(chat.id) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }

        // Floating Action Button to start a new chat
        FloatingActionButton(
            onClick = onNewChatClick,
            containerColor = KoraGreenPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("new_chat_fab")
        ) {
            Icon(
                imageVector = Icons.Default.AddComment,
                contentDescription = "New Chat",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ChatItemRow(
    chat: Chat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(chat.lastMessage.timestamp) {
        val now = System.currentTimeMillis()
        val diff = now - chat.lastMessage.timestamp
        when {
            diff < 24 * 60 * 60 * 1000L -> timeFormatter.format(Date(chat.lastMessage.timestamp))
            diff < 48 * 60 * 60 * 1000L -> "Yesterday"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(chat.lastMessage.timestamp))
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("chat_item_${chat.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KoraAvatar(contact = chat.contact, size = 52.dp)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.contact.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (chat.unreadCount > 0) KoraGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (chat.lastMessage.isFromMe) {
                        val icon = if (chat.lastMessage.status == MessageStatus.READ) {
                            Icons.Default.DoneAll
                        } else Icons.Default.Check
                        val tint = if (chat.lastMessage.status == MessageStatus.READ) {
                            KoraGreenAccent
                        } else MaterialTheme.colorScheme.onSurfaceVariant

                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier
                                .size(15.dp)
                                .padding(end = 3.dp)
                        )
                    }

                    Text(
                        text = chat.lastMessage.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = if (chat.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (chat.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (chat.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    if (chat.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(UnreadBadge)
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chat.unreadCount.toString(),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
