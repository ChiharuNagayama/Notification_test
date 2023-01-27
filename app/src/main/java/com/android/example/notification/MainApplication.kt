package com.android.example.notification

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.getExtras
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import com.android.example.notification.room.BudgetDataBase
import com.android.example.notification.room.MyDataBase
import com.android.example.notification.room.NotificationDataBase
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.android.example.notification.SoundManageService

class MainApplication: Application()  {

    var spinnerMonth:String = "1"
    var notificationDataBase:NotificationDataBase? = null
    var categoryDataBase:MyDataBase? = null
    var budgetDataBase: BudgetDataBase? = null
    var isEditBudget: Boolean = false
    var totalBudgetValue = 34000
    var mainKeyValue = 0






    override fun onCreate() {
        super.onCreate()
        saveData(this,"totalBudgetValue",totalBudgetValue)
        notificationDataBase = Room.databaseBuilder(this, NotificationDataBase::class.java, "myNotification.db").allowMainThreadQueries().build()
        categoryDataBase = Room.databaseBuilder(this, MyDataBase::class.java, "myCategory.db").allowMainThreadQueries().build()
        budgetDataBase = Room.databaseBuilder(this, BudgetDataBase::class.java, "myBudget.db").allowMainThreadQueries().build()
        instance = this
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->

            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = getString(R.string.msg_token_fmt)
            Log.d(TAG, msg)
            Log.d(TAG,token )
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()



        })


    }









    //存储key对应的数据
    fun saveData(context: Context, key: String, info: Int) {
        val sharedPreferences = context.getSharedPreferences(key, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, info.toString())
        editor.apply()

    }




    //取key对应的数据
    fun getData(context: Context, key: String): String? {
        val result = context.getSharedPreferences(key, MODE_PRIVATE).getString(key, "")
        return result?.ifEmpty {
            ""
        }
    }

    companion object {
        private var instance: MainApplication? = null
        fun instance() = instance!!
        private const val TAG = "【Firebaseテスト中】"
    }
}