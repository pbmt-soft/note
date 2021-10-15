package com.pbmt.s_food.callback


import com.pbmt.s_food.model.Order

interface ILoadOrderCallback {
    fun onLoadOrderSuccess(OrderList:List<Order>)
    fun onLoadOrderFailed(message:String)

}