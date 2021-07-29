package com.tunahan.musicplayer.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "musics")
data class Musics(
    @PrimaryKey(autoGenerate = true) var uid:Int= 0,
    @ColumnInfo(name = "file_name") val filename:String,
    @ColumnInfo(name = "file_path") val filepath:String
    ):Serializable
