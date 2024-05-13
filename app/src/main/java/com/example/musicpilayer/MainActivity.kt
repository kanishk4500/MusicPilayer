package com.example.musicpilayer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musicpilayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState : Bundle?){
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val selectBtn = binding.chooseMusicButton
        selectBtn.setOnClickListener{
            launchAudioFilePickerIntent()
        }
    }

    private fun launchAudioFilePickerIntent(){
        val intent = Intent(Intent.ACTION_GET_CONTENT) //OPEN FILE PICKER
        intent.type = "audio/*" //OPEN AUDIO FILES WITH ANY EXT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(Intent.createChooser(intent,"Select Audio File"),REQUEST_AUDIO_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_AUDIO_FILE){
            if(resultCode == Activity.RESULT_OK){
                val resultData = data?.let{
                    it.data
                }
                Log.d(TAG,"$resultData was returned")
                //pass the uri data to the player intent
                callPlayerActivity(resultData)
            }
            else{
                //Result was cancelled
                Toast.makeText(this,"Some error occurred",Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun callPlayerActivity(uri : Uri?){
        //call the player activity with the uri
        uri?.let{
            val intent = Intent(this,PlayerActivity::class.java)
            intent.data = it
            startActivity(intent)
        }
    }



    companion object{
        private const val REQUEST_AUDIO_FILE = 1000
        private const val TAG = "MAIN-ACTIVITY"
    }


}