package com.nurbk.ps.demochat.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nurbk.ps.demochat.db.ChatDatabase
import com.nurbk.ps.demochat.model.Message
import com.nurbk.ps.repository.ChatRepository
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val chatRepository =
        ChatRepository(ChatDatabase(application.applicationContext))
    fun insert(message: Message) = viewModelScope.launch {
        chatRepository.insert(message)
    }
    fun getAllChat(id: String) = chatRepository.getAllChat(id)

}