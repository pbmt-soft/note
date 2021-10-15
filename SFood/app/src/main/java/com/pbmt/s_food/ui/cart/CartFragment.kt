package com.pbmt.s_food.ui.cart

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.google.android.gms.location.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pbmt.s_food.R
import com.pbmt.s_food.adapter.MyCartAdapter
import com.pbmt.s_food.callback.ILoadTimeFromFirebaseCallback
import com.pbmt.s_food.callback.IMyButtonCallBack
import com.pbmt.s_food.common.Common
import com.pbmt.s_food.common.MySwipeHelper
import com.pbmt.s_food.database.CartDataSource
import com.pbmt.s_food.database.CartDatabase
import com.pbmt.s_food.database.CartItem
import com.pbmt.s_food.database.LocalCartDataSource
import com.pbmt.s_food.eventbus.CountCartEvent
import com.pbmt.s_food.eventbus.HideFABCart
import com.pbmt.s_food.eventbus.UpdateItemCart
import com.pbmt.s_food.model.Order
import com.pbmt.s_food.retrofit.ICloudFunctions
import com.pbmt.s_food.retrofit.RetrofitCloudClient
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.cart_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.create
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CartFragment : Fragment(), ILoadTimeFromFirebaseCallback {

    companion object {
        fun newInstance() = CartFragment()
    }

    private val REQUEST_BRAINTREE_CODE: Int=8888
    private var cartDataSource:CartDataSource?=null
    private var compositeDisposable:CompositeDisposable = CompositeDisposable()
    private var recyclerViewState:Parcelable?=null
    private lateinit var btn_place_order:Button
    private lateinit var  locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private  lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    internal  var address:String=""
    internal  var comment:String=""

    lateinit var cloudsFunctions: ICloudFunctions

    var txt_empty_cart:TextView?=null
    var txt_total_price:TextView?=null
    var group_place_order:CardView?=null
    var recycler_cart:RecyclerView?=null
    var adapter : MyCartAdapter?=null

    lateinit var listener:ILoadTimeFromFirebaseCallback

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        calculateTotalPrice()
        if (fusedLocationProviderClient != null){
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,
                Looper.getMainLooper())
        }
    }

    private lateinit var viewModel: CartViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        EventBus.getDefault().postSticky(HideFABCart(true))

        viewModel = ViewModelProvider(this).get(CartViewModel::class.java)

        viewModel.initCartDataSource(requireContext())
        val root=inflater.inflate(R.layout.cart_fragment, container, false)
        initViews(root)
        initLocation()
        viewModel.getCartMutableListaData().observe(viewLifecycleOwner, Observer {
            if (it == null || it.isEmpty()){
                recycler_cart!!.visibility=View.GONE
                group_place_order!!.visibility=View.GONE
                txt_empty_cart!!.visibility=View.VISIBLE
            }else{
                recycler_cart!!.visibility=View.VISIBLE
                group_place_order!!.visibility=View.VISIBLE
                txt_empty_cart!!.visibility=View.GONE

                adapter=MyCartAdapter(requireContext(),it)
                recycler_cart!!.adapter=adapter
            }
        })
        return root
    }

    @SuppressLint("MissingPermission")
    private fun initLocation() {
        buildLocationRequest()
        buildLocationCallback()
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(context)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
    }

    private fun buildLocationCallback() {
        locationCallback=object :LocationCallback(){
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                currentLocation=p0.lastLocation
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest= LocationRequest()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(5000)
        locationRequest.setFastestInterval(3000)
        locationRequest.setSmallestDisplacement(10f)

    }

    @SuppressLint("MissingPermission")
    private fun initViews(root: View?) {

        setHasOptionsMenu(true)

        cloudsFunctions=RetrofitCloudClient.getInstance().create(ICloudFunctions::class.java)

        listener=this

        cartDataSource= LocalCartDataSource(CartDatabase.getInstance(requireContext()).cartDAO())

        recycler_cart = root!!.findViewById(R.id.recycler_cart)as RecyclerView
        recycler_cart!!.setHasFixedSize(true)
        val layoutManager=LinearLayoutManager(context)
        recycler_cart!!.layoutManager=layoutManager
        recycler_cart!!.addItemDecoration(DividerItemDecoration(context,layoutManager.orientation))

        val swipe= object :MySwipeHelper(requireContext(), recycler_cart!!,200){
            override fun instantiateMyButton(viewHolder: RecyclerView.ViewHolder, buffer: MutableList<MyButton>) {
                buffer.add(MyButton(context!!,
                        "Delete",
                30,
                0,
                Color.parseColor("#FF3C30"),
                object:IMyButtonCallBack{
                    override fun onMyButtonClick(pos: Int) {
                        val deleteItem = adapter!!.getItemAtPosition(pos)
                        cartDataSource!!.deleteCart(deleteItem)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object: SingleObserver<Int>{
                                    override fun onSubscribe(d: Disposable) {

                                    }

                                    override fun onSuccess(t: Int) {
                                        adapter!!.notifyItemRemoved(pos)
                                        sumCart()
                                        EventBus.getDefault().postSticky(CountCartEvent(true))
                                        Toast.makeText(context,"Delete Item Success",Toast.LENGTH_SHORT).show()
                                    }

                                    override fun onError(e: Throwable) {
                                        Toast.makeText(context,"[DELETE ITEM]"+e.message,Toast.LENGTH_SHORT).show()
                                    }

                                })

                    }

                }))
            }

        }

        txt_empty_cart=root.findViewById(R.id.txt_empty_cart) as TextView
        txt_total_price=root.findViewById(R.id.txt_total_price) as TextView
        group_place_order=root.findViewById(R.id.group_place_order) as CardView

        btn_place_order=root.findViewById(R.id.btn_place_order) as Button

        btn_place_order.setOnClickListener {
            val builder=AlertDialog.Builder(requireContext())
            builder.setTitle("One more step!")

            val view=LayoutInflater.from(context).inflate(R.layout.layout_place_order,null)

            val edt_address=view.findViewById<View>(R.id.edt_address) as EditText
            val edt_comment=view.findViewById<View>(R.id.edt_comment) as EditText
            val txt_address_detail=view.findViewById<View>(R.id.txt_address_detail) as TextView
            val rdi_home=view.findViewById<View>(R.id.rdi_home_address) as RadioButton
            val rdi_other_address=view.findViewById<View>(R.id.rdi_other_address) as RadioButton
            val rdi_ship_to_this_address=view.findViewById<View>(R.id.rdi_ship_this_address) as RadioButton
            val rdi_cod=view.findViewById<View>(R.id.rdi_cod) as RadioButton
            val rdi_braintree=view.findViewById<View>(R.id.rdi_braintree) as RadioButton

            edt_address.setText(Common.currentUser!!.address)
            rdi_home.setOnCheckedChangeListener { compoundButton, b ->
                if (b){
                    edt_address.setText(Common.currentUser!!.address)
                    txt_address_detail.visibility=View.GONE
                }
            }
            rdi_other_address.setOnCheckedChangeListener { compoundButton, b ->
                if (b){
                    edt_address.setText("")
                    edt_address.setHint("Enter your Address")
                    txt_address_detail.visibility=View.GONE
                }
            }
            rdi_ship_to_this_address.setOnCheckedChangeListener { compoundButton, b ->
                if (b){
                    fusedLocationProviderClient.lastLocation
                        .addOnFailureListener { e->
                            txt_address_detail.visibility=View.GONE
                            Toast.makeText(context,""+e.message,Toast.LENGTH_SHORT).show() }
                        .addOnCompleteListener { task ->
                            val coordinates=StringBuilder()
                                .append(task.result.latitude)
                                .append("/")
                                .append(task.result.longitude)
                                .toString()

                            val singleAddress = Single.just(getAddressFromLatLng(task.result.latitude,task.result.longitude))
                            val disposable= singleAddress.subscribeWith(object:DisposableSingleObserver<String>(){
                                override fun onSuccess(t: String) {
                                    edt_address.setText(coordinates)
                                    txt_address_detail.visibility=View.VISIBLE
                                    txt_address_detail.setText(t)
                                }

                                override fun onError(e: Throwable) {
                                    edt_address.setText(coordinates)
                                    txt_address_detail.visibility=View.VISIBLE
                                    txt_address_detail.setText(e.message)
                                }

                            })



                        }
                }
            }

            builder.setView(view)
            builder.setNegativeButton("NO",{dialogInterface,_ ->
                dialogInterface.dismiss()
            }).setPositiveButton("YES",{dialogInterface,_ ->
               if (rdi_cod.isChecked)
                   paymentCOD(edt_address.text.toString(),edt_comment.text.toString())
                else if(rdi_braintree.isChecked){
                    address=edt_address.text.toString()
                   comment=edt_comment.text.toString()
                   if(!TextUtils.isEmpty(Common.currentToken)){
                       val dropInRequest=DropInRequest().clientToken(Common.currentToken)
                       startActivityForResult(dropInRequest.getIntent(context),REQUEST_BRAINTREE_CODE)
                   }
                }
            })

            val dialog = builder.create()
            dialog.show()
        }

    }

    private fun paymentCOD(address: String, comment: String) {
        compositeDisposable.add(cartDataSource!!.getAllCart(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ cartItemList ->
                cartDataSource!!.sumPrice(Common.currentUser!!.uid!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object:SingleObserver<Double>{
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onSuccess(totalPrice: Double) {
                            val finalPrice=totalPrice
                            val order=Order()
                            order.userId=Common.currentUser!!.uid!!
                            order.userName=Common.currentUser!!.name!!
                            order.userPhone=Common.currentUser!!.phone!!
                            order.shippingAddress=address
                            order.comment=comment

                            if (currentLocation !=null){
                                order.lat=currentLocation.latitude
                                order.lng=currentLocation.longitude
                            }
                            order.cartItemList=cartItemList
                            order.totalPayment=totalPrice
                            order.finalPayment=finalPrice
                            order.discount=0
                            order.isCod=true
                            order.transactionId="Cash On Delivery"

                            syncLocalTimeWithServerTime(order)
                        }

                        override fun onError(e: Throwable) {
                            Toast.makeText(requireContext(),"[SUM PRICE]"+e.message,Toast.LENGTH_SHORT).show()
                        }

                    })

            }, {throwable -> Toast.makeText(requireContext(),""+throwable.message,Toast.LENGTH_SHORT).show()}))
    }

    private fun syncLocalTimeWithServerTime(order: Order) {
        var offsetRef=FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset")
        offsetRef.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val offset=snapshot.getValue(Long::class.java)
                val estimatedServerTimeInMs= System.currentTimeMillis()+offset!!
                val sdt=SimpleDateFormat("MM dd yyyy, HH:mm")
                val date= Date(estimatedServerTimeInMs)
                Log.d("PBMT-DEV",""+sdt.format(date))
                listener.onLoadTimeSuccess(order,estimatedServerTimeInMs)
            }

            override fun onCancelled(error: DatabaseError) {
                listener.onLoadTimeFailed(error.message)  }

        })
    }

    private fun writeOrderToFirebase(order: Order) {
        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
            .child(Common.createOrderNumber())
            .setValue(order)
            .addOnFailureListener { e->Toast.makeText(requireContext(),""+e.message,Toast.LENGTH_SHORT).show() }
            .addOnCompleteListener { task->
                if (task.isSuccessful) {
                    cartDataSource!!.cleanCart(Common.currentUser!!.uid!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object:SingleObserver<Int>{
                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onSuccess(t: Int) {
                                Toast.makeText(requireContext(),"Order placed successfully",Toast.LENGTH_SHORT).show()
                            }

                            override fun onError(e: Throwable) {
                                Toast.makeText(requireContext(),"[CLEAN CART]"+e.message,Toast.LENGTH_SHORT).show()
                            }

                        })
                }
            }
    }

    private fun getAddressFromLatLng(latitude: Double, longitude: Double): String {
        val geocoder=Geocoder(context, Locale.getDefault())
        var result:String?=null
        try {
            val addressList=geocoder.getFromLocation(latitude,longitude,1)
            if (addressList != null && addressList.size >0){
                val address=addressList[0]
                val sb= StringBuilder(address.getAddressLine(0))
                result =sb.toString()
            }else{
                result="Address not found!"
            }
            return  result!!
        }catch (e:IOException){
            return  e.message!!
        }
    }

    private fun sumCart() {
        cartDataSource!!.sumPrice(Common.currentUser!!.uid!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object :SingleObserver<Double>{
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(t: Double) {
                        txt_total_price!!.text=StringBuilder("Total: $")
                                .append(t)
                    }

                    override fun onError(e: Throwable) {
                        if (!e.message!!.contains("Query returned empty"))
                            Toast.makeText(context,"[SUM CART]"+e.message,Toast.LENGTH_SHORT).show()
                    }

                })
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
             EventBus.getDefault().register(this)
    }

    override fun onStop() {
        viewModel.onStop()
        compositeDisposable.clear()
        EventBus.getDefault().postSticky(HideFABCart(false))
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        if (fusedLocationProviderClient != null){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
        super.onStop()

    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onUpdateItemInCart(event:UpdateItemCart){
        if (event.cartItem != null){
            recyclerViewState=recycler_cart!!.layoutManager!!.onSaveInstanceState()
            cartDataSource!!.updateCart(event.cartItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object:SingleObserver<Int>{
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(t: Int) {
                        calculateTotalPrice()
                        recycler_cart!!.layoutManager!!.onRestoreInstanceState(recyclerViewState)
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(requireContext(),"[UPDATE CART]"+e.message,Toast.LENGTH_SHORT).show()
                    }

                })
        }
    }

    private fun calculateTotalPrice() {
        cartDataSource!!.sumPrice(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object :SingleObserver<Double>{
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(price: Double) {
                    txt_total_price!!.text=StringBuilder("Total: $").append(Common.formatPrice(price))
                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty"))
                        Toast.makeText(requireContext(),"[SUM CART]"+e.message,Toast.LENGTH_SHORT).show()
                }

            })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_settings).setVisible(false)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.cart_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item!!.itemId == R.id.action_clear_cart){
            cartDataSource!!.cleanCart(Common.currentUser!!.uid!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object :SingleObserver<Int>{
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onSuccess(t: Int) {
                            Toast.makeText(context!!,"Clear Cart Success",Toast.LENGTH_SHORT).show()
                            EventBus.getDefault().postSticky(CountCartEvent(true))
                        }

                        override fun onError(e: Throwable) {
                           Toast.makeText(context!!,"[CLEAR CART]"+e.message,Toast.LENGTH_SHORT).show()
                        }

                    })
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onLoadTimeSuccess(order: Order, estimatedTimeMs: Long) {
        order.createDate=(estimatedTimeMs)
        order.orderStatus=0
        writeOrderToFirebase(order)
    }

    override fun onLoadTimeFailed(message: String) {
        Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==REQUEST_BRAINTREE_CODE){
            if(resultCode==RESULT_OK){
                val result=data!!.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                val nonce=result!!.paymentMethodNonce

                cartDataSource!!.sumPrice(Common.currentUser!!.uid!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object:SingleObserver<Double>{
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onSuccess(totalPrice: Double) {
                            compositeDisposable.add(
                                cartDataSource!!.getAllCart(Common.currentUser!!.uid!!)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({cartItems:List<CartItem>? ->
                                        compositeDisposable.add(cloudsFunctions.submitPayment(totalPrice,nonce!!.nonce)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({braintreeTransaction->
                                                if(braintreeTransaction.success){
                                                    val finalPrice=totalPrice
                                                    val order=Order()
                                                    order.userId=Common.currentUser!!.uid!!
                                                    order.userName=Common.currentUser!!.name!!
                                                    order.userPhone=Common.currentUser!!.phone!!
                                                    order.shippingAddress=address
                                                    order.comment=comment

                                                    if (currentLocation !=null){
                                                        order.lat=currentLocation.latitude
                                                        order.lng=currentLocation.longitude
                                                    }
                                                    order.cartItemList=cartItems
                                                    order.totalPayment=totalPrice
                                                    order.finalPayment=finalPrice
                                                    order.discount=0
                                                    order.isCod=false
                                                    order.transactionId=braintreeTransaction.transaction!!.id

                                                    syncLocalTimeWithServerTime(order)

                                                }
                                            },{t:Throwable ->
                                                Toast.makeText(context,""+t.message,Toast.LENGTH_SHORT).show()
                                            }))


                                    },{t:Throwable ->
                                        Toast.makeText(context,""+t.message,Toast.LENGTH_SHORT).show()
                                    })
                            )
                        }

                        override fun onError(e: Throwable) {
                            Toast.makeText(context,""+e.message,Toast.LENGTH_SHORT).show()
                        }

                    })


            }
        }
    }
}

