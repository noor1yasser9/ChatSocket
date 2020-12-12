package com.nurbk.ps.repository

import com.nurbk.ps.demochat.db.ChatDatabase
import com.nurbk.ps.demochat.model.Message

class ChatRepository(val db: ChatDatabase) {

    suspend fun insert(message: Message) =
        db.getArticleDao().insert(message)


    fun getAllChat(id: String) = db.getArticleDao().getAllChat(id)

}