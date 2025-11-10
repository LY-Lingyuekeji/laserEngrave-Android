package in.co.gorest.grblcontroller.util;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 阿里百炼大模型
 *
 * 文生图工具类（测试使用）
 */
public class DashScopeText2Image {
    private static final String API_KEY = "sk-54147ad9def14c2ab0179a5bce3738a8"; // 测试使用，正式请替换API—KEY
    private static final String CREATE_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis";
    private static final String QUERY_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/";

    public interface ResultCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public static void generateImage(String prompt, ResultCallback callback) {
        OkHttpClient client = new OkHttpClient();

        JSONObject root = new JSONObject();
        try {
            JSONObject input = new JSONObject();
            input.put("prompt", prompt);

            JSONObject parameters = new JSONObject();
            parameters.put("size", "1024*1024");
            parameters.put("n", 1);

            root.put("model", "wanx-v1");
            root.put("input", input);
            root.put("parameters", parameters);
        } catch (Exception e) {
            callback.onError("构造请求失败: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(root.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(CREATE_URL)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("X-DashScope-Async", "enable")
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("创建任务失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                try {
                    JSONObject json = new JSONObject(result);
                    String taskId = json.getJSONObject("output").getString("task_id");
                    pollForResult(client, taskId, callback);
                } catch (Exception e) {
                    callback.onError("解析任务ID失败: " + e.getMessage());
                }
            }
        });
    }

    private static void pollForResult(OkHttpClient client, String taskId, ResultCallback callback) {
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable[] polling = new Runnable[1];

        polling[0] = new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url(QUERY_URL + taskId)
                        .addHeader("Authorization", "Bearer " + API_KEY)
                        .get()
                        .build();

                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        callback.onError("查询失败: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String res = response.body().string();
                        try {
                            JSONObject json = new JSONObject(res);
                            JSONObject output = json.getJSONObject("output");
                            String status = output.getString("task_status");

                            if ("SUCCEEDED".equalsIgnoreCase(status)) {
                                JSONArray results = output.getJSONArray("results");
                                if (results.length() > 0) {
                                    String imageUrl = results.getJSONObject(0).getString("url");
                                    callback.onSuccess(imageUrl);
                                } else {
                                    callback.onError("无图片结果");
                                }
                            } else if ("FAILED".equalsIgnoreCase(status)) {
                                callback.onError("任务失败");
                            } else {
                                // 继续轮询
                                handler.postDelayed(polling[0], 2000);
                            }
                        } catch (Exception e) {
                            callback.onError("结果解析失败: " + e.getMessage());
                        }
                    }

                });
            }
        };

        handler.post(polling[0]);
    }
}
