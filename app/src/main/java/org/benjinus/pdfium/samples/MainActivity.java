package org.benjinus.pdfium.samples;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;

import android.Manifest;
import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import java.io.File;
import org.benjinus.pdfium.Meta;
import org.benjinus.pdfium.PdfiumSDK;
import org.benjinus.pdfium.util.Size;

public class MainActivity extends AppCompatActivity {

    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.imageView);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }


        decodePDFPage();
    }

    private void decodePDFPage() {
        try {

            File pdfFile = ((SamplesApplication) getApplication()).createNewSampleFile("Sample.pdf");

            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(pdfFile, MODE_READ_ONLY);

            PdfiumSDK sdk = new PdfiumSDK();
            sdk.newDocument(fileDescriptor);

            Log.d("PDFSDK", "Page count: " + sdk.getPageCount());

            Meta meta = sdk.getDocumentMeta();
            Log.d("PDFSDK", meta.toString());

            sdk.openPage(0);

            Size size = sdk.getPageSize(0);
            Log.d("PDFSDK", "Page size: " + size.toString());

            int width = getScreenWidth();
            int height = getScreenHeight();

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            sdk.renderPageBitmap(bitmap, 0, 0, 0, width, height, true);

            mImageView.setImageBitmap(bitmap);

            sdk.closeDocument();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    private int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

}
