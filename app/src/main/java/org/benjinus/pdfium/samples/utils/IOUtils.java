package org.benjinus.pdfium.samples.utils;

import androidx.annotation.Keep;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper methods for dealing with Files.
 */
@Keep
@SuppressWarnings("WeakerAccess")
public class IOUtils {

    public static void copy(InputStream inputStream, File output) throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(output);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        }
    }

    public static void delete(File file) {
        try {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        } catch (Throwable ignored) {
        }
    }
}
