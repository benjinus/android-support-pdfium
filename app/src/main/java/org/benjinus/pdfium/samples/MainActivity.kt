package org.benjinus.pdfium.samples

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.benjinus.pdfium.PdfiumSDK

class MainActivity : AppCompatActivity() {
    private var mImageView: ImageView? = null
    private var mTextView: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mImageView = findViewById(R.id.imageView)
        mTextView = findViewById(R.id.textView)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        decodePDFPage()
    }

    @SuppressLint("SetTextI18n")
    private fun decodePDFPage() {
        try {
            val pdfFile =
                (application as SamplesApplication).createNewSampleFile("AngularSample.pdf")
            val sdk = PdfiumSDK(pdfFile, null)
            Log.d("PDFSDK", "Page count: " + sdk.pageCount)
            val meta = sdk.documentMeta
            Log.d("PDFSDK", meta.toString())
            sdk.openPage(0)
            val size = sdk.getPageSize(0)
            Log.d("PDFSDK", "Page size: $size")
            val width = screenWidth
            val height = screenHeight
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            sdk.renderPageBitmap(bitmap, 0, 0, 0, width, height, true)
            mImageView?.setImageBitmap(bitmap)
            val searchQuery = "Lorem"
            val searchData = sdk.search(searchQuery, true)
            if (searchData.isNotEmpty()) {
                Log.d("PDFSDK", "Founded data size: " + searchData.size)
                val s = SpannableStringBuilder()
                for (i in searchData.indices) {
                    val sd = searchData[i]
                    Log.d("PDFSDK", "Founded data: $sd")
                    s.append(sd.partOfText)
                    if (i > 0 && i < searchData.size - 1) {
                        s.append("\n\n")
                    }
                }
                mTextView?.text = s
            }
            sdk.closeDocument()
        } catch (e: Exception) {
            Log.d("PDFSDK", "Exception: " + e.message)
            e.printStackTrace()
        }
    }

    private val screenHeight: Int
        get() = Resources.getSystem().displayMetrics.heightPixels
    private val screenWidth: Int
        get() = Resources.getSystem().displayMetrics.widthPixels
}