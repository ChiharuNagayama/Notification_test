package com.android.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.android.example.notification.databinding.ActivityMainBinding
import com.android.example.notification.ui.notification.NotificationManageFragment
import com.android.example.notification.ui.setting.SettingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navView: BottomNavigationView = binding.navView
        navView.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        navView.setupWithNavController(navController)



        /**ここからアラーム機能の実装
         * workで繰り返し時間を取得する→有る一定の時間になったらserviceに行く
         * */
// WorkRequest を作成　　　　　　　　　　　　　　　　　　　　　　　　　　　　一時間に一度にあとでかえる
    val notifyWorkRequest = PeriodicWorkRequestBuilder<NotifyWork>(1, TimeUnit.SECONDS)

    .build()

        // システムに WorkRequest を送信する
        WorkManager
            .getInstance(applicationContext)
            .enqueue(notifyWorkRequest)



        // Intentオブジェクトからデータを取得
// <- アクティビティクラスはIntentオブジェクトをintentプロパティとして保持
//        val intent = Intent(application, SoundManageService::class.java)
//        startService(intent)


//ここからアラームマネージャのコーディング
//
//        //実行するクラスを指定
//        val alarmMgr: AlarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val alarmIntent: PendingIntent =
//            Intent(application, AlarmReceiver::class.java).let { intent ->
//                intent.putExtra("TITLE", "たいとるだよ")
//                intent.putExtra("BODY", "ぼでぃだよおおおお")
//                PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//            }
//
//
//        // レシーバの登録
//        val ar = AlarmReceiver()
//        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
//        registerReceiver(ar, filter)
//
////        5秒後に1回のみ通知
//        alarmMgr.set(
//            AlarmManager.ELAPSED_REALTIME_WAKEUP,
//            SystemClock.elapsedRealtime() + 100 * 1000,
//            alarmIntent
//        )
//
//
//
//////        起動して５秒後通知、そのあと１０秒ごとに通知
////        alarmMgr.setInexactRepeating(
////            AlarmManager.ELAPSED_REALTIME_WAKEUP,
////            SystemClock.elapsedRealtime() + 5 * 1000,
////            10 * 1000,
////            alarmIntent
////        )
//
//////起動する時間を指定（16時）
////        val calendar: Calendar = Calendar.getInstance().apply {
////            timeInMillis = System.currentTimeMillis()
////            set(Calendar.HOUR_OF_DAY, 16)
////        }
////
//////指定時間（16時）にアラームを実行、以降は1日間隔
////        alarmMgr.setInexactRepeating(
////            AlarmManager.RTC_WAKEUP,
////            calendar.timeInMillis,
////            AlarmManager.INTERVAL_DAY,
////            alarmIntent
////        )

//    ここまでアラームマネージャーのコーディング

    }
}