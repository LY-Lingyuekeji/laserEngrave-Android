package in.co.gorest.grblcontroller.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.MainActivity;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.EngraveActivity;
import in.co.gorest.grblcontroller.activity.PreViewActivity;
import in.co.gorest.grblcontroller.base.BaseDialog;
import in.co.gorest.grblcontroller.events.RemoteFileLineJugdeCommandMessageEvent;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.util.NettyClient;
import in.co.gorest.grblcontroller.util.ZoomViewBean;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RemoteFileAdapter extends RecyclerView.Adapter<RemoteFileAdapter.ViewHolder> {

    // 用于日志记录的标签
    private static final String TAG = RemoteFileAdapter.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例。
    protected EnhancedSharedPreferences sharedPref;
    // 存储数据
    private ArrayList<String> dataList;
    // 当前的机器状态
    private String strMachineStatus;
    // 巡边功率
    private int lineJudgeLaserLevel;
    // 用来跟踪连续匹配的次数
    private int consecutiveMatches = 0;


    private  Activity activity;

    public RemoteFileAdapter(Activity activity, ArrayList<String> data) {
        this.activity = activity;
        this.dataList = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_remote_file, parent, false);
        // 初始化共享偏好设置实例
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), parent.getContext().getString(R.string.shared_preference_key));
        return new ViewHolder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // 绑定数据到视图
        String data = dataList.get(position);
        String file = data.split(",")[0].split(":")[1].trim();
        holder.itemRemoteFileName.setText(file);  // 显示 File
        // 处理文件大小并显示
        String size = data.split(",")[1].split(":")[1].trim();
        holder.itemRemoteFileSize.setText(formatSize(Long.parseLong(size)));  // 显示 Size

        holder.itemRemoteFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("RemoteFileAdapter", "fileName=" + holder.itemRemoteFileName.getText().toString());
                // 跳转雕刻页面
                Intent intent = new Intent(GrblController.getInstance(), EngraveActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  // 添加这个 FLAG
                intent.putExtra("imagePath", "");
                intent.putExtra("filePath", "/storage/emulated/0/Android/data/com.lingyue.laserengraving/files/laser" + holder.itemRemoteFileName.getText().toString());
                GrblController.getInstance().startActivity(intent);

            }
        });

        // 为"更多"图标设置点击事件
        holder.itemRemoteFileMore.setOnClickListener(v -> showPopupMenu(v, position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout itemRemoteFile;
        private TextView itemRemoteFileName;
        private TextView itemRemoteFileSize;
        private ImageView itemRemoteFileMore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemRemoteFile = itemView.findViewById(R.id.item_remote_file);
            itemRemoteFileName = itemView.findViewById(R.id.item_remote_file_name);
            itemRemoteFileSize = itemView.findViewById(R.id.item_remote_file_size);
            itemRemoteFileMore = itemView.findViewById(R.id.item_remote_file_more);
        }
    }

    /**
     * 格式化文件大小为合适的单位
     *
     * @param size 字节大小
     * @return 格式化后的文件大小
     */
    private String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    private void showPopupMenu(View view, int position) {
        // 创建 PopupMenu
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);

        // 获取 menuInflater 并加载自定义菜单
        popupMenu.getMenuInflater().inflate(R.menu.menu_remote_file_options, popupMenu.getMenu());

        // 设置菜单项点击事件
        popupMenu.setOnMenuItemClickListener(item -> {
            String fileName = dataList.get(position).split(",")[0].split(":")[1].trim();
            switch (item.getItemId()) {
                case R.id.option_run:
                    Log.d("RemoteFileAdapter", "fileName=" + fileName);
                    // 跳转雕刻页面
                    Intent intent = new Intent(GrblController.getInstance(), EngraveActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  // 添加这个 FLAG
                    intent.putExtra("imagePath", "");
                    intent.putExtra("filePath", "/storage/emulated/0/Android/data/com.lingyue.laserengraving/files/laser" + fileName);
                    GrblController.getInstance().startActivity(intent);
                    return true;
                case R.id.option_linejudge:
                    downloadAndAnalyzeNcFile(view.getContext(), "http://192.168.4.1/SD//" + fileName, fileName);
                    return true;
                case R.id.option_delete:
                    BaseDialog.showCustomDialog(view.getContext(),
                            "温馨提示", "是否删除当前文件？",
                            "确定", "取消",
                            v -> {
                                // 执行删除操作
                                NettyClient.getInstance(null).sendMsgToServer(("$SD/Delete=" + fileName + "\r\n").getBytes(StandardCharsets.UTF_8), null);
                                // 从数据源中移除被删除的文件
                                dataList.remove(position);
                                // 通知适配器更新列表，移除指定项
                                notifyItemRemoved(position);
                                // 重新更新后续项的索引，避免删除后顺序混乱
                                notifyItemRangeChanged(position, dataList.size() - position);
                            },
                            v -> {

                            });

                    return true;
                default:
                    return false;
            }
        });

        // 显示弹窗
        popupMenu.show();
    }

    /**
     * 下载并分析文件
     * @param context 上下文
     * @param fileUrl 文件地址
     * @param fileName 文件名
     */
    public void downloadAndAnalyzeNcFile(Context context, String fileUrl, String fileName) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(fileUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("NC文件", "下载失败");
                    return;
                }

                InputStream inputStream = response.body().byteStream();

                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Downloads.DISPLAY_NAME, fileName); // "file.nc"
                contentValues.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
                contentValues.put(MediaStore.Downloads.IS_PENDING, 1);

                ContentResolver resolver = context.getContentResolver();
                @SuppressLint({"NewApi", "LocalSuppress"}) Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Uri fileUri = resolver.insert(collection, contentValues);

                if (fileUri == null) {
                    Log.e("NC文件", "文件保存失败");
                    return;
                }

                // 保存文件
                try (OutputStream outputStream = resolver.openOutputStream(fileUri)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                    outputStream.flush();
                }

                // 写入完成
                contentValues.clear();
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0);
                resolver.update(fileUri, contentValues, null, null);

                inputStream.close();

                // 分析并删除
                analyzeNcFileAndDelete(context, fileUri);
            }
        });
    }

    /**
     * 分析并删除下载的文件
     * @param context 上下文
     * @param fileUri 文件地址
     */
    private void analyzeNcFileAndDelete(Context context, Uri fileUri) {
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;

        Pattern xPattern = Pattern.compile("X([-+]?[0-9]*\\.?[0-9]+)");
        Pattern yPattern = Pattern.compile("Y([-+]?[0-9]*\\.?[0-9]+)");

        try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // 防止多条命令连在一起的情况，拆开处理
                String[] segments = line.split("G[01]");
                for (String seg : segments) {
                    Matcher xMatcher = xPattern.matcher(seg);
                    while (xMatcher.find()) {
                        float xVal = Float.parseFloat(xMatcher.group(1));
                        if (xVal > maxX) maxX = xVal;
                    }

                    Matcher yMatcher = yPattern.matcher(seg);
                    while (yMatcher.find()) {
                        float yVal = Float.parseFloat(yMatcher.group(1));
                        if (yVal > maxY) maxY = yVal;
                    }
                }
            }

            Log.d("NC解析", "✅ 最大 X: " + maxX + "，最大 Y: " + maxY);

            // TODO 巡边
            EventBus.getDefault().post(new RemoteFileLineJugdeCommandMessageEvent(maxX, maxY));


        } catch (Exception e) {
            e.printStackTrace();
        }

        // 删除文件
        try {
            int deleted = context.getContentResolver().delete(fileUri, null, null);
            Log.d("NC文件", "🗑 文件已删除: " + (deleted > 0 ? "成功" : "失败"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
