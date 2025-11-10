package in.co.gorest.grblcontroller.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.ApModelAddActivity;
import in.co.gorest.grblcontroller.activity.STAModelAddActivity;
import in.co.gorest.grblcontroller.model.Device;
import in.co.gorest.grblcontroller.model.WifiNetwork;

public class DevicePagerAdapter extends RecyclerView.Adapter<DevicePagerAdapter.AddDeviceViewHolder> {

    // 用于日志记录的标签
    private static final String TAG = DevicePagerAdapter.class.getSimpleName();
    private Context context; // 上下文
    private List<WifiNetwork> deviceList; // 存储设备的列表

    private OnDevicePagerClickLitener litener;

    // 构造函数，传入设备数据列表
    public DevicePagerAdapter(Context context, List<WifiNetwork> deviceList, OnDevicePagerClickLitener litener) {
        this.context = context;
        this.deviceList = deviceList;
        this.litener = litener;
    }

    @NonNull
    @Override
    public AddDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载每个设备的布局文件
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_pager_item, parent, false);
        return new AddDeviceViewHolder(view); // 返回 ViewHolder 实例
    }

    @Override
    public void onBindViewHolder(@NonNull AddDeviceViewHolder holder, int position) {
        // 获取当前设备数据
        WifiNetwork device = deviceList.get(position);
        holder.nameTextView.setText(device.getSsid()); // 设置设备名称

        if (device.getSsid().contains("Laser-T4")) {
            Glide.with(context).load(R.mipmap.ic_laser_t4).into(holder.deviceImage);
        } else if (device.getSsid().contains("Laser-T2020")) {
            Glide.with(context).load(R.mipmap.ic_laser_t2020).into(holder.deviceImage);
        } else if (device.getSsid().contains("CNC-3018PRO")) {
            Glide.with(context).load(R.mipmap.ic_cnc_3018pro).into(holder.deviceImage);
        } else if (device.getSsid().contains("CNC-3018MAX")) {
            Glide.with(context).load(R.mipmap.ic_cnc_3018max).into(holder.deviceImage);
        } else if (device.getSsid().contains("CNC-3020PLUS")) {
            Glide.with(context).load(R.mipmap.ic_cnc_3020plus).into(holder.deviceImage);
        } else {
            Glide.with(context).load(R.mipmap.ic_unknow_404).into(holder.deviceImage);
        }


        // 忽略
        holder.tvIgnore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                litener.onIgnore();
            }
        });


        // 连接
        holder.tvConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                litener.onConnectClick(device.getMode(), device.getSsid(), device.getSsid(), device.getIpAddress());
            }
        });

//
//        // 设置点击事件，点击整个 item 时跳转到详细页面
//        holder.itemView.setOnClickListener(v -> {
//            showModuleDialog(device.getName());
//        });

    }

    @Override
    public int getItemCount() {
        return deviceList.size(); // 返回设备列表的大小
    }

    // 自定义 ViewHolder 类，管理每个 RecyclerView 项目中的视图
    public static class AddDeviceViewHolder extends RecyclerView.ViewHolder {

        ImageView deviceImage;
        TextView nameTextView, tvIgnore, tvConnect;

        // 构造函数，绑定布局中的视图
        public AddDeviceViewHolder(View itemView) {
            super(itemView);
            deviceImage = itemView.findViewById(R.id.iv_device_iamge); // 绑定设备图片 ImageView
            nameTextView = itemView.findViewById(R.id.tv_device_name); // 绑定设备名称 TextView
            tvIgnore = itemView.findViewById(R.id.tv_ignore); // 忽略 TextView
            tvConnect = itemView.findViewById(R.id.tv_connect); // 开始连接 TextView
        }
    }


    public interface OnDevicePagerClickLitener {
        void onIgnore();

        void onConnectClick(String connectType, String machineName, String wifiName, String ipAddress);
    }

}
