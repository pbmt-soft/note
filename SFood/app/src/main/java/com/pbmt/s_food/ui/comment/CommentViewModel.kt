package com.pbmt.s_food.ui.comment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pbmt.s_food.model.CommentModel

class CommentViewModel : ViewModel() {
    val mutableLiveDataCommentList:MutableLiveData<List<CommentModel>>

    init {
        mutableLiveDataCommentList=MutableLiveData()
    }
     fun setCommentList(commentList:List<CommentModel>){
         mutableLiveDataCommentList.value=commentList
     }
}