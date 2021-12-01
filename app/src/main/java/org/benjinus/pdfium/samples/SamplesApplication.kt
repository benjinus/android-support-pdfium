package org.benjinus.pdfium.samples

import android.app.Application
import org.benjinus.pdfium.samples.utils.IOUtils
import java.io.File
import java.io.IOException
import java.lang.Exception

class SamplesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createSampleFile("AngularSample.pdf")
    }

    private fun createSampleFile(fileName: String): File {
        return try {
            val stream = assets.open(fileName)
            val target = getSampleFile(fileName)
            if (target.isFile) {
                IOUtils.delete(target)
            }
            IOUtils.copy(stream, target)
            target
        } catch (e: IOException) {
            e.printStackTrace()
            throw IOException("Error creating file")
        }
    }

    private fun getSampleFile(fileName: String): File {
        return File(filesDir, fileName)
    }

    fun createNewSampleFile(fileName: String): File {
        val file = getSampleFile(fileName)
        return if (!file.isFile) {
            createSampleFile(fileName)
        } else file
    }
}