package com.pbmt.s_food.ui.view_orders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pbmt.s_food.model.Order

class ViewOrdersViewModel : ViewModel() {
    val mutableLiveDataOrderList:MutableLiveData<List<Order>>
    init {
        mutableLiveDataOrderList=MutableLiveData()
    }
    fun setMutableLiveDataOrderList(orderList: List<Order>){
        mutableLiveDataOrderList.value=orderList
    }
}