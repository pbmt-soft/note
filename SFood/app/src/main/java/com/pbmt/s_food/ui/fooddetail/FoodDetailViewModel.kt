package com.pbmt.s_food.ui.fooddetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pbmt.s_food.common.Common
import com.pbmt.s_food.model.CommentModel
import com.pbmt.s_food.model.FoodModel

class FoodDetailViewModel : ViewModel() {

    private var mutableLiveDataFood:MutableLiveData<FoodModel>?=null
    private var mutableLiveDataComment:MutableLiveData<CommentModel>?=null

    init {
        mutableLiveDataComment= MutableLiveData()
    }

    fun getFoodMutableListaData(): MutableLiveData<FoodModel> {
        if (mutableLiveDataFood == null) {
            mutableLiveDataFood = MutableLiveData()
        }
        mutableLiveDataFood!!.value = Common.foodSelected!!
        return mutableLiveDataFood!!
    }

    fun getCommentMutableListaData(): MutableLiveData<CommentModel> {
        if (mutableLiveDataComment == null) {
            mutableLiveDataComment = MutableLiveData()
        }
        return mutableLiveDataComment!!
    }

    fun setCommentModel(commentModel: CommentModel) {
        if (mutableLiveDataComment !=null){
            mutableLiveDataComment!!.value=(commentModel)
        }
    }

    fun setFoodModel(foodModel: FoodModel) {
        if (mutableLiveDataFood != null){
            mutableLiveDataFood!!.value=(foodModel)
        }


    }


}