package in.co.gorest.grblcontroller;

import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceFragmentCompat;
import java.util.Objects;

public class SettingActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null) getSupportActionBar().setSubtitle(getString(R.string.text_app_about));
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new AppAboutFragment()).commit();
    }

    public static class AppAboutFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(getString(R.string.shared_preference_key));
            addPreferencesFromResource(R.xml.application_about);
            findPreference("pref_app_version").setSummary(BuildConfig.VERSION_NAME);
            if(this.hasPaidVersion()){
                getPreferenceScreen().removePreference(Objects.requireNonNull(findPreference("buy_grbl_controller_plus")));
            }

        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {

        }

        public boolean hasPaidVersion() {
            PackageManager pm = requireActivity().getPackageManager();
            try {
                pm.getPackageInfo("in.co.gorest.grblcontroller.plus", PackageManager.GET_ACTIVITIES);
                return true;
            } catch (PackageManager.NameNotFoundException ignored) {}

            return false;
        }


    }
}
