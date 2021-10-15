package com.pbmt.s_food.callback


import com.pbmt.s_food.model.Order

interface ILoadTimeFromFirebaseCallback {
    fun onLoadTimeSuccess(order:Order,estimatedTimeMs:Long)
    fun onLoadTimeFailed(message:String)
}