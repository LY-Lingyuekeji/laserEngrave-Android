package in.co.gorest.grblcontroller.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.io.File;
import in.co.gorest.grblcontroller.GrblController;

public class PrefUtil {
    private static PrefUtil instance = null;
    private static String shareName = "ENGRAVER_SHARE_DATA";
    private Context mContext;
    private SharedPreferences preferences;
    private final String TAG = PrefUtil.class.getSimpleName();
    private String DATA_URL = "/data/data/";
    private String SHARED_PREFS = "/shared_prefs";

    private PrefUtil(Context context) {
        this.mContext = context;
        this.preferences = context.getSharedPreferences(shareName, 0);
    }

    public static PrefUtil getInstance() {
        if (instance == null) {
            synchronized (PrefUtil.class) {
                if (instance == null) {
                    instance = new PrefUtil(GrblController.getInstance().getApplicationContext());
                }
            }
        }
        return instance;
    }

    public void put(String str, boolean z) {
        SharedPreferences.Editor edit = this.preferences.edit();
        if (edit != null) {
            edit.putBoolean(str, z);
            edit.commit();
        }
    }

    private SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(shareName, 0);
    }

    public void putNew(Context context, String str, String str2) {
        SharedPreferences.Editor edit = getPreferences(context).edit();
        if (edit != null) {
            Log.i("edit", str);
            edit.putString(str, str2);
            edit.commit();
        }
    }

    public void put(String str, String str2) {
        SharedPreferences.Editor edit = this.preferences.edit();
        if (edit != null) {
            edit.putString(str, str2);
            edit.commit();
        }
    }

    public void put(String str, int i) {
        SharedPreferences.Editor edit = this.preferences.edit();
        if (edit != null) {
            edit.putInt(str, i);
            edit.commit();
        }
    }

    public void put(String str, float f) {
        SharedPreferences.Editor edit = this.preferences.edit();
        if (edit != null) {
            edit.putFloat(str, f);
            edit.commit();
        }
    }

    public void put(String str, long j) {
        SharedPreferences.Editor edit = this.preferences.edit();
        if (edit != null) {
            edit.putLong(str, j);
            edit.commit();
        }
    }

    public String get(String str) {
        return this.preferences.getString(str, "");
    }

    public String get(String str, String str2) {
        return this.preferences.getString(str, str2);
    }

    public boolean get(String str, boolean z) {
        return this.preferences.getBoolean(str, z);
    }

    public int get(String str, int i) {
        return this.preferences.getInt(str, i);
    }

    public float get(String str, float f) {
        return this.preferences.getFloat(str, f);
    }

    public long get(String str, long j) {
        return this.preferences.getLong(str, j);
    }

    public void remove(String str) {
        SharedPreferences.Editor edit = this.preferences.edit();
        edit.remove(str);
        edit.commit();
    }

    public void clearAll() {
        try {
            File file = new File(this.DATA_URL + this.mContext.getPackageName() + this.SHARED_PREFS, shareName + ".xml");
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception unused) {
        }
    }
}
