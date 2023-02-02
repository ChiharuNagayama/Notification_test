package com.android.example.notification.data

/**
 * 横棒グラフの描画用
 * @property data BudgetGraphData
 * @constructor
 */
//val category: String,：カテゴリー
//    val budget:Float,：利用金額
//    val budgetTotal: Float,：予算額

data class BudgetValueBean(val data: BudgetGraphData)
data class BudgetGraphData(
    val category: String,
    val budget:Float,
    val budgetTotal: Float,

)