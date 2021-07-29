package com.tunahan.musicplayer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.tunahan.musicplayer.MainActivity
import com.tunahan.musicplayer.R
import com.tunahan.musicplayer.database.AppDatabase
import com.tunahan.musicplayer.model.Musics

private lateinit var mediaPlayer: MediaPlayer
private lateinit var sharedPref2: SharedPreferences
private lateinit var editor2: SharedPreferences.Editor
private var seekItem:Int? = null
private var image:Int = R.drawable.ic_baseline_pause_24
private var pausevalue:String = "ACTION_PAUSE"
private lateinit  var ModelList: List<Musics>
private var filename:String? = null
private var path:String? = null
private var nextPath:Int? = null
private lateinit var sharedPref:SharedPreferences
private lateinit var editor:SharedPreferences.Editor


open class ForegroundService: Service() {

    private val CHANNEL_ID = "ForegroundServiceChannel"

    companion object {

        fun startService(
            context: Context,
            path: String,
            action: String,
            positionplus: Int,
            filename:String
        ) {
            val startIntent = Intent(context, ForegroundService::class.java)
            startIntent.putExtra("path", path)
            startIntent.putExtra("action", action)
            startIntent.putExtra("positionplus", positionplus)
            startIntent.putExtra("filename",filename)
            ContextCompat.startForegroundService(context, startIntent)

        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, ForegroundService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        path = intent?.getStringExtra("path")
        val action = intent?.getStringExtra("action")
        nextPath = sharedPref2.getInt("positionplus",0)
        filename = intent?.getStringExtra("filename")

        action?.let { actionFunc(it) }




        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )

        val buttonIntent = Intent(this, ForegroundService::class.java)
        buttonIntent.putExtra("action", "ACTION_SPREVIOUS")
        buttonIntent.putExtra("filename", filename)

        val buttonIntent2 = Intent(this, ForegroundService::class.java)
        buttonIntent2.putExtra("action", pausevalue)
        buttonIntent2.putExtra("filename", filename)




        val buttonIntent3 = Intent(this, ForegroundService::class.java)
        buttonIntent3.putExtra("action", "ACTION_SNEXT")
        buttonIntent3.putExtra("nextpath", nextPath)
        buttonIntent3.putExtra("filename", filename)



        val previouspendingIntent =
            PendingIntent.getService(this, 2, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val pauspendingIntent =
            PendingIntent.getService(this, 3, buttonIntent2, PendingIntent.FLAG_UPDATE_CURRENT)
        val nextpendingIntent =
            PendingIntent.getService(this, 4, buttonIntent3, PendingIntent.FLAG_UPDATE_CURRENT)


        val picture: Bitmap = BitmapFactory.decodeResource(resources,R.mipmap.ic_launcher_foreground)
        val mediaSession = MediaSessionCompat(this, "Main_Session")

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentTitle("Music Player")
            .setContentText("${filename}")
            .setLargeIcon(picture)
            .addAction(R.drawable.ic_baseline_skip_previous_24, "Previous", previouspendingIntent)
            .addAction(image, "Paus", pauspendingIntent)
            .addAction(R.drawable.ic_baseline_skip_next_24, "Next", nextpendingIntent)
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .setContentIntent(pendingIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)

                    .setMediaSession(mediaSession.sessionToken)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(1, notification)


        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }


    override fun onDestroy() {
        nextPath = 0
        super.onDestroy()
    }


    override fun onCreate() {
        super.onCreate()
        sharedPref2 = this.getSharedPreferences("com.tunahan.musicplayer2", MODE_PRIVATE)
        editor2 = sharedPref2.edit()
        editor2.apply()

        sharedPref = this.getSharedPreferences("com.tunahan.musicplayer", MODE_PRIVATE)
        editor = sharedPref.edit()
        editor.clear()
        editor.apply()

        val db = Room.databaseBuilder(
            applicationContext, AppDatabase::class.java,
            "main_database"
        ).allowMainThreadQueries()
            .build()

        val musicsDao = db.MusicsDao()
        ModelList = musicsDao.getAll()
        mediaPlayer = MyMP
        mediaPlayer.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }






    }


    private fun actionFunc(action:String?){
        when (action) {
            "ACTION_PLAY" -> {
                MainActivity().uiUpdate(R.drawable.newpause)
                pausevalue = "ACTION_PAUSE"
                if (seekItem != null) {
                    mediaPlayer.apply {
                        seekTo(seekItem!!)
                        start()
                    }
                    seekItem = null
                    image = R.drawable.ic_baseline_pause_24


                } else {
                    mediaPlayer.reset()
                    mediaPlayer.apply {
                        setDataSource(path)
                        prepare()
                        start()

                    }
                    image = R.drawable.ic_baseline_pause_24
                    


                }
            }
            "ACTION_PAUSE" -> {
                pausevalue = "ACTION_PLAY"
                mediaPlayer.pause()
                seekItem = mediaPlayer.currentPosition
                image = R.drawable.ic_baseline_play_arrow_24
                MainActivity().uiUpdate(R.drawable.newplay)
                filename = ModelList[nextPath!!].filename



            }
            "ACTION_NEXT" -> {

                mediaPlayer.reset()
                mediaPlayer.apply {
                    setDataSource(path)
                    prepare()
                    start()
                }
                image = R.drawable.ic_baseline_pause_24


            }
            "ACTION_PREVIOUS" -> {
                mediaPlayer.reset()
                mediaPlayer.apply {
                    setDataSource(path)
                    prepare()
                    start()
                }
                image = R.drawable.ic_baseline_pause_24


            }
            "ACTION_RESET" -> {
                seekItem = null
                mediaPlayer.reset()
                actionFunc("ACTION_PLAY")



            }
            "ACTION_SNEXT" -> {
                nextPath = nextPath?.plus(1)
                MainActivity().uiUpdate(R.drawable.newpause)
                nextPath?.let { MainActivity().runnable(it) }
                nextPath?.let { editor.putInt("changed", it) }
                editor.apply()

                if (nextPath!! >= ModelList.size){
                    nextPath = -1
                    editor2.putInt("positionplus", nextPath!!)
                    editor2.apply()
                    actionFunc(action)
                    image = R.drawable.ic_baseline_pause_24

                }
                else{
                    mediaPlayer.reset()
                    mediaPlayer.apply {
                        setDataSource(ModelList[nextPath!!].filepath)
                        prepare()
                        start()
                    }
                    image = R.drawable.ic_baseline_pause_24
                    editor2.putInt("positionplus", nextPath!!)
                    editor2.apply()
                    filename = ModelList[nextPath!!].filename

                }
            }
            "ACTION_SPREVIOUS" -> {
                nextPath = nextPath?.minus(1)
                MainActivity().uiUpdate(R.drawable.newpause)
                nextPath?.let { MainActivity().runnable(it) }
                if (nextPath!! < 0){
                    nextPath = ModelList.size
                    editor2.putInt("positionplus", nextPath!!)
                    editor2.apply()
                    actionFunc(action)
                    image = R.drawable.ic_baseline_pause_24

                }
                else{
                    mediaPlayer.reset()
                    mediaPlayer.apply {
                        setDataSource(ModelList[nextPath!!].filepath)
                        prepare()
                        start()
                    }
                    image = R.drawable.ic_baseline_pause_24
                }
                editor2.putInt("positionplus", nextPath!!)
                editor2.apply()
                editor.putInt("changed", nextPath!!)
                editor.apply()
                filename = ModelList[nextPath!!].filename

             }


        }
    }













}




