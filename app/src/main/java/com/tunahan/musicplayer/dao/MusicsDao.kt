package com.tunahan.musicplayer.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.tunahan.musicplayer.model.Musics

@Dao
interface MusicsDao {
    @Query("SELECT * FROM musics")
    fun getAll(): List<Musics>


    @Insert
    fun insertAll(vararg musics: Musics)

    @Delete()
    fun delete(musics: Musics)
}