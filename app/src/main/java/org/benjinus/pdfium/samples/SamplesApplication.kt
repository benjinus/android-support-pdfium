package org.benjinus.pdfium.samples;

import android.app.Application;
import android.os.Environment;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.benjinus.pdfium.samples.utils.IOUtils;

public class SamplesApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        createSampleFile("AngularSample.pdf");
    }

    private File createSampleFile(String fileName) {
        try {
            InputStream in = getAssets().open(fileName);
            File target = getSampleFile(fileName);
            if (target.isFile()) {
                IOUtils.delete(target);
            }

            IOUtils.copy(in, target);
            return target;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public File getSampleFile(String fileName) {
        return new File(getFilesDir(), fileName);
    }

    public File createNewSampleFile(String fileName) {

        File file = getSampleFile(fileName);

        if (!file.isFile()) {
            return createSampleFile(fileName);
        }

        return file;
    }
}
