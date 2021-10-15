package com.pbmt.s_food.ui.foodlist

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pbmt.s_food.R
import com.pbmt.s_food.adapter.MyCategoriesAdapter
import com.pbmt.s_food.adapter.MyFoodListAdapter
import com.pbmt.s_food.common.Common
import com.pbmt.s_food.ui.menu.MenuViewModel
import dmax.dialog.SpotsDialog

class FoodListFragment : Fragment() {


    private lateinit var foodViewModel: FoodListViewModel
    private lateinit var dialog: AlertDialog
    private lateinit var layoutAnimationController: LayoutAnimationController
    private var adapter:MyFoodListAdapter?=null
    private var recycler_food_list: RecyclerView?=null

    override fun onStop() {
        if (adapter != null)
            adapter!!.onStop()
        super.onStop()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodViewModel =
            ViewModelProvider(this).get(FoodListViewModel::class.java)
        val root = inflater.inflate(R.layout.food_list_fragment, container, false)

        initViews(root)

        foodViewModel.getFoodMutableListaData().observe(viewLifecycleOwner, Observer {
            dialog.dismiss()
            adapter= MyFoodListAdapter(requireContext(),it)
            recycler_food_list!!.adapter=adapter
            recycler_food_list!!.layoutAnimation=layoutAnimationController
        })
        return root
    }

    private fun initViews(root: View?) {
        dialog= SpotsDialog.Builder().setContext(context).setCancelable(false).build()
        dialog.show()
        layoutAnimationController= AnimationUtils.loadLayoutAnimation(context,R.anim.layout_item_from_left)
        recycler_food_list=root!!.findViewById(R.id.recycler_food_list)
        recycler_food_list!!.setHasFixedSize(true)
        recycler_food_list!!.layoutManager=LinearLayoutManager(context)

        (activity as AppCompatActivity).supportActionBar!!.title=Common.categorySelected!!.name
    }


}