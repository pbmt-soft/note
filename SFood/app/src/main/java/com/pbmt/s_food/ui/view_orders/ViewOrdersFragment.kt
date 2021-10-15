package com.pbmt.s_food.ui.view_orders

import android.app.AlertDialog
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pbmt.s_food.R
import com.pbmt.s_food.adapter.MyOrderAdapter
import com.pbmt.s_food.callback.ILoadOrderCallback
import com.pbmt.s_food.common.Common
import com.pbmt.s_food.model.Order
import com.pbmt.s_food.ui.foodlist.FoodListViewModel
import dmax.dialog.SpotsDialog
import java.util.*
import kotlin.collections.ArrayList

class ViewOrdersFragment : Fragment(),ILoadOrderCallback {


    private lateinit var viewModel: ViewOrdersViewModel
    internal lateinit var dialog: AlertDialog
    internal lateinit var recycler_view_order: RecyclerView
    internal lateinit var listener:ILoadOrderCallback


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewModel =
                ViewModelProvider(this).get(ViewOrdersViewModel::class.java)
        val root = inflater.inflate(R.layout.view_orders_fragment, container, false)
            initView(root)
        loadOrderFromFirebase()

        viewModel.mutableLiveDataOrderList.observe(viewLifecycleOwner, Observer {
            Collections.reverse(it)
            val adapter= MyOrderAdapter(requireContext(),it)
            recycler_view_order.adapter=adapter

        })
        return root
    }

    private fun loadOrderFromFirebase() {
        dialog.show()

        val orderList=ArrayList<Order>()
        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .orderByChild("userId")
                .equalTo(Common.currentUser!!.uid!!)
                .limitToLast(100)
                .addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (orderSnapshot in snapshot.children){
                            val order=orderSnapshot.getValue(Order::class.java)
                            order!!.orderNumber =orderSnapshot.key
                            orderList.add(order)
                        }
                        listener.onLoadOrderSuccess(orderList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        listener.onLoadOrderFailed(error.message)
                    }

                })
    }

    private fun initView(root: View?) {
        listener=this
        dialog= SpotsDialog.Builder().setContext(context).setCancelable(false).build()

        recycler_view_order=root!!.findViewById(R.id.recycler_view_order) as RecyclerView
        recycler_view_order.setHasFixedSize(true)
        val layoutManager=LinearLayoutManager(requireContext())
        recycler_view_order.layoutManager=layoutManager
        recycler_view_order.addItemDecoration(DividerItemDecoration(requireContext(),layoutManager.orientation))


    }


    override fun onLoadOrderSuccess(orderList: List<Order>) {
        dialog.dismiss()
        viewModel!!.setMutableLiveDataOrderList(orderList)
    }

    override fun onLoadOrderFailed(message: String) {
        dialog.dismiss()
        Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show()
    }

}