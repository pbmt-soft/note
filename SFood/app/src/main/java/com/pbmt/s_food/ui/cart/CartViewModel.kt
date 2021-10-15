package com.pbmt.s_food.ui.cart

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pbmt.s_food.common.Common
import com.pbmt.s_food.database.CartDataSource
import com.pbmt.s_food.database.CartDatabase
import com.pbmt.s_food.database.CartItem
import com.pbmt.s_food.database.LocalCartDataSource
import com.pbmt.s_food.model.FoodModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CartViewModel : ViewModel() {

    private val compositeDisposable:CompositeDisposable
    private var cartDataSource:CartDataSource?=null
    private var mutableLiveDataCartItem:MutableLiveData<List<CartItem>>?=null

    init {
        compositeDisposable= CompositeDisposable()
    }

    fun initCartDataSource(context: Context){
        cartDataSource= LocalCartDataSource(CartDatabase.getInstance(context).cartDAO())
    }
    fun getCartMutableListaData(): MutableLiveData<List<CartItem>> {
        if (mutableLiveDataCartItem == null) {
            mutableLiveDataCartItem = MutableLiveData()
        }
        getCartItems()
        return mutableLiveDataCartItem!!
    }

    private fun getCartItems(){
        compositeDisposable.addAll(cartDataSource!!.getAllCart(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({cartItems->
                mutableLiveDataCartItem!!.value=cartItems
            },{t:Throwable? -> mutableLiveDataCartItem!!.value=null}))
    }

    fun onStop(){
        compositeDisposable.clear()
    }
}