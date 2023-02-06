package com.android.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("AlarmReveiver", "onReceive");

        val smsintent = Intent(context, SoundManageService::class.java)

        val title=intent?.getStringExtra("TITLE")
        val body=intent?.getStringExtra("BODY")

        smsintent.putExtra("TITLE",title)
        smsintent.putExtra("BODY",body)
        context?.startService(smsintent)
    }

}