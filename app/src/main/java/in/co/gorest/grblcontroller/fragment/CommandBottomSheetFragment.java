package in.co.gorest.grblcontroller.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.TelnetConnectionActivity;
import in.co.gorest.grblcontroller.adapters.MessageAdapter;
import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import in.co.gorest.grblcontroller.model.MessageModel;
import in.co.gorest.grblcontroller.util.NettyClient;
import in.co.gorest.grblcontroller.util.WebSocketManager;

public class CommandBottomSheetFragment extends BottomSheetDialogFragment {
    // 用于日志记录的标签
    private static final String TAG = CommandBottomSheetFragment.class.getSimpleName();
    // 输出详细命令 switch
    private Switch switchMessageDetail;
    // 消息列表
    private RecyclerView recyclerViewMessage;
    // 消息适配器
    private MessageAdapter messageAdapter;
    // 存储消息的列表
    private List<MessageModel> messageModelList = new ArrayList<>();
    // 消息输入框
    private EditText etMessage;
    // 发送
    private TextView tvSendMessage;
    // $$（配置） 快捷命令
    private TextView tvCommandConfig;
    // $#（参数） 快捷命令
    private TextView tvCommandParam;
    // $G（状态） 快捷命令
    private TextView tvCommandState;
    // $I（版本） 快捷命令
    private TextView tvCommandVersion;

    public CommandBottomSheetFragment() {
    }

    public static CommandBottomSheetFragment newInstance() {
        return new CommandBottomSheetFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 注册EventBus
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销EventBus
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_command_bottom_sheet, container, false);
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


        // 获取 BottomSheetBehavior
        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        // 设置 BottomSheet 不随下滑自动关闭
        bottomSheetBehavior.setHideable(false);  // 禁用下滑自动关闭
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED); // 确保弹窗是展开状态
    }

    /**
     * 初始化界面
     *
     * @param view view
     */
    private void initView(View view) {
        // 输出详细命令 switch
        switchMessageDetail = view.findViewById(R.id.switch_message_detail);
        // 消息列表
        recyclerViewMessage = view.findViewById(R.id.recycler_view_message);
        // 消息输入框
        etMessage = view.findViewById(R.id.et_message);
        // 发送
        tvSendMessage = view.findViewById(R.id.tv_send_message);
        // $$（配置） 快捷命令
        tvCommandConfig = view.findViewById(R.id.tv_command_config);
        // $#（参数） 快捷命令
        tvCommandParam = view.findViewById(R.id.tv_command_param);
        // $G（状态） 快捷命令
        tvCommandState = view.findViewById(R.id.tv_command_state);
        // $I（版本） 快捷命令
        tvCommandVersion = view.findViewById(R.id.tv_command_version);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 创建适配器
        messageAdapter = new MessageAdapter(messageModelList);
        // 设置布局管理器
        recyclerViewMessage.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        // 设置适配器
        recyclerViewMessage.setAdapter(messageAdapter);
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // 发送
        tvSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString();
                if (!TextUtils.isEmpty(message)) {
                    // 发送消息
                    sendMessage(message);
                    // 清空输入框
                    etMessage.setText("");
                } else {
                    Toast.makeText(requireContext(), "发送的消息不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // $$（配置） 快捷命令
        tvCommandConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("$$");
            }
        });

        // $#（参数） 快捷命令
        tvCommandParam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("$#");
            }
        });

        // $G（状态） 快捷命令
        tvCommandState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("$G");
            }
        });

        // $I（版本） 快捷命令
        tvCommandVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("$I");
            }
        });

    }

    /**
     * 发送消息方法
     */
    private void sendMessage(String message) {
        // 发送命令
        sendJogCommand(message);

        // 发送消息后添加到消息列表并更新 RecyclerView
        messageModelList.add(new MessageModel(message, true));  // true 表示消息是自己发送的
        messageAdapter.notifyItemInserted(messageModelList.size() - 1);  // 通知适配器插入新项
        recyclerViewMessage.scrollToPosition(messageModelList.size() - 1);  // 滚动到最新的消息

    }

    /**
     * 发送命令
     *
     * @param command 命令
     */
    private void sendJogCommand(String command) {
        WebSocketManager webSocketManager = WebSocketManager.getInstance();
        boolean isConnected = webSocketManager.isConnected();
//        boolean isConnected = NettyClient.getInstance().getConnectStatus();
        Log.d(TAG, "isConnected=" + isConnected);
        if (isConnected) {
            Log.d(TAG, "command=" + command);
//            NettyClient.getInstance(new Handler(new Handler.Callback() {
//                @Override
//                public boolean handleMessage(@NonNull Message msg) {
//                    return false;
//                }
//            })).sendMsgToServer((command + "\r\n").getBytes(StandardCharsets.UTF_8), null);
            webSocketManager.send(command + "\r\n");
        } else {
            Log.d(TAG, "未连接上设备");
        }
    }


    /**
     * ServiceMessageEvent
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceMessageEvent(ServiceMessageEvent event) {
        if (!event.getMessage().isEmpty()) {
            String receivedMessage = event.getMessage().toString().trim();
            Log.d(TAG, "接收到的消息: " + receivedMessage);
            // 消息是以 “<” 开头
            if (receivedMessage.startsWith("<")) {
                // 如果 switch 开启
                if (!switchMessageDetail.isChecked()) {
                    // 不进行添加
                    Log.d(TAG, "消息过滤");
                } else {
                    // 如果 Switch 关闭，接收所有消息
                    messageModelList.add(new MessageModel(receivedMessage, false));  // false 表示接收到的消息
                    messageAdapter.notifyItemInserted(messageModelList.size() - 1);  // 通知适配器插入新项
                    recyclerViewMessage.scrollToPosition(messageModelList.size() - 1);  // 滚动到最后一条消息
                }
            } else {
                // 如果 Switch 关闭，接收所有消息
                messageModelList.add(new MessageModel(receivedMessage, false));  // false 表示接收到的消息
                messageAdapter.notifyItemInserted(messageModelList.size() - 1);  // 通知适配器插入新项
                recyclerViewMessage.scrollToPosition(messageModelList.size() - 1);  // 滚动到最后一条消息
            }
        }
    }

}