package kalpas.testservice;

import android.os.Environment;

public class StorageWriter {

    public boolean isAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

}
