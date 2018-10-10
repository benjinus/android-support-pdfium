package org.benjinus.pdfium.samples;

import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.benjinus.pdfium.Meta;
import org.benjinus.pdfium.PdfiumSDK;
import org.benjinus.pdfium.util.Size;

import java.io.File;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        decodePDFPage();
    }

    private void decodePDFPage() {
        try {
            File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File pdfFile = new File(downloads, "周长青.pdf");

            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(pdfFile, MODE_READ_ONLY);

            PdfiumSDK sdk = new PdfiumSDK();
            sdk.newDocument(fileDescriptor);

            Log.d("PDFSDK", "Page count: " + sdk.getPageCount());

            Meta meta = sdk.getDocumentMeta();
            Log.d("PDFSDK", meta.toString());

            sdk.openPage(0);

            Size size = sdk.getPageSize(0);
            Log.d("PDFSDK", "Page size: " + size.toString());

//            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//            sdk.renderPageBitmap(bitmap, 0, 0, 0, );

            sdk.closeDocument();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
