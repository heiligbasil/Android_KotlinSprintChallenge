package com.lambdaschool.android_kotlinsprintchallenge

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.SeekBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.button_retrieve.setOnClickListener { showVideo(edit_text_retrieve.text.toString()) }

        this.seek_bar!!.setOnSeekBarChangeListener(this)

        this.image_button.setOnClickListener {
            if (video_view.isPlaying) {
                video_view.pause()
                image_button.setImageResource(android.R.drawable.ic_media_play)
                seekbarUpdateHandler.removeCallbacks(updateSeekbar);
            } else {
                video_view.start()
                image_button.setImageResource(android.R.drawable.ic_media_pause)
                seekbarUpdateHandler.postDelayed(updateSeekbar, 0);
            }
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser)
            video_view.seekTo(progress)
        seekBar?.secondaryProgress = video_view.bufferPercentage / 100 * video_view.duration
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        seekBar?.progress?.let { this.video_view.seekTo(it) }
    }

    private val seekbarUpdateHandler = Handler()
    private val updateSeekbar = object : Runnable {
        override fun run() {
            seek_bar.setProgress(video_view.currentPosition)
            seekbarUpdateHandler.postDelayed(this, 50)
        }
    }

    fun showVideo(videoId: String) {
        video_view?.stopPlayback()
        var videoModel: VideoModel? = null
        val job = Job()
        val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)

        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                videoModel = HubbleVideoDao.retrieveHubbleVideoDataById(videoId)
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, videoModel?.short_description, Toast.LENGTH_LONG).show()
                //val mediaController: MediaController? = MediaController(this@MainActivity)
                image_button.setImageResource(android.R.drawable.ic_media_play)
                video_view.stopPlayback()
                video_view.setVideoPath(videoModel?.getVideoUrl())
                //mediaController?.setAnchorView(video_view)
                //video_view.setMediaController(mediaController)
                video_view.setOnPreparedListener {
                    seek_bar.max = video_view.duration
                    seek_bar.progress = 0
                }
                video_view.setOnCompletionListener { image_button.setImageResource(android.R.drawable.ic_media_play) }
            }
        }
    }
}