package com.pbmt.s_food.callback

import com.pbmt.s_food.model.CategoryModel
import com.pbmt.s_food.model.PopularCategoryModel

interface ICategoryLoadCallBack {
    fun onCategoryLoadSuccess(categoryModelList:List<CategoryModel>)
    fun onCategoryLoadFailed(message:String)
}