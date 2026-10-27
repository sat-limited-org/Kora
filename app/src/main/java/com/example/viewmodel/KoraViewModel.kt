package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleData
import com.example.data.repository.UserRepository
import com.example.model.Chat
import com.example.model.Contact
import com.example.model.Message
import com.example.model.MessageStatus
import com.example.model.ThemeMode
import com.example.model.UserProfile
import com.example.model.UserProfileDocument
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class NavigationTab(val label: String) {
    CHATS("Chats"),
    CONTACTS("Contacts"),
    SETTINGS("Settings")
}

enum class CallType {
    VOICE,
    VIDEO
}

data class KoraUiState(
    val chats: List<Chat> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val messagesByChatId: Map<String, List<Message>> = emptyMap(),
    val activeChatId: String? = null,
    val selectedTab: NavigationTab = NavigationTab.CHATS,
    val searchQuery: String = "",
    val userProfile: UserProfile = UserProfile(),
    val isContactTyping: Boolean = false,
    val activeCallContact: Contact? = null,
    val activeCallType: CallType? = null,
    val currentUserId: String? = null
) {
    val activeChat: Chat?
        get() = chats.find { it.id == activeChatId }

    val activeMessages: List<Message>
        get() = activeChatId?.let { messagesByChatId[it] } ?: emptyList()

    val totalUnreadCount: Int
        get() = chats.sumOf { it.unreadCount }
}

class KoraViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository(application.applicationContext)
    private var profileObserveJob: Job? = null

    private val _uiState = MutableStateFlow(
        KoraUiState(
            chats = SampleData.getInitialChats(),
            contacts = SampleData.initialContacts,
            messagesByChatId = SampleData.initialMessages,
            userProfile = UserProfile()
        )
    )
    val uiState: StateFlow<KoraUiState> = _uiState.asStateFlow()

    fun syncAuthenticatedUser(user: FirebaseUser) {
        val uid = user.uid
        _uiState.update { it.copy(currentUserId = uid) }

        profileObserveJob?.cancel()
        profileObserveJob = viewModelScope.launch {
            // First ensure profile exists or create it under SAT Limited
            val existing = userRepository.getUserProfile(uid).getOrNull()
            if (existing == null) {
                val displayName = user.displayName?.ifBlank { "SAT Team Member" } ?: "SAT Team Member"
                val email = user.email ?: "user@satlimited.com"
                val username = "@${displayName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }}"
                val initialDoc = UserProfileDocument(
                    userId = uid,
                    name = displayName,
                    email = email,
                    organization = "SAT Limited",
                    username = username,
                    status = "Connected with SAT Limited on Kora 🌿",
                    phone = user.phoneNumber ?: "+263 77 123 4567"
                )
                userRepository.saveUserProfile(initialDoc)
            }

            // Real-time observation of Firestore profile
            userRepository.observeUserProfile(uid).collect { doc ->
                if (doc != null) {
                    _uiState.update { state ->
                        state.copy(
                            userProfile = state.userProfile.copy(
                                name = doc.name,
                                email = doc.email,
                                organization = doc.organization,
                                username = doc.username,
                                status = doc.status,
                                phone = doc.phone
                            )
                        )
                    }
                }
            }
        }
    }

    fun resetOnSignOut() {
        profileObserveJob?.cancel()
        _uiState.update {
            it.copy(
                currentUserId = null,
                selectedTab = NavigationTab.CHATS,
                activeChatId = null
            )
        }
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.update { it.copy(selectedTab = tab, searchQuery = "") }
    }

    fun openChat(chatId: String) {
        _uiState.update { state ->
            val updatedChats = state.chats.map {
                if (it.id == chatId) it.copy(unreadCount = 0) else it
            }
            state.copy(
                activeChatId = chatId,
                chats = updatedChats,
                isContactTyping = false
            )
        }
    }

    fun openChatWithContact(contact: Contact) {
        val existingChat = _uiState.value.chats.find { it.contact.id == contact.id }
        if (existingChat != null) {
            openChat(existingChat.id)
        } else {
            val initialMsg = Message(
                id = UUID.randomUUID().toString(),
                chatId = contact.id,
                senderId = contact.id,
                text = "Hey! Let's connect on Kora 🌿",
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.READ
            )
            val newChat = Chat(
                id = contact.id,
                contact = contact,
                lastMessage = initialMsg,
                unreadCount = 0,
                isPinned = false
            )
            _uiState.update { state ->
                state.copy(
                    chats = listOf(newChat) + state.chats,
                    messagesByChatId = state.messagesByChatId + (contact.id to listOf(initialMsg)),
                    activeChatId = contact.id,
                    isContactTyping = false
                )
            }
        }
    }

    fun closeChat() {
        _uiState.update { it.copy(activeChatId = null, isContactTyping = false) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun sendMessage(chatId: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val messageId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val newMessage = Message(
            id = messageId,
            chatId = chatId,
            senderId = "me",
            text = trimmed,
            timestamp = now,
            status = MessageStatus.SENT
        )

        _uiState.update { state ->
            val chatMessages = (state.messagesByChatId[chatId] ?: emptyList()) + newMessage
            val updatedMessages = state.messagesByChatId + (chatId to chatMessages)

            val updatedChats = state.chats.map { chat ->
                if (chat.id == chatId) {
                    chat.copy(lastMessage = newMessage)
                } else chat
            }

            state.copy(
                messagesByChatId = updatedMessages,
                chats = updatedChats
            )
        }

        viewModelScope.launch {
            delay(400)
            _uiState.update { state ->
                val list = state.messagesByChatId[chatId]?.map {
                    if (it.id == messageId) it.copy(status = MessageStatus.DELIVERED) else it
                } ?: emptyList()
                state.copy(messagesByChatId = state.messagesByChatId + (chatId to list))
            }

            delay(600)
            _uiState.update { it.copy(isContactTyping = true) }

            delay(1200)
            val replyText = generateContextualReply(trimmed)
            val replyMessage = Message(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                senderId = chatId,
                text = replyText,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.READ
            )

            _uiState.update { state ->
                val currentList = (state.messagesByChatId[chatId] ?: emptyList())
                val updatedWithRead = currentList.map {
                    if (it.id == messageId) it.copy(status = MessageStatus.READ) else it
                } + replyMessage

                val updatedChats = state.chats.map { chat ->
                    if (chat.id == chatId) {
                        chat.copy(
                            lastMessage = replyMessage,
                            unreadCount = if (state.activeChatId == chatId) 0 else chat.unreadCount + 1
                        )
                    } else chat
                }

                state.copy(
                    isContactTyping = false,
                    messagesByChatId = state.messagesByChatId + (chatId to updatedWithRead),
                    chats = updatedChats
                )
            }
        }
    }

    private fun generateContextualReply(userMessage: String): String {
        val lower = userMessage.lowercase()
        return when {
            "hello" in lower || "hi" in lower || "hey" in lower ->
                "Hey there! Great to hear from you. How are you liking Kora so far? 🌿"
            "sat" in lower || "organization" in lower || "limited" in lower ->
                "Welcome to the SAT Limited team channel! Ready for today's updates 🚀"
            "how are you" in lower ->
                "Doing great, thanks! Just testing out the smooth Compose interface 🚀"
            "coffee" in lower || "lunch" in lower || "dinner" in lower ->
                "Count me in! What time works best for you? ☕"
            "call" in lower || "zoom" in lower ->
                "Sure thing, feel free to tap the call button at the top whenever you're ready!"
            "theme" in lower || "dark" in lower || "green" in lower ->
                "The green and dark aesthetic is so easy on the eyes, really love it."
            "test" in lower || "prototype" in lower ->
                "Firebase Auth & Firestore are hooked up with SAT Limited registration!"
            else ->
                SampleData.simulatedReplies.random()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(userProfile = it.userProfile.copy(themeMode = mode)) }
    }

    fun updateUserProfile(name: String, status: String, phone: String, username: String) {
        val current = _uiState.value.userProfile
        val updated = current.copy(
            name = name.ifBlank { current.name },
            status = status.ifBlank { current.status },
            phone = phone.ifBlank { current.phone },
            username = username.ifBlank { current.username }
        )
        _uiState.update { it.copy(userProfile = updated) }

        val uid = _uiState.value.currentUserId
        if (uid != null) {
            viewModelScope.launch {
                val doc = UserProfileDocument(
                    userId = uid,
                    name = updated.name,
                    email = updated.email,
                    organization = "SAT Limited",
                    username = updated.username,
                    status = updated.status,
                    phone = updated.phone
                )
                userRepository.saveUserProfile(doc)
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(userProfile = it.userProfile.copy(notificationsEnabled = enabled)) }
    }

    fun toggleReadReceipts(enabled: Boolean) {
        _uiState.update { it.copy(userProfile = it.userProfile.copy(readReceiptsEnabled = enabled)) }
    }

    fun toggleEnterIsSend(enabled: Boolean) {
        _uiState.update { it.copy(userProfile = it.userProfile.copy(enterIsSend = enabled)) }
    }

    fun addNewContact(name: String, phone: String, status: String) {
        if (name.isBlank()) return
        val newContact = Contact(
            id = "c_${UUID.randomUUID()}",
            name = name.trim(),
            phone = phone.trim().ifEmpty { "+1 (555) 000-0000" },
            avatarColorHex = listOf(0xFF059669, 0xFF0D9488, 0xFF2563EB, 0xFF9333EA, 0xFFEA580C).random(),
            statusMessage = status.trim().ifEmpty { "Connected with SAT Limited on Kora 🍃" },
            isOnline = true,
            lastSeen = "online"
        )
        _uiState.update { it.copy(contacts = listOf(newContact) + it.contacts) }
        openChatWithContact(newContact)
    }

    fun startCall(contact: Contact, type: CallType) {
        _uiState.update {
            it.copy(
                activeCallContact = contact,
                activeCallType = type
            )
        }
    }

    fun endCall() {
        _uiState.update {
            it.copy(
                activeCallContact = null,
                activeCallType = null
            )
        }
    }

    fun togglePinned(chatId: String) {
        _uiState.update { state ->
            val updated = state.chats.map {
                if (it.id == chatId) it.copy(isPinned = !it.isPinned) else it
            }
            state.copy(chats = updated)
        }
    }
}
