package in.co.gorest.grblcontroller.util;

import static com.xuexiang.xui.XUI.getContext;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.model.DeviceConnectRecord;
import in.co.gorest.grblcontroller.model.EngraveHistoryRecord;
import in.co.gorest.grblcontroller.model.StaModelConfig;

public class FileUtil {

    // 写入：保存 List<EngraveHistoryRecord> 到文件
    public static void saveEngraveHistoryToFile(List<EngraveHistoryRecord> records, File file) {
        String json = new Gson().toJson(records);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 读取：从文件中读取 List<EngraveHistoryRecord>
    public static List<EngraveHistoryRecord> readEngraveHistoryFromFile(File file) {
        if (!file.exists()) return new ArrayList<>();

        try {
            String content = readFile(file);
            return new Gson().fromJson(content, new TypeToken<List<EngraveHistoryRecord>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 写入：保存 List<DeviceConnectRecord> 到默认路径
    public static void saveConnectRecordList(List<DeviceConnectRecord> records, File file) {
        String json = new Gson().toJson(records);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 读取：从默认路径读取 List<DeviceConnectRecord>
    public static List<DeviceConnectRecord> readConnectRecordList(File file) {
        if (!file.exists()) return new ArrayList<>();

        try {
            String content = readFile(file);
            return new Gson().fromJson(content, new TypeToken<List<DeviceConnectRecord>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    // 写入：保存 List<StaModelConfig> 到默认路径
    public static void saveStaModelConfigList(List<StaModelConfig> configs, File file) {
        String json = new Gson().toJson(configs);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 读取：从默认路径读取 List<StaModelConfig>
    public static List<StaModelConfig> readStaModelConfigList(File file) {
        if (!file.exists()) return new ArrayList<>();

        try {
            String content = readFile(file);
            return new Gson().fromJson(content, new TypeToken<List<StaModelConfig>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 工具方法：读取文件内容为字符串
    public static String readFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }




}
