package com.android.example.notification

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class NotifyWork(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

        val smsintent = Intent(applicationContext, SoundManageService::class.java)
/**ここに処理を記載*/

        val datenow = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"))

        //通知用の日時取得
        val dtformat1 = DateTimeFormatter.ofPattern("ddHH")
        val fdate1 = dtformat1.format(datenow)
        val datenow1  = fdate1.toString()
        val datenow2 = datenow1.toIntOrNull()

        //場合分け（通知文）用の日時取得
        val dtformat2 = DateTimeFormatter.ofPattern("MM")
        val fdate2 = dtformat2.format(datenow).toString()

////今の月の金額をベタ打ちで場合分けする。
//        1月の場合
        var yosangaku = if ( fdate2 == "01"){
            "12000"
        } else if( fdate2 == "02"){
            "193000"
        }else if( fdate2 == "03"){
            "14000"
        }else if( fdate2 == "04"){
            "15000"
        }else if( fdate2 == "05"){
            "16000"
        }else if( fdate2 == "06"){
            "17000"
        }else if( fdate2 == "07"){
            "18000"
        }else if( fdate2 == "08"){
            "19000"
        }else if( fdate2 == "09"){
            "20000"
        }else if( fdate2 == "10"){
            "21000"
        }else if( fdate2 == "11"){
            "22000"
        }else if( fdate2 == "12"){
            "23000"
        }else {
            "error"
        }

        var riyougaku = if ( fdate2 == "12"){
            "12000"
        } else if( fdate2 == "11"){
            "13000"
        }else if( fdate2 == "10"){
            "14000"
        }else if( fdate2 == "09"){
            "15000"
        }else if( fdate2 == "08"){
            "16000"
        }else if( fdate2 == "07"){
            "17000"
        }else if( fdate2 == "06"){
            "18000"
        }else if( fdate2 == "05"){
            "19000"
        }else if( fdate2 == "04"){
            "20000"
        }else if( fdate2 == "03"){
            "21000"
        }else if( fdate2 == "02"){
            "2000"
        }else if( fdate2 == "01"){
            "23000"
        }else {
            "error"
        }

        var cn = yosangaku.toInt()
        var hn = riyougaku.toInt()

//                デバッグは条件変更のたびにアンスト＋通知ONすること
        var msg1 = if (cn < hn){
            "予算額を超過しているよ！"
        }else if( cn > hn)
        {
            "まだまだ余裕があるよ"
        }else
        {
            "節約！"
        }

        var msg2 = if (cn < hn){
            "もう少し節約したほうがいいかも、、、！"
        }else if(  cn > hn)
        {

            var jn = cn - hn
            var an = jn.toString()

            an+"円余裕があるよ.気になっていたバッグも買えちゃうかも♡"
        }else
        {
            "今月の利用額がいっぱいだよ！節約しよう！"
        }

//28日の１６時台であれば、通知を出す

        if(2516 != datenow2){
            smsintent.putExtra("TITLE",msg1)
            smsintent.putExtra("BODY",msg2)
            applicationContext?.startService(smsintent)

            // Indicate whether the work finished successfully with the Result
            return Result.success()

        }else{


            return Result.failure()


        }


        /**ここまで処理を記載する*/

    }
}

