package com.example.foregroundservice1

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
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
        // initialize dependencies here (e.g. perform dependency injection)
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
//        throw UnsupportedOperationException() // bound Service is a different story
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "onStartCommand[Begin]")
        if (!isStarted) {
            Log.d(LOG_TAG, "onStartCommand1")
            makeForeground()

            // place here any logic that should run just once when the Service is started
            val demoString = intent?.getStringExtra(EXTRA_DEMO) ?: ""
            Log.d(LOG_TAG, "demoString = $demoString")
            someTask()

            isStarted = true

        }
        return START_STICKY // makes sense for a Foreground Service, or even START_REDELIVER_INTENT
    }

    fun someTask() {
        Thread {
            for (i in 1..120) {
                Log.d(LOG_TAG, "i = $i")
                try {
                    TimeUnit.SECONDS.sleep(1)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            stopSelf()
            isStarted = false
        }.start()
    }

    private fun makeForeground() {
        val intent = Intent(this, MainActivity::class.java)
        Log.d(LOG_TAG, "makeForeground1")
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        Log.d(LOG_TAG, "makeForeground2")
        // before calling startForeground, we must create a notification and a corresponding
        // notification channel

        createServiceNotificationChannel()
        Log.d(LOG_TAG, "makeForeground3")
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText("Foreground Service demonstration")
            .setSmallIcon(R.drawable.small_icon)
            .setContentIntent(pendingIntent)
            .build()
        Log.d(LOG_TAG, "makeForeground4")
        ServiceCompat.startForeground(this, ONGOING_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        Log.d(LOG_TAG, "makeForeground5")
    }
    private fun createServiceNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) {
            return // notification channels were added in API 26
        }
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Foreground Service channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager.createNotificationChannel(channel)
    }
    companion object {
        private const val ONGOING_NOTIFICATION_ID = 101
        private const val CHANNEL_ID = "1001"
        private const val EXTRA_DEMO = "EXTRA_DEMO"
        fun startService(context: Context, demoString: String) {
            Log.d(LOG_TAG, "startService1")
            val intent = Intent(context, ForegroundService::class.java)
            Log.d(LOG_TAG, "startService2")
            intent.putExtra(EXTRA_DEMO, demoString)
            Log.d(LOG_TAG, "startService3")
            if (Build.VERSION.SDK_INT < 26) {
                Log.d(LOG_TAG, "startService41")
                context.startService(intent)
                Log.d(LOG_TAG, "startService42")
            } else {
                Log.d(LOG_TAG, "startService51")
                context.startForegroundService(intent)
                Log.d(LOG_TAG, "startService52")
            }
        }
        fun stopService(context: Context) {
            val intent = Intent(context, ForegroundService::class.java)
            context.stopService(intent)
        }
    }
}