package com.alvimatruck.utils


import android.content.Context
import android.content.SharedPreferences

class SharedHelper {
    companion object {
        var sharedPreferences: SharedPreferences? = null
        var editor: SharedPreferences.Editor? = null

        fun putKey(context: Context, Key: String?, Value: String?) {
            sharedPreferences =
                context.getSharedPreferences("AlvimaTruck_preferances", Context.MODE_PRIVATE)
            editor = sharedPreferences!!.edit()
            editor!!.putString(Key, Value)
            editor!!.apply()
        }


        fun getKey(context: Context, Key: String?): String {
            sharedPreferences =
                context.getSharedPreferences("AlvimaTruck_preferances", Context.MODE_PRIVATE)
            val data: String = sharedPreferences!!.getString(Key, "").toString()
            return data
        }


        fun putKey(context: Context, Key: String?, Value: Boolean?) {
            sharedPreferences =
                context.getSharedPreferences("AlvimaTruck_preferances", Context.MODE_PRIVATE)
            editor = sharedPreferences!!.edit()
            editor!!.putBoolean(Key, Value!!)
            editor!!.apply()
        }

        fun getBoolKey(context: Context, Key: String?): Boolean {
            sharedPreferences =
                context.getSharedPreferences("AlvimaTruck_preferances", Context.MODE_PRIVATE)
            return sharedPreferences!!.getBoolean(Key, false)
        }

    }
}