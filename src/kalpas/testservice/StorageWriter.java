package kalpas.testservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class StorageWriter {

    public boolean isAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public void appendText(Context context, String text) {
        File dir= new File(Environment.getExternalStorageDirectory().getPath() + "/smslog");
        dir.mkdirs();
        File file = new File(dir, Preferences.getFile());
        Log.d(MainActivity.TAG, file.getAbsolutePath());
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file, true);
            stream.write(text.getBytes());
            stream.flush();
            stream.close();
        } catch (FileNotFoundException e) {
            Log.e(MainActivity.TAG, "file not found", e);
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "IO exception", e);
        }
    }

}
