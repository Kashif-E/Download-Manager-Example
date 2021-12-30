package com.infinity.downloadmanagerexample

import android.Manifest
import android.annotation.TargetApi
import android.app.DownloadManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.infinity.downloadmanagerexample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // image downloader
    private val downloadManager: DownloadManager by lazy { getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }

    private val references = mutableListOf<Long>()
    private val notificationID = 101
    private val channelID = "com.infinity.downloadmanagerexample"
    private val askLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                Toast.makeText(this, "Pictures are being downloaded.", Toast.LENGTH_SHORT).show()
                download(
                    listOf(
                        "https://cdni.autocarindia.com/Utils/ImageResizer.ashx?n=https://cdni.autocarindia.com/ExtraImages/20210204035447_Ford_Raptor_lead_1.jpg&w=700&q=90&c=1",
                        "https://www.ccarprice.com/products/Ford-F-150-XL-2021.jpg",
                    ), "Ford F150"
                )
            } else {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    lateinit var binding: ActivityMainBinding


    val onComplete = object : BroadcastReceiver() {

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context, intent: Intent) {

            val referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            references.remove(referenceId)


            if (references.isEmpty()) {
                sendNotification()
            }


        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        creatNotificationChannelAndRegisterReceiver()

        binding.button.setOnClickListener {
            askLocationPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }


    }

    fun download(imageList: List<String>, name: String) {

        imageList.forEachIndexed { index, url ->
            try {


                val request = DownloadManager.Request(Uri.parse(url))
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle("title")
                    .setMimeType("image/jpg")
                    .setDescription("Downloading..")
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_PICTURES,
                        "/YourDirectory//${name} $index.jpg"
                    )

                references.add(downloadManager.enqueue(request))

            } catch (e: java.lang.Exception) {
                Toast.makeText(this, "Image download failed.", Toast.LENGTH_SHORT)
                    .show()
            }

        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification() {

        val notification = Notification.Builder(this, channelID)
            .setContentTitle("Download Manager")
            .setContentText("All download complete")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setChannelId(channelID)
            .setNumber(10)
            .build()


        notificationManager.notify(notificationID, notification)

    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {

        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(
            "com.infinity.downloadmanagerexample",
            "DownloadManager",
            importance
        )
        channel.description = "All download complete"
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        notificationManager.createNotificationChannel(channel)
    }

    fun creatNotificationChannelAndRegisterReceiver() {
        createNotificationChannel()

        registerReceiver(
            onComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }

    fun unRegisterBroadcast() {
        unregisterReceiver(onComplete)
    }

    override fun onDestroy() {
        super.onDestroy()
        unRegisterBroadcast()
    }

}