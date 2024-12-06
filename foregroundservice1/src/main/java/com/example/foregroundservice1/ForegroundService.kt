package com.example.foregroundservice1

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import java.util.concurrent.TimeUnit

const val LOG_TAG = "myLogs"

class ForegroundService: Service() {

    private lateinit var notificationManager: NotificationManager

    // onStartCommand can be called multiple times, so we keep track of "started" state manually
    private var isStarted = false

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.d(LOG_TAG, "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        isStarted = false
        Log.d(LOG_TAG, "onDestroy")
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(LOG_TAG, "onBind")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "onStartCommand")
        if (!isStarted) {
            makeForeground()
            isStarted = true
        }

        readFlags(flags)

        // place here any logic that should run just once when the Service is started
        val demoString = intent?.getStringExtra(EXTRA_DEMO) ?: ""
        Log.d(LOG_TAG, "demoString = $demoString")
        someTask()
        return super.onStartCommand(intent, flags, startId)
//        return START_STICKY // makes sense for a Foreground Service, or even START_REDELIVER_INTENT
    }

    fun readFlags(flags: Int) {
        Log.d(LOG_TAG,"flags = $flags")
        if (flags and START_FLAG_REDELIVERY == START_FLAG_REDELIVERY)
            Log.d(LOG_TAG,"START_FLAG_REDELIVERY")
        if (flags and START_FLAG_RETRY == START_FLAG_RETRY)
            Log.d(LOG_TAG, "START_FLAG_RETRY")
    }

    fun someTask() {
        Thread {
            for (i in 1..200) {
                if (!isStarted)
                    break

                Log.d(LOG_TAG, "i = $i")
                try {
                    TimeUnit.SECONDS.sleep(1)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            stopSelf()
        }.start()
    }

    private fun makeForeground() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        // before calling startForeground, we must create a notification and a corresponding
        // notification channel

        createServiceNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText("Foreground Service demonstration")
            .setSmallIcon(R.drawable.small_icon)
            .setContentIntent(pendingIntent)
//            .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+ "://" +applicationContext.getPackageName()+"/"+R.raw.my_sound))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        ServiceCompat.startForeground(this, ONGOING_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    }
    private fun createServiceNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) {
            return // notification channels were added in API 26
        }
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = CHANNEL_DESCRIPTION
        channel.setSound(android.net.Uri.parse("android.resource://" + applicationContext.getPackageName() + "/" + R.raw.my_sound), null)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val ONGOING_NOTIFICATION_ID = 101
        private const val CHANNEL_ID = "1001"
        private const val CHANNEL_NAME = "Foreground Service channel"
        private const val CHANNEL_DESCRIPTION = "Foreground Service channel"
        private const val EXTRA_DEMO = "EXTRA_DEMO"
        fun startService(context: Context, demoString: String) {
            val intent = Intent(context, ForegroundService::class.java)
            intent.putExtra(EXTRA_DEMO, demoString)
            if (Build.VERSION.SDK_INT < 26) {
                context.startService(intent)
            } else {
                context.startForegroundService(intent)
            }
        }
        fun stopService(context: Context) {
            val intent = Intent(context, ForegroundService::class.java)
            context.stopService(intent)
        }
    }
}