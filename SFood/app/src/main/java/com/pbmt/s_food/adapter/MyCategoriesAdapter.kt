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
import com.pbmt.s_food.callback.IRecyclerItemClickListener
import com.pbmt.s_food.common.Common
import com.pbmt.s_food.eventbus.CategoryClick
import com.pbmt.s_food.model.CategoryModel
import org.greenrobot.eventbus.EventBus


class MyCategoriesAdapter (internal  var context: Context,
                           internal var categoriesList:List<CategoryModel>)
    : RecyclerView.Adapter<MyCategoriesAdapter.MyViewHolder>() {


    inner class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        var txt_category: TextView?=null
        var category_img: ImageView?=null


        internal var listener:IRecyclerItemClickListener?=null

        fun setListener(listener:IRecyclerItemClickListener){
            this.listener=listener
        }
        init {
            txt_category=itemView.findViewById(R.id.txt_category)
            category_img=itemView.findViewById(R.id.category_img)
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            listener!!.onItemClick(view!!,adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_category_item,parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(categoriesList[position].image).into(holder.category_img!!)
        holder.txt_category!!.text = categoriesList[position].name

        //event
        holder.setListener(object :IRecyclerItemClickListener{
            override fun onItemClick(view: View, pos: Int) {
                Common.categorySelected= categoriesList[pos]
                EventBus.getDefault().postSticky(CategoryClick(true,categoriesList.get(pos)))
            }

        })

    }

    override fun getItemCount(): Int {
       return categoriesList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (categoriesList.size ==1){
            Common.DEFAULT_COLUMN_COUNT
        }else{
            if(categoriesList.size%2 ==0) {
                Common.DEFAULT_COLUMN_COUNT
            } else
                if (position > 1 && position == categoriesList.size-1)
                    Common.FULL_WIDTH_COLUMN
                else
                    Common.DEFAULT_COLUMN_COUNT


        }

    }
}