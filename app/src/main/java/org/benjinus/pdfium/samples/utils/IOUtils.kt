package org.benjinus.pdfium.samples.utils

import androidx.annotation.Keep
import java.io.*
import kotlin.Throws

/**
 * Helper methods for dealing with Files.
 */
@Keep
object IOUtils {
    @Throws(IOException::class)
    fun copy(inputStream: InputStream?, output: File?) {
        var outputStream: OutputStream? = null
        try {
            outputStream = FileOutputStream(output)
            var read = 0
            val bytes = ByteArray(1024)
            while (inputStream!!.read(bytes).also { read = it } != -1) {
                outputStream.write(bytes, 0, read)
            }
        } finally {
            try {
                inputStream?.close()
            } finally {
                outputStream?.close()
            }
        }
    }

    fun delete(file: File) {
        try {
            file.delete()
        } catch (ignored: Throwable) {
        }
    }
}