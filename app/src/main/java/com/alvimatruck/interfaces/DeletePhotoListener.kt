package com.alvimatruck.interfaces

import android.net.Uri

interface DeletePhotoListener {
    fun onDeletePhoto(imageUri: Uri)
}