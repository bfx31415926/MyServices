/*
    ПРИМЕЧАНИЯ:
    toDo В реализации урока не предусмотрена
         остановка службы
 */
package com.example.study98;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    final String LOG_TAG = "myLogs";

    boolean bound = false;
    ServiceConnection sConn;
    Intent intent;
    MyService myService;
    TextView tvInterval;
    long interval;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tvInterval = (TextView) findViewById(R.id.tvInterval);
        intent = new Intent(this, MyService.class);
        sConn = new ServiceConnection() {

            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(LOG_TAG, "MainActivity onServiceConnected");
                myService = ((MyService.MyBinder) binder).getService();
                bound = true;
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(LOG_TAG, "MainActivity onServiceDisconnected");
                bound = false;
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "BEFORE bindService");
        bindService(intent, sConn, 0);
        Log.d(LOG_TAG, "AFTER bindService");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!bound) {
            Log.d(LOG_TAG, "onStop[-1]");
            return;
        }
        Log.d(LOG_TAG, "onStop[1]");
        unbindService(sConn);
        Log.d(LOG_TAG, "onStop[2]");
        bound = false;
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

    public void onClickUp(View v) {
        if (!bound) return;
        interval = myService.upInterval(500);
        tvInterval.setText("interval = " + interval);
    }

    public void onClickDown(View v) {
        if (!bound) return;
        interval = myService.downInterval(500);
        tvInterval.setText("interval = " + interval);
    }
}