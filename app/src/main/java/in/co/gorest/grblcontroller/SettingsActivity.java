package in.co.gorest.grblcontroller;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import org.greenrobot.eventbus.EventBus;
import in.co.gorest.grblcontroller.events.UiToastEvent;
import in.co.gorest.grblcontroller.listeners.MachineStatusListener;
import in.co.gorest.grblcontroller.model.Constants;

public class SettingsActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null) getSupportActionBar().setSubtitle(getString(R.string.text_application_settings));
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(getString(R.string.shared_preference_key));
            addPreferencesFromResource(R.xml.application_pref);
        }

        @Override
        public void onResume() {
            super.onResume();
            String defaultConnectionType = getPreferenceManager().getSharedPreferences().getString(getString(R.string.preference_default_serial_connection_type), Constants.SERIAL_CONNECTION_TYPE_BLUETOOTH);
            if(defaultConnectionType.equals(Constants.SERIAL_CONNECTION_TYPE_USB_OTG)){
                getPreferenceScreen().findPreference(getString(R.string.preference_auto_connect)).setEnabled(false);
            }

            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.equals(getString(R.string.preference_ignore_error_20))){
                MachineStatusListener.getInstance().setIgnoreError20(sharedPreferences.getBoolean(key, false));
                if(sharedPreferences.getBoolean(key, false)){
                    EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_warning_error_20), true, true));
                }
            }

            if(key.equals(getString(R.string.preference_default_serial_connection_type))
                    || key.equals(getString(R.string.preference_single_step_mode))
                    || key.equals(getString(R.string.usb_serial_baud_rate))
                    || key.equals(getString(R.string.preference_keep_screen_on))
                    || key.equals(getString(R.string.preference_update_pool_interval))
                    || key.equals(getString(R.string.preference_start_up_string))){
                EventBus.getDefault().post(new UiToastEvent("Application restart required", true, true));
            }

        }

    }

}
