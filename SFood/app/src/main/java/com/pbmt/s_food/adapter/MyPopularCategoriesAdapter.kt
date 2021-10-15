package com.pbmt.s_food.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pbmt.s_food.R
import com.pbmt.s_food.callback.IRecyclerItemClickListener
import com.pbmt.s_food.common.Common
import com.pbmt.s_food.eventbus.CategoryClick
import com.pbmt.s_food.eventbus.PopularFoodItemClick
import com.pbmt.s_food.model.PopularCategoryModel
import de.hdodenhof.circleimageview.CircleImageView
import org.greenrobot.eventbus.EventBus

class MyPopularCategoriesAdapter(internal  var context: Context,
                                 internal var popularCategoryModels:List<PopularCategoryModel>)
    :RecyclerView.Adapter<MyPopularCategoriesAdapter.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_popular_categories_item,parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(popularCategoryModels.get(position).image).into(holder.category_image!!)
        holder.txt_category_name!!.setText(popularCategoryModels.get(position).name)

        //event
        holder.setListener(object :IRecyclerItemClickListener{
            override fun onItemClick(view: View, pos: Int) {
                //Common.categorySelected= categoriesList[pos]
                EventBus.getDefault().postSticky(PopularFoodItemClick(popularCategoryModels[pos]))
            }

        })
    }

    override fun getItemCount(): Int {
       return popularCategoryModels.size
    }

    inner class MyViewHolder(itemView:View):RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var txt_category_name:TextView?=null

        var category_image:CircleImageView?=null


        internal var listener: IRecyclerItemClickListener?=null

        fun setListener(listener: IRecyclerItemClickListener){
            this.listener=listener
        }

        init {
           txt_category_name=itemView.findViewById(R.id.txt_category_name)
            category_image=itemView.findViewById(R.id.category_image)
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            listener!!.onItemClick(view!!,adapterPosition)
        }
    }

}