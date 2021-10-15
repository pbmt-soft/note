package com.pbmt.s_food.ui.home

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pbmt.s_food.callback.IBestDealLoadCallback
import com.pbmt.s_food.callback.IPopularLoadCallback
import com.pbmt.s_food.common.Common
import com.pbmt.s_food.model.BestDealModel
import com.pbmt.s_food.model.PopularCategoryModel

class HomeViewModel() : ViewModel(),IPopularLoadCallback,IBestDealLoadCallback {

    private  var popularListMutableLiveData:MutableLiveData<List<PopularCategoryModel>>?= null
    private  var bestDealListMutableLiveData:MutableLiveData<List<BestDealModel>>?= null
    private lateinit var messageError:MutableLiveData<String>
    private var  popularLoadCallbackListener:IPopularLoadCallback
    private  var bestDealLoadCallbackListener:IBestDealLoadCallback


    val bestDealList:LiveData<List<BestDealModel>>
        get(){
            if (bestDealListMutableLiveData ==null){
                bestDealListMutableLiveData= MutableLiveData()
                messageError= MutableLiveData()
                loadBestDealList()
            }
            return bestDealListMutableLiveData!!
        }

    private fun loadBestDealList() {
        val tempList=ArrayList<BestDealModel>()
        val bestDealRef=FirebaseDatabase.getInstance().getReference(Common.BEST_DEAL_REF)
        bestDealRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapshot in snapshot!!.children){
                    val model= itemSnapshot.getValue<BestDealModel>(BestDealModel::class.java)
                    tempList.add(model!!)
                }
                bestDealLoadCallbackListener.onBestDealLoadSuccess(tempList)
            }

            override fun onCancelled(error: DatabaseError) {
                bestDealLoadCallbackListener.onBestDealLoadFailed(error.message)
            }

        })
    }

    val popularList:LiveData<List<PopularCategoryModel>>
    get(){
        if (popularListMutableLiveData ==null){
            popularListMutableLiveData= MutableLiveData()
            messageError= MutableLiveData()
            loadPopularList()
        }
        return popularListMutableLiveData!!
    }

    private fun loadPopularList() {
        val tempList=ArrayList<PopularCategoryModel>()
        val popularRef=FirebaseDatabase.getInstance().getReference(Common.POPULAR_REF)
        popularRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapshot in snapshot!!.children){
                    val model= itemSnapshot.getValue<PopularCategoryModel>(PopularCategoryModel::class.java)
                    tempList.add(model!!)
                }
                popularLoadCallbackListener.onPopularLoadSuccess(tempList)
            }

            override fun onCancelled(error: DatabaseError) {
                popularLoadCallbackListener.onPopularLoadFailed(error.message)
            }

        })
    }

    init {
           popularLoadCallbackListener=this
           bestDealLoadCallbackListener=this
        }



    override fun onPopularLoadSuccess(popularModelList: List<PopularCategoryModel>) {
        popularListMutableLiveData!!.value=popularModelList
    }

    override fun onPopularLoadFailed(message: String) {
       messageError.value=message
    }

    override fun onBestDealLoadSuccess(bestDealList: List<BestDealModel>) {
        bestDealListMutableLiveData!!.value=bestDealList
    }

    override fun onBestDealLoadFailed(message: String) {
        messageError.value=message
    }

}