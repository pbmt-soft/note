package com.pbmt.s_food.ui.comment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pbmt.s_food.R
import com.pbmt.s_food.adapter.MyCommentAdapter
import com.pbmt.s_food.callback.ICommentCallBack
import com.pbmt.s_food.common.Common
import com.pbmt.s_food.model.CommentModel
import dmax.dialog.SpotsDialog


class CommentFragment : BottomSheetDialogFragment(), ICommentCallBack {

    private var viewModel: CommentViewModel? =null

    private var recycler_comment:RecyclerView? =null
    private  var dialog: AlertDialog? =null

    private var listener:ICommentCallBack

     init {
         listener=this

     }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val itemView=inflater.inflate(R.layout.bottom_sheet_comment_fragment, container, false)
        initViews(itemView)

        loadCommentFromFirebase()
        viewModel!!.mutableLiveDataCommentList.observe(viewLifecycleOwner,Observer{commentList->
            val adapter= MyCommentAdapter(requireContext(),commentList)
            recycler_comment!!.adapter=adapter

        })

        return itemView
    }

    private fun loadCommentFromFirebase() {
        dialog!!.show()

        val commentModels=ArrayList<CommentModel>()
        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
            .child(Common.foodSelected!!.id!!)
            .orderByChild("commentTimeStamp")
            .limitToLast(100)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (commentSnapshot in snapshot.children){
                        val commentModel=commentSnapshot.getValue<CommentModel>(CommentModel::class.java)
                        commentModels.add(commentModel!!)
                    }
                    listener.onCommentLoadSuccess(commentModels)
                }

                override fun onCancelled(error: DatabaseError) {
                   listener.onCommentLoadFailed(error.message)
                }

            })
    }

    private fun initViews(itemView: View?) {
        viewModel= ViewModelProvider(this).get(CommentViewModel::class.java)
        dialog= SpotsDialog.Builder().setContext(context).setCancelable(false).build()
        recycler_comment=itemView!!.findViewById(R.id.recycler_comment)
        recycler_comment!!.setHasFixedSize(true)
        val layoutManager=LinearLayoutManager(context,RecyclerView.VERTICAL,true)
        recycler_comment!!.layoutManager=layoutManager
        recycler_comment!!.addItemDecoration(DividerItemDecoration(context,layoutManager.orientation))
    }

    override fun onCommentLoadSuccess(commentModelList: List<CommentModel>) {
        dialog!!.dismiss()
        viewModel!!.setCommentList(commentModelList)
    }

    override fun onCommentLoadFailed(message: String) {
        Toast.makeText(context,""+message,Toast.LENGTH_SHORT).show()
        dialog!!.dismiss()
    }

    companion object {
        private  var instance: CommentFragment? =null

        fun getInstance():CommentFragment{
            if (instance == null)
                instance= CommentFragment()
            return instance!!
        }
    }

}