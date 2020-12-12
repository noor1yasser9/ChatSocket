package com.nurbk.ps.demochat.db

import  androidx.lifecycle.LiveData
import androidx.room.*
import com.nurbk.ps.demochat.model.Message

@Dao
interface ChatDao {


    @Insert
    suspend fun insert(message: Message)

    @Query("SELECT * FROM MessageTable WHERE idUser LIKE :id ORDER BY id DESC")
    fun getAllChat(id: String): LiveData<List<Message>>


}