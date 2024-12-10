package in.co.gorest.grblcontroller.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.events.AfterUploadFileEvent;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FileUploader {

    private static final String TAG = "FileUploader";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120,TimeUnit.SECONDS)
            .build();

    public void uploadFile(String filePath, String url, Context context) {


        // 使用自定义布局创建 AlertDialog
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_upload, null);

        // 获取 ProgressBar 和 TextView
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        TextView progressText = dialogView.findViewById(R.id.progressText);
        TextView progressSpeed = dialogView.findViewById(R.id.progressSpeed);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("提示");
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setCancelable(false);
        AlertDialog dialog = alertDialogBuilder.create();

        ((Activity) context).runOnUiThread(() -> {
            dialog.show();
        });


        File file = new File(filePath);


        ProgressRequestBody requestBody = new ProgressRequestBody(file, "application/octet-stream", new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage, long bytesUploaded, long totalBytes, String uploadSpeed) {
                // 更新 ProgressBar 和 TextView
                ((Activity) context).runOnUiThread(() -> {
                    progressBar.setProgress(percentage);
                    progressText.setText(percentage + "%");
                    progressSpeed.setText("上传速度：" + uploadSpeed);
                    Log.d(TAG, "percentage="+ percentage + "     speed="+ uploadSpeed);
                });
            }

            @Override
            public void onError() {
                ((Activity) context).runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(context, "上传失败", Toast.LENGTH_SHORT).show();
                    // 上传完成 消息分发
//                    EventBus.getDefault().post(new AfterUploadFileEvent("FAILED"));
                });
            }

            @Override
            public void onFinish() {
                ((Activity) context).runOnUiThread(() -> {
                    progressBar.setProgress(100);
                    progressText.setText("上传完成");
                    Toast.makeText(context, "上传完成", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();


                    // 上传完成 消息分发
//                    EventBus.getDefault().post(new AfterUploadFileEvent("SUCCESS"));

                });
            }
        });


        // Create a multipart request body
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), requestBody)
                .build();

        // Create the request
        Request request = new Request.Builder()
                .url(url)
                .post(multipartBody)
                .build();

        // Execute the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle the error
                Log.d(TAG ,"onFailure: " + e.getMessage().toString());
                dialog.dismiss();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    Log.d(TAG ,"Upload successful: " + response.body().string());
                    // TODO 软复位
                } else {
                    dialog.dismiss();
                    // Handle the failure
                    Log.d(TAG ,"Upload failed: " + response.body().string());
                }
            }
        });
    }

}
