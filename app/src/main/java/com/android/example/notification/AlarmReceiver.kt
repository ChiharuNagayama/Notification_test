package com.android.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val smsintent = Intent(context, SoundManageService::class.java)
        smsintent.putExtra("TITLE","たいとるだよ")
        smsintent.putExtra("BODY","ぼでぃだよaaa")
        context?.startService(smsintent)
    }

}