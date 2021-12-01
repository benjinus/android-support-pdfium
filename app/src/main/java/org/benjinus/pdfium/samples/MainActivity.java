package org.benjinus.pdfium.samples;

import java.io.File;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.benjinus.pdfium.Meta;
import org.benjinus.pdfium.PdfiumSDK;
import org.benjinus.pdfium.search.TextSearchContext;
import org.benjinus.pdfium.util.Size;

public class MainActivity extends AppCompatActivity {

    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.imageView);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {

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
                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        decodePDFPage();
    }

    private void decodePDFPage() {
        try {

            File pdfFile = ((SamplesApplication) getApplication()).createNewSampleFile("3941.pdf");

            PdfiumSDK sdk = new PdfiumSDK(pdfFile, null);

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

            String searchQuery = "angular";
            for (int i = 0; i < sdk.getPageCount(); i++) {
                String chars = sdk.extractCharacters(i, 0, sdk.countCharactersOnPage(i));
                if (chars != null && chars.length() > 0) {
                    //                    Log.d("PDFSDK", chars);
                    if (chars.toLowerCase().contains(searchQuery.toLowerCase())) {
                        Log.d("PDFSDK", "Page " + (i + 1) + " chars count: " + chars.length());
                        Log.d("PDFSDK", "Page " + (i + 1) + " equals query: " + searchQuery);
                        TextSearchContext ctx = sdk.newPageSearch(i, searchQuery, false, true);
                        ctx.prepareSearch();
                        ctx.startSearch();
                        if (ctx.countResult() > 0) {
                            Log.d("PDFSDK", "Search \"" + searchQuery + "\" index " + (i + 1));
                        }
                        ctx.stopSearch();
                    }
                }
            }

            sdk.closeDocument();

        } catch (Exception e) {
            Log.d("PDFSDK", "Exception: " + e.getMessage());
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
