package com.tunahan.musicplayer.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tunahan.musicplayer.dao.MusicsDao
import com.tunahan.musicplayer.model.Musics

@Database(entities = arrayOf(Musics::class),version = 1)
abstract class AppDatabase:RoomDatabase() {
    abstract fun MusicsDao(): MusicsDao
}