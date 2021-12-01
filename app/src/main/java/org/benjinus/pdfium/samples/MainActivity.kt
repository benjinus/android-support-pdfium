package org.benjinus.pdfium.samples;

import java.io.File;
import java.util.List;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.SpannableStringBuilderKt;
import org.benjinus.pdfium.Meta;
import org.benjinus.pdfium.PdfiumSDK;
import org.benjinus.pdfium.search.SearchData;
import org.benjinus.pdfium.util.Size;

public class MainActivity extends AppCompatActivity {

    ImageView mImageView;
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.imageView);
        mTextView = findViewById(R.id.textView);

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

    @SuppressLint("SetTextI18n")
    private void decodePDFPage() {
        try {

            File pdfFile = ((SamplesApplication) getApplication()).createNewSampleFile("AngularSample.pdf");

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

            String searchQuery = "Lorem";
            List<SearchData> searchData = sdk.search(searchQuery, true);
            if (searchData.size()>0){
                Log.d("PDFSDK", "Founded data size: " + searchData.size());
                SpannableStringBuilder s = new SpannableStringBuilder();
                for (int i = 0; i< searchData.size(); i++){
                    SearchData sd = searchData.get(i);
                    Log.d("PDFSDK", "Founded data: " + sd.toString());
                    s.append(sd.getPartOfText());
                    if (i>0 && i < searchData.size()-1){
                        s.append("\n\n");
                    }
                }
                mTextView.setText(s);
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
