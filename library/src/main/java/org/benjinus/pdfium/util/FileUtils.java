package org.benjinus.pdfium.util;

import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.lang.reflect.Field;

public class FileUtils {

    private static final Class FD_CLASS = FileDescriptor.class;

    private static Field mFdField = null;

    public static int getNumFd(ParcelFileDescriptor fdObj) {
        try {
            if (mFdField == null) {
                mFdField = FD_CLASS.getDeclaredField("descriptor");
                mFdField.setAccessible(true);
            }

            return mFdField.getInt(fdObj.getFileDescriptor());
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
