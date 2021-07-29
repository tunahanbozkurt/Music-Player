package com.tunahan.musicplayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.tunahan.musicplayer.adapter.ClickListener
import com.tunahan.musicplayer.adapter.RecyclerViewAdapter
import com.tunahan.musicplayer.database.AppDatabase
import com.tunahan.musicplayer.databinding.ActivityMainBinding
import com.tunahan.musicplayer.model.Musics
import com.tunahan.musicplayer.service.ForegroundService
import com.tunahan.musicplayer.service.MyMP
import kotlinx.android.synthetic.main.grid_list.view.*
import java.io.File


private var ModelList: List<Musics> = arrayListOf()
private lateinit var sharedPref:SharedPreferences
private lateinit var recyclerViewAdapter: RecyclerViewAdapter
@SuppressLint("StaticFieldLeak")
private lateinit var binding: ActivityMainBinding
private lateinit var mediaPlayer: MediaPlayer
private  var currentPosition:Int = 0
private lateinit var editor:SharedPreferences.Editor
private var saveduid:Int? = null
private lateinit var path:String
private var nextMusic = 0
private var positionplus = 0
private  var runnable = Runnable {  }
private var handler = Handler()
private lateinit var sharedPref2:SharedPreferences
private lateinit var editor2:SharedPreferences.Editor
private  var loc:View? = null
private lateinit var nextPath:String
private var dialogstatue:Boolean = true





@Suppress("NAME_SHADOWING", "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity(),ClickListener {
    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(findViewById(R.id.main_toolbar))



        refreshing(this, this)
        val seekBar = binding.seekBar

        binding.playButton.setOnClickListener { playButton() }
        binding.nextButton.setOnClickListener { nextButton() }
        binding.previousButton.setOnClickListener { previousButton() }

        if(dialogstatue){
            AlertDialog.Builder(this)
                .setTitle("UYARI")
                .setMessage("Müzikler Görüntülenebilmesi İçin '/Dahili depolama/Download' Uzantılı Klasör İçerisinde Bulunmalıdır. ")
                .setPositiveButton("Tamam",DialogInterface.OnClickListener { dialog, which ->

                })
                .setNegativeButton("Tekrar Gösterme",DialogInterface.OnClickListener { dialog, which ->
                    dialogstatue = false
                    editor2.putBoolean("dialog",false)
                    editor2.apply()

                })
                .show()
        }








        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentPosition = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val endPoint = seekBar?.progress
                mediaPlayer.seekTo(endPoint!!)
            }

        })
    }

    @SuppressLint("CommitPrefEdits")
    fun refreshing(context: Context, activity: MainActivity) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                0
            )
        } else {
            val db = Room.databaseBuilder(
                applicationContext, AppDatabase::class.java,
                "main_database"
            ).allowMainThreadQueries()
                .build()
            db.clearAllTables()

            val path = "/storage/emulated/0/Download/"
            val directory = File(path)
            val files = directory.listFiles()


            var i = 0

            while (i < files.size) {
                for (input in files) {
                    i += 1
                    if (input.name.endsWith(".mp4") || input.name.endsWith(".mp3")) {
                        var name = input.name
                        if(name.length > 20){
                            name = name.substring(0,20)
                        }
                        val dbItem = Musics(filename = name, filepath = path + input.name)
                        val musicsDao = db.MusicsDao()
                        musicsDao.insertAll(dbItem)
                        ModelList = musicsDao.getAll()



                    }
                }
            }

        }
        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.layoutManager = layoutManager
        recyclerViewAdapter = RecyclerViewAdapter(ModelList = ModelList, this)
        binding.recyclerView.adapter = recyclerViewAdapter
        recyclerViewAdapter.notifyDataSetChanged()
        sharedPref = this.getSharedPreferences("com.tunahan.musicplayer", MODE_PRIVATE)
        sharedPref2 = this.getSharedPreferences("com.tunahan.musicplayer2", MODE_PRIVATE)
        editor2 = sharedPref2.edit()
        dialogstatue = sharedPref2.getBoolean("dialog",true)
        editor2.apply()
        editor = sharedPref.edit()
        editor.clear()
        editor.apply()

        if (ModelList.isNotEmpty()) {
            path = ModelList[0].filepath
        }

        positionplus = 0
        mediaPlayer = MyMP

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 0) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                refreshing(this, this)
            } else {
                finish()
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.toolbar_items, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_refresh -> {
            refreshing(this, this)
            true
        }
        else -> {
            Toast.makeText(applicationContext, "UNEXPECTED ERROR", Toast.LENGTH_LONG).show()
            super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("CommitPrefEdits")
    override fun buttonClicked(view: View, model: Musics, position: Int) {

        path = model.filepath
        nextMusic = position
        positionplus = position
        handler.removeCallbacks(runnable)

        saveduid = sharedPref.getInt("changed", model.uid)
        editor = sharedPref.edit()
        editor.putInt("changed", position)
        editor.apply()

        editor2.putInt("positionplus", positionplus)
        editor2.apply()

        println(saveduid)
        println(position)
        if (saveduid == positionplus) {

            if (mediaPlayer.isPlaying) {
                ForegroundService.startService(
                    this, path = path, action = "ACTION_PAUSE",
                    positionplus, ModelList[positionplus].filename
                )


                binding.seekbartextend.text = createTimeLabel(mediaPlayer.duration)

                binding.playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)

            } else {
                ForegroundService.startService(
                    this, path = path, action = "ACTION_PLAY",
                    positionplus,ModelList[positionplus].filename
                )




                binding.seekbartextend.text = createTimeLabel(mediaPlayer.duration)

                mediaPlayer.setOnCompletionListener {
                    nextButton()
                }

                binding.playButton.setImageResource(R.drawable.newpause)
                runnable = Runnable {

                    view.image.animate().scaleX(1.1f).scaleY(1.1f).setDuration(700).withEndAction {
                        view.image.scaleX = 1f
                        view.image.scaleY = 1f
                    }
                    binding.seekBar.max = mediaPlayer.duration
                    binding.seekBar.progress = mediaPlayer.currentPosition
                    binding.seekbarTextstart.text = createTimeLabel(mediaPlayer.currentPosition)
                    binding.seekbartextend.text = createTimeLabel(mediaPlayer.duration)
                    handler.postDelayed(runnable, 1000)
                }
                handler.post(runnable)

            }


        } else {


            ForegroundService.startService(
                this, path = path, action = "ACTION_RESET",
                positionplus,ModelList[positionplus].filename
            )



            binding.seekbartextend.text = createTimeLabel(mediaPlayer.duration)

            mediaPlayer.setOnCompletionListener {
                nextButton()
            }
            binding.playButton.setImageResource(R.drawable.newpause)
            runnable = Runnable {
                view.image.animate().scaleX(1.1f).scaleY(1.1f).setDuration(700).withEndAction {
                    view.image.scaleX = 1f
                    view.image.scaleY = 1f
                }
                binding.seekBar.max = mediaPlayer.duration
                binding.seekBar.progress = mediaPlayer.currentPosition
                binding.seekbarTextstart.text = createTimeLabel(mediaPlayer.currentPosition)
                binding.seekbartextend.text = createTimeLabel(mediaPlayer.duration)
                handler.postDelayed(runnable, 1000)
            }
            handler.post(runnable)
        }


    }

     fun playButton() {
        if (ModelList.isNotEmpty()){
            handler.removeCallbacks(runnable)
            editor2.putInt("positionplus", positionplus)
            editor2.apply()


            if (mediaPlayer.isPlaying) {
                ForegroundService.startService(
                    this, path = path, action = "ACTION_PAUSE",
                    positionplus,ModelList[positionplus].filename
                )
                binding.seekbartextend.text = createTimeLabel(mediaPlayer.duration)

                binding.playButton.setImageResource(R.drawable.newplay)

            } else {
                ForegroundService.startService(
                    this, path = path, action = "ACTION_PLAY",
                    positionplus,ModelList[positionplus].filename
                )


                mediaPlayer.setOnCompletionListener {
                    nextButton()
                }
                binding.playButton.setImageResource(R.drawable.newpause)

                runnable(positionplus)


            }



        }
        else{
            Toast.makeText(this,"Hiçbir Müzik Dosyası Bulunamadı!",Toast.LENGTH_LONG).show()

        }
    }



    private fun nextButton() {
        if (ModelList.isNotEmpty()){
            handler.removeCallbacks(runnable)
            positionplus = sharedPref2.getInt("positionplus", 0) + 1
            editor2.putInt("positionplus", positionplus)
            editor2.apply()
            editor.putInt("changed", positionplus)
            editor.apply()


            if (positionplus >= ModelList.size) {
                editor2.putInt("positionplus", -1)
                editor2.apply()
                nextButton()
            } else {
                nextPath = ModelList[positionplus].filepath

                ForegroundService.startService(
                    this, path = nextPath, action = "ACTION_NEXT",
                    positionplus,ModelList[positionplus].filename
                )

                binding.seekbartextend.text = createTimeLabel(mediaPlayer.duration)

                mediaPlayer.setOnCompletionListener {
                    nextButton()
                }

                binding.playButton.setImageResource(R.drawable.newpause)

                runnable(positionplus)




            }
        }
        else{
            Toast.makeText(this,"Hiçbir Müzik Dosyası Bulunamadı! ",Toast.LENGTH_LONG).show()

        }



    }

    private fun previousButton() {

        if (ModelList.isNotEmpty()){
            handler.removeCallbacks(runnable)
            positionplus = sharedPref2.getInt("positionplus", 0) - 1
            editor2.putInt("positionplus", positionplus)
            editor2.apply()
            editor.putInt("changed", positionplus)
            editor.apply()

            if (positionplus < 0) {
                editor2.putInt("positionplus", ModelList.size)
                editor2.apply()
                previousButton()
            }
            else {
                val previousPath = ModelList[positionplus].filepath
                ForegroundService.startService(this,path = previousPath,action = "ACTION_PREVIOUS",
                    positionplus,ModelList[positionplus].filename)

                binding.seekbartextend.text = createTimeLabel(mediaPlayer.duration)

                mediaPlayer.setOnCompletionListener {
                    nextButton()
                }
                binding.playButton.setImageResource(R.drawable.newpause)

                runnable(positionplus)
                mediaPlayer.reset()

            }
        }
        else{
            Toast.makeText(this,"Hiçbir Müzik Dosyası Bulunamadı!",Toast.LENGTH_LONG).show()

        }



    }
    fun uiUpdate(data:Int){
        binding.playButton.setImageResource(data)
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        ForegroundService.stopService(this)
        if (mediaPlayer.isPlaying){
            mediaPlayer.reset()
        }
        super.onDestroy()
    }


    fun createTimeLabel(duration: Int): String? {
        var timeLabel: String? = ""
        val min = duration / 1000 / 60
        val sec = duration / 1000 % 60
        timeLabel += "$min:"
        if (sec < 10) timeLabel += "0"
        timeLabel += sec
        return timeLabel
    }

     fun runnable(positionplus:Int){
        handler.removeCallbacks(runnable)
        runnable = Runnable {
            loc = binding.recyclerView.findViewHolderForAdapterPosition(positionplus)?.itemView?.image
            loc?.animate()?.scaleX(1.1f)?.scaleY(1.1f)?.setDuration(700)?.withEndAction {
                (loc as ImageView).scaleX = 1f
                (loc as ImageView).scaleY = 1f
            }
            binding.seekBar.max = mediaPlayer.duration
            binding.seekBar.progress = mediaPlayer.currentPosition

            binding.seekbarTextstart.text = createTimeLabel(mediaPlayer.currentPosition)
            binding.seekbartextend.text = createTimeLabel(mediaPlayer.duration)
            handler.postDelayed(runnable, 1000)
        }
        handler.post(runnable)
    }


        override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
            when(keyCode){
                KeyEvent.KEYCODE_HEADSETHOOK-> {
                    playButton()
                    return true
                }
            }
            return super.onKeyDown(keyCode, event)
        }







}



