package com.pbmt.s_food.callback

import com.pbmt.s_food.model.BestDealModel
import com.pbmt.s_food.model.PopularCategoryModel

interface IBestDealLoadCallback {
    fun onBestDealLoadSuccess(bestDealList:List<BestDealModel>)
    fun onBestDealLoadFailed(message:String)
}