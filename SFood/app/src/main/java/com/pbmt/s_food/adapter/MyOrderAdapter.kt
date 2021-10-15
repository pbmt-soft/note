package com.pbmt.s_food.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pbmt.s_food.R
import com.pbmt.s_food.common.Common
import com.pbmt.s_food.model.Order
import java.text.SimpleDateFormat
import java.util.*

class MyOrderAdapter (internal  var context: Context,
                      internal var orderList:List<Order>)
    : RecyclerView.Adapter<MyOrderAdapter.MyViewHolder>()  {

    internal var calendar:Calendar
    internal var simpleDateFormat:SimpleDateFormat

    init {
        calendar= Calendar.getInstance()
        simpleDateFormat= SimpleDateFormat("dd-MM-yyyy HH:mm:ss")

    }

   inner  class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
      internal var txt_order_date: TextView?=null
       internal var txt_order_status: TextView?=null
       internal var txt_order_number: TextView?=null
       internal var txt_order_comment: TextView?=null
      internal  var order_img: ImageView?=null

       init {
           order_img = itemView.findViewById(R.id.order_img) as ImageView
           txt_order_date=itemView.findViewById(R.id.txt_order_date) as TextView
           txt_order_status=itemView.findViewById(R.id.txt_order_status) as TextView
           txt_order_number=itemView.findViewById(R.id.txt_order_number) as TextView
           txt_order_comment=itemView.findViewById(R.id.txt_order_comment) as TextView


       }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_order_item,parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(orderList[position].cartItemList!![0].foodImage).into(holder.order_img!!)
        calendar.timeInMillis=orderList[position].createDate
        val date=Date(orderList[position].createDate)
        holder.txt_order_date!!.text=StringBuilder(Common.getDateOfWeek(calendar.get(Calendar.DAY_OF_WEEK)))
                .append(" ")
                .append(simpleDateFormat.format(date))
        holder.txt_order_number!!.text=StringBuilder("Order Number: ").append(orderList[position].orderNumber)
        holder.txt_order_comment!!.text=StringBuilder("Comment: ").append(orderList[position].comment)
        holder.txt_order_status!!.text=StringBuilder("Status: ").append(Common.convertStatusToText(orderList[position].orderStatus))
    }

    override fun getItemCount(): Int {
        return  orderList.size
    }
}