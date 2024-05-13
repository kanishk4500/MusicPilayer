package com.example.musicpilayer

import android.content.ContentResolver
import android.content.ContentValues.TAG
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musicpilayer.databinding.ActivityPlayerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPlayerBinding
    private lateinit var tvSongTitle : TextView
    private lateinit var sbSeekBar: SeekBar
    private lateinit var btnPlayPause : Button
    private lateinit var mediaPlayer : MediaPlayer
    val handler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState : Bundle?){
       super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        tvSongTitle = binding.songTitleTextView
        sbSeekBar = binding.seekBar
        btnPlayPause = binding.playPauseButton
        mediaPlayer = MediaPlayer()

        setContentView(binding.root)


        //receive the intent that opened it
        val intent = intent
        //get the uri data
        val data = intent.data
        if(isUriValid(data)){
            setMusicPlayer(data)
        }
        else{
            Toast.makeText(this,"some error occurred",Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun setMusicPlayer(uri : Uri?){
        //uri is checked and valid
        initMediaPlayer(uri!!)
        val songName = getFileNameFromUri(contentResolver, uri)
        tvSongTitle.text = songName
        sbSeekBar.visibility = View.VISIBLE
        btnPlayPause.visibility = View.VISIBLE
        //setting the seekbar position
        sbSeekBar.max = mediaPlayer.duration
        addOnClickListeners()
        addOnSeekListeners()
        updateSeekBar()
    }

    private fun initMediaPlayer(uri : Uri){
        mediaPlayer = MediaPlayer.create(this,uri)
        sbSeekBar.max = mediaPlayer.duration
        mediaPlayer.setOnCompletionListener {
            Log.d(TAG,"song is over")
            sbSeekBar.progress = 0
            binding.playPauseButton.text = "Play"
            handler.removeCallbacks(runnableUiChange)
        }
    }

    private fun addOnClickListeners(){
        btnPlayPause.setOnClickListener{
            if(mediaPlayer.isPlaying){
                mediaPlayer.pause()
                btnPlayPause.text = "play"
            }
            else{
                mediaPlayer.start()
                btnPlayPause.text = "Pause"
            }
        }
    }

    private val runnableUiChange = object : Runnable {
        override fun run() {
            val currentDuration = mediaPlayer.currentPosition
            sbSeekBar.progress = currentDuration
            Log.d(TAG,"progress from runnable $currentDuration")
            handler.postDelayed(this, 200)
        }
    }

    private fun updateSeekBar(){
        handler.postDelayed(runnableUiChange,200)
    }

    private fun addOnSeekListeners(){
        sbSeekBar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(runnableUiChange)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(runnableUiChange)
                val currentDuration = sbSeekBar.progress
                Log.d(TAG,"progress after stopTrackingTouch $currentDuration")
                mediaPlayer.seekTo(currentDuration)
                updateSeekBar()
            }
        })
    }




    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    private fun isUriValid(uri : Uri?) : Boolean{
        return (uri != null) && isAudioFileByMimeType(uri)
    }

    private fun isAudioFileByMimeType(uri: Uri): Boolean {
        val contentResolver: ContentResolver = contentResolver
        val mimeType: String? = contentResolver.getType(uri)
        return (mimeType != null) && (mimeType.startsWith("audio/"))// Check if MIME type starts with "audio/"
    }

    private fun getFileNameFromUri(contentResolver: ContentResolver, uri: Uri): String? {
        var fileName: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }

    companion object{
        private const val TAG = "PlayerActvity"
    }
}