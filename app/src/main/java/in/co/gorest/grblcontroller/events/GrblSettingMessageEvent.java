package in.co.gorest.grblcontroller.events;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.co.gorest.grblcontroller.util.GrblLookups;

public class GrblSettingMessageEvent {

    private final GrblLookups lookups;
    private final String message;
    private final static Pattern MESSAGE_REGEX = Pattern.compile("(\\$\\d+)=([^ ]*)\\s?\\(?([^)]*)?\\)?");

    private String setting;
    private String value;
    private String units;
    private String description;
    private String shortDescription;


    public GrblSettingMessageEvent(GrblLookups lookups, String message) {
        this.lookups = lookups;
        this.message = message;
        parse();
    }

    @NonNull
    @Override
    public String toString() {
        String descriptionStr = "";
        if (!TextUtils.isEmpty(description)) {
            if (!TextUtils.isEmpty(units)) {
                descriptionStr = " (" + shortDescription + ", " + units + ")";
            } else {
                descriptionStr = " (" + description + ")";
            }
        }

        return String.format("%s = %s   %s", setting, value, descriptionStr);
    }

    public String getSetting() {
        return setting;
    }

    public String getUnits() {
        return units;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    private void parse() {
        Matcher m = MESSAGE_REGEX.matcher(message);
        if (m.find()) {
            setting = m.group(1);
            value = m.group(2);

            String[] lookup = lookups.lookup(setting);
            if (lookup != null) {
                units = lookup[2];
                description = lookup[3];
                shortDescription = lookup[1];
            }
        }
    }

}
