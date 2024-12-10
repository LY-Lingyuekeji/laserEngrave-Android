package in.co.gorest.grblcontroller.util;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class FileManager {
    public static FileManager fileManager;
    private ArrayList<String> delPaths = new ArrayList<>();

    public static FileManager get() {
        if (fileManager == null) {
            fileManager = new FileManager();
        }
        return fileManager;
    }

    public void addDelPath(String path) {
        if (delPaths == null) delPaths = new ArrayList<>();
        delPaths.add(path);
    }

    public void removeDelPath(String path) {
        delPaths.remove(path);
    }

    public void clearPaths() {
        for (String delPath : delPaths) {
            Log.e("spm", "delete：" + delPath + "," + new File(delPath).delete());
        }
        fileManager = null;
    }
}
