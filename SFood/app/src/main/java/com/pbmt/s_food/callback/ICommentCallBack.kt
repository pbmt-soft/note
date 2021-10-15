package com.pbmt.s_food.callback


import com.pbmt.s_food.model.CommentModel

interface ICommentCallBack {
    fun onCommentLoadSuccess(commentModelList:List<CommentModel>)
    fun onCommentLoadFailed(message:String)
}
