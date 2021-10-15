package com.pbmt.s_food.common

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.core.text.set
import com.pbmt.s_food.model.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

object Common {
    fun formatPrice(price: Double): String {
        if (price !=0.toDouble()){
            val df=DecimalFormat("#,##0.00")
            df.roundingMode=RoundingMode.HALF_UP
            val finalPrice= StringBuilder(df.format(price)).toString()
            return  finalPrice.replace(".",",")
        }
        else
            return  "0,00"
    }

    fun calculateExtraPrice(userSelectedSize: SizeModel?, userSelectedAddon: MutableList<AddonModel>?): Double {
        var result:Double=0.0
        if (userSelectedSize == null && userSelectedAddon == null)
            return 0.0
        else if (userSelectedSize == null){
            for (addonModel in userSelectedAddon!!){
                result += addonModel.price!!.toDouble()
            }
            return result
        }
        else if (userSelectedAddon == null){
            result = userSelectedSize!!.price.toDouble()
            return result
        }
        else{
            result = userSelectedSize!!.price.toDouble()
            for (addonModel in userSelectedAddon!!){
                result += addonModel.price!!.toDouble()
            }
            return result
        }
    }

    fun setSpanString(welcome: String, name: String, txtUser: TextView?) {
        val builder= SpannableStringBuilder()
        builder.append(welcome)
        val txtSpannable= SpannableString(name)
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan,0,name.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(txtSpannable)
        txtUser!!.setText(builder,TextView.BufferType.SPANNABLE)

    }

    fun createOrderNumber(): String {
        return StringBuilder()
            .append(System.currentTimeMillis())
            .append(Math.abs(Random().nextInt()))
            .toString()
    }

    fun getDateOfWeek(i: Int): String {
        when(i) {
            1 -> return "Monday"
            2 -> return "Tuesday"
            3 -> return "Wednesday"
            4 -> return "Thursday"
            5 -> return "Friday"
            6 -> return "Saturday"
            7 -> return "Sunday"
            else->
                return "Unk"
        }
    }

    fun convertStatusToText(orderStatus: Int): String {

        when(orderStatus) {
            0 -> return "Placed"
            1 -> return "Shipping"
            2 -> return "Shipped"
            -1 -> return "Cancelled"

            else->
                return "Unk"
        }
    }

    fun buildToken(authorizeToken: String?): String {
        return

    }


    var authorizeToken: String?=null
    var currentToken: String=""
    val ORDER_REF: String="Order"
    var foodSelected: FoodModel? =null
    var categorySelected: CategoryModel? =null
    const val COMMENT_REF: String="Comments"
    const val CATEGORY_REF: String="Category"
    const val FULL_WIDTH_COLUMN: Int=1
    const val DEFAULT_COLUMN_COUNT: Int=0
    const val BEST_DEAL_REF: String="BestDeals"
    const val POPULAR_REF="MostPopular"
    const val USER_REFERENCE="Users"
    var currentUser:UserModel? =null
}