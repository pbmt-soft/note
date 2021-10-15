package com.pbmt.s_food.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pbmt.s_food.R
import com.pbmt.s_food.callback.IRecyclerItemClickListener
import com.pbmt.s_food.common.Common
import com.pbmt.s_food.database.CartDataSource
import com.pbmt.s_food.database.CartDatabase
import com.pbmt.s_food.database.CartItem
import com.pbmt.s_food.database.LocalCartDataSource
import com.pbmt.s_food.eventbus.CategoryClick
import com.pbmt.s_food.eventbus.CountCartEvent
import com.pbmt.s_food.eventbus.FoodItemClick
import com.pbmt.s_food.model.FoodModel
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.operators.single.SingleObserveOn
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus


class MyFoodListAdapter  (internal  var context: Context,
                          internal var foodList:List<FoodModel>)
    : RecyclerView.Adapter<MyFoodListAdapter.MyViewHolder>() {

    private  val compositeDisposable:CompositeDisposable
    private val cartDataSource:CartDataSource

    init {
        compositeDisposable= CompositeDisposable()
        cartDataSource=LocalCartDataSource(CartDatabase.getInstance(context).cartDAO())
    }


    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var txt_food_name: TextView?=null
        var txt_food_price: TextView?=null
        var food_img: ImageView?=null
        var fav_img: ImageView?=null
        var cart_img: ImageView?=null

        internal var listener: IRecyclerItemClickListener?=null

        fun setListener(listener: IRecyclerItemClickListener){
            this.listener=listener
        }

        init {
            txt_food_name=itemView.findViewById(R.id.txt_food_name)
            txt_food_price=itemView.findViewById(R.id.txt_food_price)
            food_img=itemView.findViewById(R.id.img_food)
            fav_img=itemView.findViewById(R.id.img_fav)
            cart_img=itemView.findViewById(R.id.img_cart)
            itemView.setOnClickListener(this)

        }

        override fun onClick(view: View?) {
            listener!!.onItemClick(view!!,adapterPosition)
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_food_item,parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(foodList[position].image).into(holder.food_img!!)
        holder.txt_food_name!!.text = foodList[position].name
        holder.txt_food_price!!.text = foodList[position].price.toString()

        //event
        holder.setListener(object :IRecyclerItemClickListener{
            override fun onItemClick(view: View, pos: Int) {
                Common.foodSelected= foodList[pos]
                Common.foodSelected!!.key =pos.toString()
                EventBus.getDefault().postSticky(FoodItemClick(true, foodList[pos]))
            }

        })

        holder.cart_img!!.setOnClickListener {
            val cartItem=CartItem()
            cartItem.uid=Common.currentUser!!.uid
            cartItem.userPhone=Common.currentUser!!.phone

            cartItem.foodId=foodList[position].id!!
            cartItem.foodName=foodList[position].name!!
            cartItem.foodImage=foodList[position].image!!
            cartItem.foodPrice=foodList[position].price!!.toDouble()
            cartItem.foodQuantity=1
            cartItem.foodExtraPrice=0.0
            cartItem.foodAddon="Default"
            cartItem.foodSize="Default"

            cartDataSource.getItemWithAllOptionsInCart(Common.currentUser!!.uid!!,cartItem.foodId,cartItem.foodSize!!,cartItem.foodAddon!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SingleObserver<CartItem>{
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onSuccess(cartItemFromDB: CartItem) {
                            if (cartItemFromDB.equals(cartItem)){
                                cartItemFromDB.foodExtraPrice= cartItem.foodExtraPrice
                                cartItemFromDB.foodAddon= cartItem.foodAddon
                                cartItemFromDB.foodSize= cartItem.foodSize
                                cartItemFromDB.foodQuantity= cartItemFromDB.foodQuantity + cartItem.foodQuantity

                                cartDataSource.updateCart(cartItemFromDB)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(object: SingleObserver<Int>{
                                            override fun onSubscribe(d: Disposable) {

                                            }

                                            override fun onSuccess(t: Int) {
                                               Toast.makeText(context,"Update Cart Success",Toast.LENGTH_SHORT).show()
                                                EventBus.getDefault().postSticky(CountCartEvent(true))
                                            }

                                            override fun onError(e: Throwable) {
                                                Toast.makeText(context,"[UPDATE CART]"+e.message,Toast.LENGTH_SHORT).show()
                                            }

                                        })

                            }else{
                                compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({
                                            Toast.makeText(context,"Add to cart success",Toast.LENGTH_SHORT).show()
                                            EventBus.getDefault().postSticky(CountCartEvent(true))
                                        },{
                                            t:Throwable? ->Toast.makeText(context,"[INSERT CART]"+t!!.message,Toast.LENGTH_SHORT).show()
                                        }))
                            }
                        }

                        override fun onError(e: Throwable) {
                           if (e.message!!.contains("empty")){
                               compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                       .subscribeOn(Schedulers.io())
                                       .observeOn(AndroidSchedulers.mainThread())
                                       .subscribe({
                                           Toast.makeText(context,"Add to cart success",Toast.LENGTH_SHORT).show()
                                           EventBus.getDefault().postSticky(CountCartEvent(true))
                                       },{
                                           t:Throwable? ->Toast.makeText(context,"[INSERT CART]"+t!!.message,Toast.LENGTH_SHORT).show()
                                       }))
                           }else{
                               Toast.makeText(context,"[CART ERROR]"+e.message,Toast.LENGTH_SHORT).show()
                           }
                        }

                    })
        }

    }

    fun onStop(){
        if (compositeDisposable != null)
            compositeDisposable.clear()
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

}