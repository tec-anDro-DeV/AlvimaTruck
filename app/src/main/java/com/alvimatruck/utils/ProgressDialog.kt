package com.alvimatruck.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.alvimatruck.R


object ProgressDialog {
    private var progressDialog: Dialog? = null
    fun start(context: Context) {
        if (!isShowing) {
            if (!(context as Activity).isFinishing) {
                progressDialog = Dialog(context)
                progressDialog!!.setCancelable(false)
                progressDialog!!.setCanceledOnTouchOutside(false)
                progressDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                progressDialog!!.setContentView(R.layout.view_progress_dialog)
                progressDialog!!.show()

            }

        }
    }

    fun dismiss() {
        try {
            if (progressDialog != null && progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
            }
        } catch (e: Exception) {
            // Handle or log or ignore
        } finally {
            progressDialog = null
        }
    }

    val isShowing: Boolean
        get() = if (progressDialog != null) {
            progressDialog!!.isShowing
        } else {
            false
        }
}