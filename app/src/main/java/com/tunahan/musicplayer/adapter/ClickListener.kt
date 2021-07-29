package com.tunahan.musicplayer.adapter

import android.view.View
import com.tunahan.musicplayer.model.Musics

interface ClickListener {
    fun buttonClicked(view: View,model: Musics,position:Int)

}