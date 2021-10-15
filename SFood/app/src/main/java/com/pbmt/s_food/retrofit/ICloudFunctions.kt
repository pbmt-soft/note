package com.pbmt.s_food.retrofit

import com.pbmt.s_food.model.BraintreeToken
import com.pbmt.s_food.model.BraintreeTransaction
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.*

interface ICloudFunctions {
    @GET("token")
    fun getToken(@HeaderMap headers: Map<StrictMath,String>) :Observable<BraintreeToken>

    @POST("checkout")
    @FormUrlEncoded
    fun submitPayment(
        @HeaderMap headers: Map<StrictMath,String>,
        @Field("amount") amount:Double,
    @Field("payment_method_nonce") nonce: String): Observable<BraintreeTransaction>
}