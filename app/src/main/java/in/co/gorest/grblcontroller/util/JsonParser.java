package in.co.gorest.grblcontroller.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.model.Device;

public class JsonParser {
    public static List<Device> parseDevicesFromJson(String json, String type) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        Type listType = new TypeToken<List<Device>>(){}.getType();

        List<Device> devices = new ArrayList<>();
        if (type.equals("laser")) {
            devices = gson.fromJson(jsonObject.getAsJsonArray("laser"), listType);
        } else if (type.equals("cnc")) {
            devices = gson.fromJson(jsonObject.getAsJsonArray("cnc"), listType);
        }
        return devices;
    }
}
