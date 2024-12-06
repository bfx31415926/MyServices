/*
КОММЕНТАРИИ:
1. После старта Foreground Service приходит уведомление(специально сформированное)
2.Выяснилось, что, по крайней мере, START_STICKY для Foreground Service
  не помогает перезапустить службу, если ее грохнет система или
  user остановит (из списка работающих служб)
3. Поскольку подключил к каналу звук (см.  createServiceNotificationChannel()),
   то чтобы он был слышен - не забыть отключить на телефоне опцию "Не беспокоить"
4. Этот модуль сваял на основе сайта [https://www.techyourchance.com/foreground-service-in-android/]
   и видео ютуба [https://www.youtube.com/watch?v=Fd72095vxic]
5. Не смог настроить собственный звук для уведомления - работает дефолтный
 */
package com.example.foregroundservice1

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
    }

    fun onclick(v: View) {
        when(v.id) {
            R.id.btnStart ->
                ForegroundService.startService(this, "some string to ForegroundService")
            R.id.btnStop ->
                ForegroundService.stopService(this)
        }
    }
}
