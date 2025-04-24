package in.co.gorest.grblcontroller.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import in.co.gorest.grblcontroller.GrblController;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.adapters.RemoteFileAdapter;
import in.co.gorest.grblcontroller.events.MaterialSelectedEvent;
import in.co.gorest.grblcontroller.events.RemoteFileLineJugdeCommandMessageEvent;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.helpers.EnhancedSharedPreferences;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.util.NettyClient;
import in.co.gorest.grblcontroller.util.ZoomViewBean;

public class RemoteFileFragment extends Fragment {
    // 用于日志记录的标签
    private final static String TAG = RemoteFileFragment.class.getSimpleName();
    // 用于管理和访问增强的共享偏好设置实例。
    protected EnhancedSharedPreferences sharedPref;

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

    // 巡边功率
    private int lineJudgeLaserLevel;
    // 当前的机器状态
    private String strMachineStatus;
    // 用来跟踪连续匹配的次数
    private int consecutiveMatches = 0;

    public RemoteFileFragment() {
    }

    public static RemoteFileFragment newInstance() {
        return new RemoteFileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化共享偏好设置实例
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), getString(R.string.shared_preference_key));
        // 注册EventBus
        EventBus.getDefault().register(this);
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
            checkSdCardData();
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
     * 获取SD信息
     */
    private void  checkSdCardData() {
        NettyClient.getInstance(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                Log.d(TAG, "message=" + msg.obj);
                if (!TextUtils.isEmpty(msg.obj.toString())) {
                    // 检查数据是否符合预期的格式
                    if (isValidData(msg.obj.toString())) {
                        // 如果符合格式，解析并添加到 remoteFileList
                        parseData(msg.obj.toString());
                        Log.d(TAG, "Updated remoteFileList: " + remoteFileList);
                        // 设置 LayoutManager
                        rvRemoteFile.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                        // 初始化适配器
                        adapter = new RemoteFileAdapter(requireActivity(), remoteFileList);
                        // 设置适配器
                        rvRemoteFile.setAdapter(adapter);

                        if (isValidSdCardData(msg.obj.toString())) {
                            // 解析并设置 SD 卡的空间信息
                            parseSdCardData(msg.obj.toString());
                        }
                    } else if (isValidSdCardData(msg.obj.toString())) {
                        // 解析并设置 SD 卡的空间信息
                        parseSdCardData(msg.obj.toString());
                    }
                } else {
                    // 如果为空就重新查询
                    checkSdCardData();
                }
                return false;
            }
        })).sendMsgToServer("$SD/List\r\n".getBytes(StandardCharsets.UTF_8), null);
    }

    /**
     * 检查是否符合预期的数据格式
     *
     * @param data 源数据
     * @return 布尔值
     */
    private boolean isValidData(String data) {
        String regex = "FILE:([^\\|]+)\\|SIZE:(\\d+)";
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
        String regex = "FILE:([^\\|]+)\\|SIZE:(\\d+)";
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

    /**
     * MaterialSelectedEvent
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemoteFileLineJugdeCommandMessageEvent(RemoteFileLineJugdeCommandMessageEvent event) {
       float maxX = event.getMaxX();
       float maxY = event.getMaxY();

       // 显示巡边弹窗
       showDialogLineJugde(maxX, maxY);


    }

    /**
     * 显示巡边弹窗
     */
    private void showDialogLineJugde(float maxX, float maxY) {
        Dialog dialog = new Dialog(requireActivity(), R.style.CustomDialog);
        dialog.setContentView(R.layout.dialog_linejugde);

        // 设置窗口背景为透明，以显示圆角效果
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 设置可取消（点击空白处取消）
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);  // 点击外部空白区域取消 Dialog

        // 巡边提示
        TextView tvDialogLinejugdeTips = dialog.findViewById(R.id.tv_dialog_linejugde_tips);
        // 定义一个计数器，用来循环显示点数（模拟巡边正在动态进行）
        final int[] dotCount = {0};  // 用数组包裹，方便在Runnable中修改
        final String baseText = "自动巡边中，请耐心等待";  // 基础文字
        // 创建 Handler 和 Runnable 来更新显示的内容
        Handler handler = new Handler();  // 使用主线程的 Looper
        Runnable loadingRunnable = new Runnable() {
            @Override
            public void run() {
                // 根据当前的点数决定显示的文本
                StringBuilder loadingText = new StringBuilder(baseText);

                // 增加点数
                for (int i = 0; i < dotCount[0]; i++) {
                    loadingText.append(".");
                }

                // 更新 TextView 显示
                tvDialogLinejugdeTips.setText(loadingText.toString());

                // 更新点数，最多到 3 个点后重置
                dotCount[0]++;
                if (dotCount[0] > 3) {
                    dotCount[0] = 0;  // 重置点数
                }

                // 每 500 毫秒更新一次
                handler.postDelayed(this, 500);
            }
        };
        // 启动动画
        handler.post(loadingRunnable);


        // 记录机器初始状态
        final String originalStutas = strMachineStatus;

        // 开始巡边 激光功率不能设置太大,强行改为乘10（原为100）
        lineJudgeLaserLevel = sharedPref.getInt(getString(R.string.preference_laser_level_line_judge_setting), 2);
        Log.d(TAG, "lineJudgeLaserLevel=" + lineJudgeLaserLevel);

        // 起点回到左下角
        sendJogCommand("G0 X0 Y0");
        sendJogCommand("M3 S" + (lineJudgeLaserLevel * 10));
        sendJogCommand("F3500");

        // 沿矩形边缘走一圈
        sendJogCommand("G1 X" + maxX + " Y0");  // → 右下角
        sendJogCommand("G1 X" + maxX + " Y" + maxY);  // ↑ 右上角
        sendJogCommand("G1 X0" + " Y" + maxY);  // ← 左上角
        sendJogCommand("G1 X0 Y0");  // ↓ 回到起点

        sendJogCommand("M5");

        // 回到原始起点或指定位置
        sendJogCommand("G0 X0 Y0");

        // 开始轮询检查坐标是否恢复
        checkCoordinatesUntilOriginal(dialog, originalStutas);

        // 显示 Dialog
        dialog.show();
    }

    /**
     * 开始轮询检查巡边是否完成
     *
     * @param dialog         巡边弹窗
     * @param originalStutas 机器状态
     */
    private void checkCoordinatesUntilOriginal(Dialog dialog, String originalStutas) {
        // 创建一个 Handler 来进行轮询检查
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 检查当前坐标是否已恢复到原始坐标
                if (strMachineStatus.equals(originalStutas)) {
                    // 如果坐标匹配，增加连续匹配的次数
                    consecutiveMatches++;

                    // 如果连续三次匹配，关闭进度对话框
                    if (consecutiveMatches >= 3) {
                        dialog.dismiss();
                        consecutiveMatches = 0;  // 重置匹配次数
                    } else {
                        // 如果匹配不够三次，继续检查
                        checkCoordinatesUntilOriginal(dialog, originalStutas);
                    }
                } else {
                    // 如果坐标不匹配，重置匹配次数
                    consecutiveMatches = 0;
                    // 继续检查
                    checkCoordinatesUntilOriginal(dialog, originalStutas);
                }
            }
        }, 500); // 每隔500毫秒检查一次坐标
    }


    /**
     * 发送命令
     *
     * @param command
     */
    private void sendJogCommand(String command) {
        Log.d(TAG, "command=" + command);
        NettyClient.getInstance(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                return false;
            }
        })).sendMsgToServer((command + "\r\n").getBytes(StandardCharsets.UTF_8), null);
    }

    /**
     * ServiceMessageEvent
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceMessageEvent(ServiceMessageEvent event) {
        if (!event.getMessage().isEmpty()) {
            Activity topActivity = GrblController.getInstance().getTopActivity();
            if (event.getMessage().startsWith("<")) {
                Log.d(TAG, "message=" + event.getMessage().toString());
                String[] parts = event.getMessage().substring(1, event.getMessage().toString().length() - 1).split("\\|");
                Log.d(TAG, "status=" + parts[0] + " Mpos=" + parts[1] + " Wpos=" + parts[2] + " Fs=" + parts[3]);

                strMachineStatus = parts[0];
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销EventBus
        EventBus.getDefault().unregister(this);
    }
}