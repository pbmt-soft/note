package com.pbmt.s_food.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.pbmt.s_food.R
import com.pbmt.s_food.database.CartDataSource
import com.pbmt.s_food.database.CartDatabase
import com.pbmt.s_food.database.CartItem
import com.pbmt.s_food.database.LocalCartDataSource
import com.pbmt.s_food.eventbus.UpdateItemCart
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus


class MyCartAdapter(internal  var context: Context,
                    internal var cartList:List<CartItem>)
    : RecyclerView.Adapter<MyCartAdapter.MyViewHolder>() {

    internal var compositeDisposable:CompositeDisposable
    internal var cartDataSource:CartDataSource

    init {
        compositeDisposable= CompositeDisposable()
        cartDataSource= LocalCartDataSource(CartDatabase.getInstance(context).cartDAO())
    }

  inner  class MyViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){

       var cart_img: ImageView?=null
       var txt_food_name: TextView
       var txt_food_price: TextView
       var btn_number: ElegantNumberButton

      init {
          cart_img=itemView.findViewById(R.id.cart_img) as ImageView
          txt_food_name=itemView.findViewById(R.id.txt_food_name) as TextView
          txt_food_price=itemView.findViewById(R.id.txt_food_price) as TextView
          btn_number=itemView.findViewById(R.id.number_btn) as ElegantNumberButton
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_cart_item,parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        Glide.with(context).load(cartList[position].foodImage).into(holder.cart_img!!)
        holder.txt_food_name.text=StringBuilder(cartList[position].foodName!!).toString()
        holder.txt_food_price.text=StringBuilder("").append(cartList[position].foodPrice + cartList[position].foodExtraPrice)
        holder.btn_number.number=cartList[position].foodQuantity.toString()

        holder.btn_number.setOnValueChangeListener { view, oldValue, newValue ->
            cartList[position].foodQuantity=newValue
            EventBus.getDefault().postSticky(UpdateItemCart(cartList[position]))
        }
    }

    fun  getItemAtPosition(pos:Int):CartItem{
        return cartList[pos]
    }

    override fun getItemCount(): Int {
        return cartList.size
    }
}