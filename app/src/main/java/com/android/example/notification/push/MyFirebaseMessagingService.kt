package com.android.example.notification.push


import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.android.example.notification.MainActivity
import com.android.example.notification.R
import com.android.example.notification.constant.MyConstant
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        var messageData:Map<String, String>? = null

        Log.d(TAG, "From: ${remoteMessage?.from}")

        // メッセージにデータ・ロードが含まれているかどうかを確認します。
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            //push通知のメッセージデータを取得
            messageData = remoteMessage.data
        }

        // メッセージに通知が含まれているかどうかをチェックします。
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            //Push通知はApp中に表示と遷移設定
            sendNotification(messageData,it.title.toString(),it.body.toString())
        }
    }

    /**
     * 通知を受け取って遷移先画面のデータ設定
     * @param context Context
     * @param messageData Map<String, String>?
     * @return PendingIntent?
     */
    private fun getPendingIntent(context: Context, messageData:Map<String, String>?): PendingIntent? {

//        通知から値の受け取り
        //目的画面のId
        val destId: Int = R.id.navigation_home
        /*
        支払通知のメッセージデータ（お金、時間、店舗名、カテゴリー）
        bundleに保存、遷移先画面にデータを取得用
         */
        val bundle = Bundle()
        bundle.putString("money",messageData?.get("money").toString())
        bundle.putString("date",messageData?.get("date").toString())
        bundle.putString("address",messageData?.get("address").toString())
        bundle.putString("category",messageData?.get("category").toString())
//目的画面へ遷移
        return NavDeepLinkBuilder(context)
            .setGraph(R.navigation.mobile_navigation)
            .addDestination(destId)
            .setArguments(bundle)
            .createPendingIntent()
    }




//    override fun onNewToken(token: String) {
//        Log.d(TAG, "Refreshed token: $token")
//
//        // If we want to send messages to this application instance or
//        // manage the app's subscription on the server-side, send the
//        // Instance ID token to your app server.
//        sendRegistrationToServer(token)
//    }
//    private fun sendRegistrationToServer(token: String?) {
//    }
private fun sendNotification(messageData:Map<String, String>?,messageTitle:String,messageBody:String) {
    //Appのpackageと表示したいレイアウトを伝え設定
    val views = RemoteViews(packageName, R.layout.layout_notification)
    views.setTextViewText(R.id.date,messageData?.get("date"))
    views.setTextViewText(R.id.shop_name_txt,messageData?.get("address"))
    views.setTextViewText(R.id.category_tx,messageData?.get("category"))
    views.setTextViewText(R.id.money_tx,messageData?.get("money")+"円")
    //取得遷移先のIntent
    val penIntent =getPendingIntent(this,messageData)
    //支払通知ChannelIdを設定
    val notificationBuilder = NotificationCompat.Builder(this, MyConstant.CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notifications_black_24dp)//通知表示アイコン
        .setContentTitle(messageTitle)//通知表示のタイトル
        .setContentText(messageBody)//Push通知のメッセージボディー
        .setCustomBigContentView(views)//レイアウトのビュー
        .setAutoCancel(true)
        .setContentIntent(penIntent)


    val notificationManager = getSystemService(NotificationManager::class.java)
    // Since android Oreo notification channel is needed.
    notificationManager.notify(MyConstant.CHANNEL_ID, 1, notificationBuilder.build())
}

    companion object {
        private const val TAG = "【Firebaseテスト中】"
    }
}