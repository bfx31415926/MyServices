package com.example.study95_new

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
import com.example.study95_new.MyService.myObject.firstDelayDone
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

const val LOG_TAG = "myLogs"

class MyService: Service() {
    object myObject {
        var firstDelayDone = false
    }
    private lateinit var notificationManager: NotificationManager
    private lateinit var es: ExecutorService

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        es = Executors.newFixedThreadPool(2)
        Log.d(LOG_TAG, "MyService onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, "MyService onDestroy")
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(LOG_TAG, "onBind")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "MyService onStartCommand")

        makeForeground()

        val time = intent!!.getIntExtra(MainActivity.PARAM_TIME, 1)
        val pi = intent.getParcelableExtra<PendingIntent>(MainActivity.PARAM_PINTENT)
        val mr = MyRun(time, startId, pi!!)
        es.execute(mr)

        return super.onStartCommand(intent, flags, startId)
    }
    inner class MyRun(var time: Int, var startId: Int, var pi: PendingIntent) : Runnable {
        init {
            Log.d(LOG_TAG, "MyRun#$startId create")
        }
        override fun run() {
            Log.d(LOG_TAG, "MyRun#$startId start, time = $time")
            try {
                if(!firstDelayDone){
                    TimeUnit.SECONDS.sleep(8)
                    firstDelayDone = true;
                }
                // сообщаем о старте задачи
                pi.send(MainActivity.STATUS_START)

                // начинаем выполнение задачи
                TimeUnit.SECONDS.sleep(time.toLong())

                // сообщаем об окончании задачи
                val intent = Intent().putExtra(MainActivity.PARAM_RESULT, time * 100)
                pi.send(this@MyService, MainActivity.STATUS_FINISH, intent)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: PendingIntent.CanceledException) {
                e.printStackTrace()
            }
            stop()
        }

        fun stop() {
            Log.d(
                LOG_TAG, "MyRun#" + startId + " end, stopSelfResult("
                        + startId + ") = " + stopSelfResult(startId)
            )

        }
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
            .build()
        ServiceCompat.startForeground(this, ONGOING_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
//        startForeground(ONGOING_NOTIFICATION_ID, notification)
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
        fun startService(context: Context, intent: Intent) {
            if (Build.VERSION.SDK_INT < 26) {
                context.startService(intent)
            } else {
                context.startForegroundService(intent)
            }
        }
        fun stopService(context: Context) {
            val intent = Intent(context, MyService::class.java)
            context.stopService(intent)
        }
    }
}
