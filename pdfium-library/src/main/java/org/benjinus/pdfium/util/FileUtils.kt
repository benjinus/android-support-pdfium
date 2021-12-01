package org.benjinus.pdfium.util

import android.annotation.SuppressLint
import android.os.ParcelFileDescriptor
import java.io.FileDescriptor
import java.lang.reflect.Field

object FileUtils {
    private val FD_CLASS: Class<*> = FileDescriptor::class.java
    private var mFdField: Field? = null
    @SuppressLint("DiscouragedPrivateApi")
    @JvmStatic
    fun getNumFd(fdObj: ParcelFileDescriptor): Int {
        return try {
            if (mFdField == null) {
                mFdField = FD_CLASS.getDeclaredField("descriptor")
                mFdField?.isAccessible = true
            }
            mFdField?.getInt(fdObj.fileDescriptor) ?: -1
        } catch (e: ReflectiveOperationException) {
            e.printStackTrace()
            -1
        }
    }
}