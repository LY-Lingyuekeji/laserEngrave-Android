package in.co.gorest.grblcontroller.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import org.greenrobot.eventbus.EventBus;
import java.util.List;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.ApModelAddActivity;
import in.co.gorest.grblcontroller.activity.MachineDetailActivity;
import in.co.gorest.grblcontroller.events.DeviceConnectEvent;
import in.co.gorest.grblcontroller.model.DeviceConnectRecord;
import in.co.gorest.grblcontroller.util.WebSocketManager;

public class DeviceConnectRecordAdapter extends RecyclerView.Adapter<DeviceConnectRecordAdapter.DeviceConnectRecordViewHolder> {

    private Context context; // 上下文
    private List<DeviceConnectRecord> deviceConnectRecordList; // 连接记录的列表

    // 构造函数，传入设备数据列表
    public DeviceConnectRecordAdapter(Context context, List<DeviceConnectRecord> deviceConnectRecordList) {
        this.context = context;
        this.deviceConnectRecordList = deviceConnectRecordList;
    }

    @NonNull
    @Override
    public DeviceConnectRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载每个设备的布局文件
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_connect_record, parent, false);
        return new DeviceConnectRecordViewHolder(view); // 返回 ViewHolder 实例
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceConnectRecordViewHolder holder, int position) {
        DeviceConnectRecord record = deviceConnectRecordList.get(position);
        if (record.getMachineName().contains("Laser")) {
            // 设置激光雕刻机器图片
            if (record.getMachineName().contains("T2020")) {
                // 设置激光雕刻机 T2020图片
                Glide.with(holder.itemView.getContext()).load(R.mipmap.ic_laser_t2020).into(holder.ivImage);
            } else {
                // 设置激光雕刻机 T4图片
                Glide.with(holder.itemView.getContext()).load(R.mipmap.ic_laser_t4).into(holder.ivImage);
            }
        } else {
            // 设置CNC雕刻机机器图片
            if (record.getMachineName().contains("3018MAX")) {
                // 设置CNC雕刻机 3018MAX图片
                Glide.with(holder.itemView.getContext()).load(R.mipmap.ic_cnc_3018max).into(holder.ivImage);
            } else if (record.getMachineName().contains("3018PRO")) {
                // 设置CNC雕刻机 3018PRO图片
                Glide.with(holder.itemView.getContext()).load(R.mipmap.ic_cnc_3018pro).into(holder.ivImage);
            } else {
                // 设置CNC雕刻机 3020PLUS图片
                Glide.with(holder.itemView.getContext()).load(R.mipmap.ic_cnc_3020plus).into(holder.ivImage);
            }
        }

        // 获取当前连接的 Wi-Fi SSID
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String currentSsid = wifiInfo != null ? wifiInfo.getSSID().replaceAll("^\"|\"$", "") : "";
        // 判断连接状态
        boolean isConnected = currentSsid.equals(record.getSsid());

        if (isConnected) {
            holder.tvStatus.setText("已连接");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_green_1e853a_r20_noleftbottom);
            holder.tvConnect.setText("断开连接");
        } else {
            holder.tvStatus.setText("未连接");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_gray_cfcfcf_r20_noleftbottom);
            holder.tvConnect.setText("连接");
        }

        // 机器名称
        holder.tvName.setText(record.getMachineName());
        holder.tvMode.setText(record.getMode());
        holder.tvSsid.setText("WIFI: " + record.getSsid());
        holder.tvLaserModule.setText("激光模组: " + record.getLaserModule());
        holder.tvSize.setText("行程: " + record.getSize());
        holder.tvIp.setText("IP: " + record.getIpAddress());
        holder.tvTime.setText("上次连接时间: " + record.getTime());

        // 卡片点击
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.tvStatus.getText().equals("未连接")) {
                    Toast.makeText(holder.itemView.getContext(), "未连接到该设备，无法查看", Toast.LENGTH_SHORT).show();
                    return;
                }
                // TODO 跳转机器详情页面
                Intent intent = new Intent(holder.itemView.getContext(), MachineDetailActivity.class);
                intent.putExtra("machineName", record.getMachineName().toString());
                holder.itemView.getContext().startActivity(intent);
            }
        });

        // 连接
        holder.tvConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.tvStatus.getText().equals("未连接")) {
                    // 跳转
                    Intent intent = new Intent(context, ApModelAddActivity.class);
                    intent.putExtra("connectTarget", holder.tvName.getText().toString());
                    context.startActivity(intent);
                } else {
                    // 断开NettyClient
                    WebSocketManager webSocketManager = WebSocketManager.getInstance();
                    webSocketManager.disconnect();
//                    NettyClient.getInstance().disconnect();
                    // 发送EventBus事件
                    EventBus.getDefault().post(new DeviceConnectEvent("disconnect","null","null", "null"));
                    // 关闭页面
                    if (context instanceof Activity) {
                        Activity activity = (Activity) context;
                        activity.finish();
                    }
//                    BaseDialog.showCustomDialog(holder.itemView.getContext(), "温馨提示",
//                            "出于安全性考虑，Android 移除了程序主动断开当前 Wi-Fi 的能力，仅能断开设备通讯，请到 Wi-Fi 设置手动断开\r\n\r\n是否需要断开连接的设备？",
//                            "确认", "取消",
//                            v1 -> {
//                                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                context.startActivity(intent);
//                                if (context instanceof Activity) {
//                                    Activity activity = (Activity) context;
//                                    activity.finish();
//                                }
//                            },
//                            v1 -> {
//                                Log.d("DeviceConnectRecordAdapter", "用户选择取消");
//                            });
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return deviceConnectRecordList.size(); // 返回设备列表的大小
    }

    // 自定义 ViewHolder 类，管理每个 RecyclerView 项目中的视图
    public static class DeviceConnectRecordViewHolder extends RecyclerView.ViewHolder {

        // 机器图片
        ImageView ivImage;
        TextView tvStatus, tvName, tvMode, tvSsid, tvLaserModule, tvSize, tvIp, tvTime, tvConnect;

        // 构造函数，绑定布局中的视图
        public DeviceConnectRecordViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvName = itemView.findViewById(R.id.tv_name);
            tvMode = itemView.findViewById(R.id.tv_mode);
            tvSsid = itemView.findViewById(R.id.tv_ssid);
            tvLaserModule = itemView.findViewById(R.id.tv_laser_module);
            tvSize = itemView.findViewById(R.id.tv_size);
            tvIp = itemView.findViewById(R.id.tv_ip);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvConnect = itemView.findViewById(R.id.tv_connect);
        }
    }
}
