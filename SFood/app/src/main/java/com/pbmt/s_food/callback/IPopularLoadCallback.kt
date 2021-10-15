package com.pbmt.s_food.callback

import com.pbmt.s_food.model.PopularCategoryModel

 interface IPopularLoadCallback {
     fun onPopularLoadSuccess(popularModelList:List<PopularCategoryModel>)
     fun onPopularLoadFailed(message:String)
}