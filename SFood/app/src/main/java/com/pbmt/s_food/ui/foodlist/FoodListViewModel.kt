package com.pbmt.s_food.ui.foodlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pbmt.s_food.common.Common
import com.pbmt.s_food.model.FoodModel

class FoodListViewModel : ViewModel() {
    private var foodListMutableLiveData: MutableLiveData<List<FoodModel>>? = null

    fun getFoodMutableListaData(): MutableLiveData<List<FoodModel>> {
        if (foodListMutableLiveData == null) {
            foodListMutableLiveData = MutableLiveData()
        }
        foodListMutableLiveData!!.value = Common.categorySelected!!.foods
        return foodListMutableLiveData!!
    }
}