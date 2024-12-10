package in.co.gorest.grblcontroller.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import androidx.annotation.RequiresApi;

import java.util.Locale;

/**
 * 作者: liuhuaqian on 2020/4/22.
 */
public class LanguageUtil {
    private static final String TAG = "LanguageUtil";

    /**
     * @param context
     * @param newLanguage 想要切换的语言类型 比如 "en" ,"zh"
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void changeAppLanguage(Context context, int newLanguage) {


        Resources resources = context.getResources();

        Configuration configuration = new Configuration();
        configuration.locale = getLocale(newLanguage);
        // updateConfiguration
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(configuration, dm);
    }

    private static Locale getLocale(int newLanguage) {
        String language = "";
        switch (newLanguage) {
            case 1:
                language = "zh";
                break;
            case 2:
                language = "en";
                break;
            case 3:
                language = "ru";
                break;
            case 4:
                language = "es";
                break;
            case 5:
                language = "de";
                break;
            case 6:
                language = "ja";
                break;
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    language = LocaleList.getDefault().get(0).getLanguage();
                }
//                else {
//                    language = LaserApp.sApp.getResources().getConfiguration().locale.getLanguage();
//                }
                break;
        }
        return new Locale(language);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static Context updateResources(Context context, int language) {
        Resources resources = context.getResources();
        Locale locale = getLocale(language);
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        configuration.setLocales(new LocaleList(locale));
        return context.createConfigurationContext(configuration);
    }
}
