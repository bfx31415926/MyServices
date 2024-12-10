/*
    ПРИМЕЧАНИЯ:
    1. Модули study97_1 и study97_2 соответствуют уроку Виноградова = 97
    2. Виесто обычного сервиса запускаем ForeGround Service
    3. Чтобы ForeGround Service запустился надо не забыть две вещи:
        a) в манифесте study97_1 прописать:
            <queries>
                <package android:name="com.example.study97_2" />
            </queries>
        b) в манифесте study97_2 прописать для <service> интент-фильтр:
            <intent-filter>
                <action android:name="com.example.study97_2.MyService" />

                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
    4. Уведомление, хоть и формируется, как обычно, для ForeGround Service,
       но не выдается. Возможно это из-за того, что сервис вызывается неявным интентом

 */
package com.example.study97_1;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    final String LOG_TAG = "myLogs";

    boolean bound = false;
    ServiceConnection sConn;
    Intent intent;

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Log.d(LOG_TAG, "MainActivity onCreate[BEGIN]");

        intent = new Intent();//"com.example.study97_2");
        intent.setAction("com.example.study97_2.MyService");
        intent.setPackage("com.example.study97_2");

        sConn = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(LOG_TAG, "MainActivity onServiceConnected");
                bound = true;
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(LOG_TAG, "MainActivity onServiceDisconnected");
                bound = false;
            }
        };
        Log.d(LOG_TAG, "MainActivity onCreate1");
    }

    public void onClickStart(View v) {
        Log.d(LOG_TAG, "onClickStart1");
        if (Build.VERSION.SDK_INT < 26) {
            startService(intent);
        } else {
            Log.d(LOG_TAG, "onClickStart11");
            startForegroundService(intent);
            Log.d(LOG_TAG, "onClickStart12");
        }
        Log.d(LOG_TAG, "onClickStart2");
    }

    public void onClickStop(View v) {
        stopService(intent);
    }

    public void onClickBind(View v) {
        Log.d(LOG_TAG, "onClickBind1");
        bindService(intent, sConn, BIND_AUTO_CREATE);
        Log.d(LOG_TAG, "onClickBind2");
    }

    public void onClickUnBind(View v) {
        if (!bound) return;
        unbindService(sConn);
        bound = false;
    }

    protected void onDestroy() {
        super.onDestroy();
        onClickUnBind(null);
    }
}