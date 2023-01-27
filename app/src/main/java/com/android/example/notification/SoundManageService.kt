package com.android.example.notification
import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class SoundManageService : Service() {

    // クラス定数
    companion object {
        private const val CHANNEL_ID = "soundmanagerservice_notification_channel"
    }
    // サービスの初期化時に実行する処理
    // <- 親クラスのonCreate()メソッドの呼び出しは不要

    override fun onCreate() {
        super.onCreate()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        val title=intent?.getStringExtra("TITLE")
        val body=intent?.getStringExtra("BODY")


        // 通知チャネル名
        val name = getString(R.string.notification_channel_name)

        // 通知チャネルの重要度
        // -> NotificationManagerのクラス定数を利用
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        // 通知チャネル
        val channel = NotificationChannel(CHANNEL_ID, name, importance)

        // NotificationManagerオブジェクトの取得
        val manager = getSystemService(NotificationManager::class.java)

        // 通知チャネルをセット
        manager.createNotificationChannel(channel)


        // Notificationオブジェクトを生成するBuilderオブジェクト
        val builder = NotificationCompat.Builder(this@SoundManageService, CHANNEL_ID)

        // 通知エリアに表示するアイコン
        builder.setSmallIcon(android.R.drawable.ic_dialog_info)

        // 通知ドロワーに表示するタイトル
        // -> Context.getString(resId:)メソッドを用いてCharSequence型文字列を取得
        builder.setContentTitle(title)

        // 通知ドロワーに表示するメッセージ
        builder.setContentText(body)

        // Notificationオブジェクト
        val notification = builder.build()

        // サービスクラスからNotificationManagerCompatオブジェクトを取得
        val manager2 = NotificationManagerCompat.from(this@SoundManageService)

        manager2.notify(100, notification)

        //サービス終了
        stopSelf()



        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}