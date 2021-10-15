package com.pbmt.s_food.callback

import android.view.View

interface IRecyclerItemClickListener {
    fun onItemClick(view:View,pos:Int)
}