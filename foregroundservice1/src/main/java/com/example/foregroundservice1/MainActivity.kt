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
        if (v.id == R.id.btnStart) {
            ForegroundService.startService(this, "some string to ForegroundService")
        }
    }
}
