package in.co.gorest.grblcontroller.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.RemoteFileAdapter;
import in.co.gorest.grblcontroller.util.NettyClient;

public class RemoteFileFragment extends Fragment {
    // 用于日志记录的标签
    private final static String TAG = RemoteFileFragment.class.getSimpleName();

    // 设备SD卡剩余空间
    private TextView tvSdFree;
    // 设备SD卡已使用空间
    private TextView tvSdUsed;
    // 设备SD卡总储存空间
    private TextView tvSdTotal;
    // 设备SD卡文件列表
    private RecyclerView rvRemoteFile;
    // 设备SD卡文件列表适配器
    private RemoteFileAdapter adapter;
    // 设备SD卡文件列表数据源
    private ArrayList<String> remoteFileList = new ArrayList<>();


    public RemoteFileFragment() {
    }

    public static RemoteFileFragment newInstance() {
        return new RemoteFileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_remote_file, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 初始化界面
        initView(view);
        // 初始化数据
        initData();
        // 初始化事件监听
        setupListeners();
    }

    /**
     * 初始化界面
     *
     * @param view view
     */
    private void initView(View view) {
        // 选择文件
        rvRemoteFile = view.findViewById(R.id.rv_remote_file);
        // 设备SD卡剩余空间
        tvSdFree = view.findViewById(R.id.tv_sd_free);
        // 设备SD卡已使用空间
        tvSdUsed = view.findViewById(R.id.tv_sd_used);
        // 设备SD卡总储存空间
        tvSdTotal = view.findViewById(R.id.tv_sd_total);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // TODO 检查连接并获取SD卡列表
        boolean isConnected = NettyClient.getInstance(null).getConnectStatus();
        if (isConnected) {
            Log.d(TAG, "isConnected=" + isConnected);
            NettyClient.getInstance(new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    Log.d(TAG, "message=" + msg.obj);
                    // 检查数据是否符合预期的格式
                    if (isValidData(msg.obj.toString())) {
                        // 如果符合格式，解析并添加到 remoteFileList
                        parseData(msg.obj.toString());
                        Log.d(TAG, "Updated remoteFileList: " + remoteFileList);
                        // 设置 LayoutManager
                        rvRemoteFile.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                        // 初始化适配器
                        adapter = new RemoteFileAdapter(remoteFileList);
                        // 设置适配器
                        rvRemoteFile.setAdapter(adapter);
                    } else if (isValidSdCardData(msg.obj.toString())) {
                        // 解析并设置 SD 卡的空间信息
                        parseSdCardData(msg.obj.toString());
                    }
                    return false;
                }
            })).sendMsgToServer("$SD/List\r\n".getBytes(StandardCharsets.UTF_8), null);
        } else {
            Toast.makeText(getActivity(), "请先连接设备", Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {

    }

    /**
     * 检查是否符合预期的数据格式
     *
     * @param data 源数据
     * @return 布尔值
     */
    private boolean isValidData(String data) {
        String regex = "FILE:([\\w/\\-_\\.]+)\\|SIZE:(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);
        return matcher.find();  // 如果匹配到至少一个符合格式的项，返回 true
    }

    /**
     * 解析符合格式的数据并添加到 remoteFileList
     *
     * @param data 数据
     */
    private void parseData(String data) {
        String regex = "FILE:([\\w/\\-_\\.]+)\\|SIZE:(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);

        // 提取每一条匹配的数据
        while (matcher.find()) {
            String file = matcher.group(1);
            String size = matcher.group(2);

            // 将符合格式的文件信息添加到 remoteFileList
            remoteFileList.add("File: " + file + ", Size: " + size);
        }
    }

    /**
     * 检查是否符合 SD 卡空间信息格式
     *
     * @param data 源数据
     * @return 布尔值
     */
    private boolean isValidSdCardData(String data) {
        String regex = "\\[SD Free:(\\d+\\.\\d+ \\w+) Used:(\\d+\\.\\d+ \\w+) Total:(\\d+\\.\\d+ \\w+)\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);
        return matcher.find();  // 如果匹配到至少一个符合格式的项，返回 true
    }

    /**
     * 解析 SD 卡空间信息并显示到界面
     *
     * @param data 数据
     */
    private void parseSdCardData(String data) {
        String regex = "\\[SD Free:(\\d+\\.\\d+ \\w+) Used:(\\d+\\.\\d+ \\w+) Total:(\\d+\\.\\d+ \\w+)\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);

        if (matcher.find()) {
            String free = matcher.group(1);
            String used = matcher.group(2);
            String total = matcher.group(3);

            // 设置数据到 TextView
            tvSdFree.setText(free);
            tvSdUsed.setText(used);
            tvSdTotal.setText(total);
        }
    }
}