package com.nurbk.ps.demochat.other


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.*


fun permission(context: Context, permission: ArrayList<String>, onComplete: () -> Unit) {
    Dexter.withContext(context)
        .withPermissions(
            permission
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                report?.let {
                    if (report.areAllPermissionsGranted()) {
                        onComplete()
                    }
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permission: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                token?.continuePermissionRequest()

            }
        })
        .withErrorListener {
        }
        .check()
}

fun imageToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    return Base64.encodeToString(
        byteArrayOutputStream.toByteArray(),
        Base64.DEFAULT
    )
}


fun decodeImage(data: String): Bitmap? {
    val decodedString: ByteArray = Base64.decode(data, Base64.DEFAULT)
  return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
}




