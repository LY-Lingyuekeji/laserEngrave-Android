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
import in.co.gorest.grblcontroller.model.StaModelConfig;
import in.co.gorest.grblcontroller.util.NettyClient;

public class StaModelConfigAdapter extends RecyclerView.Adapter<StaModelConfigAdapter.StaModelConfigViewHolder> {

    private Context context; // 上下文
    private List<StaModelConfig> staModelConfigList; // 配置记录列表

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }


    // 构造函数，传入设备数据列表
    public StaModelConfigAdapter(Context context, List<StaModelConfig> staModelConfigList) {
        this.context = context;
        this.staModelConfigList = staModelConfigList;
    }

    @NonNull
    @Override
    public StaModelConfigViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载每个设备的布局文件
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sta_model_add_config, parent, false);
        return new StaModelConfigViewHolder(view); // 返回 ViewHolder 实例
    }

    @Override
    public void onBindViewHolder(@NonNull StaModelConfigViewHolder holder, int position) {
        StaModelConfig staModelConfig = staModelConfigList.get(position);
        if (staModelConfig.getMachineName().contains("Laser")) {
            // 设置激光雕刻机器图片
            if (staModelConfig.getMachineName().contains("T2020")) {
                // 设置激光雕刻机 T2020图片
                Glide.with(holder.itemView.getContext()).load(R.mipmap.ic_laser_t2020).into(holder.ivImage);
            } else {
                // 设置激光雕刻机 T4图片
                Glide.with(holder.itemView.getContext()).load(R.mipmap.ic_laser_t4).into(holder.ivImage);
            }
        } else {
            // 设置CNC雕刻机机器图片
            if (staModelConfig.getMachineName().contains("3018MAX")) {
                // 设置CNC雕刻机 3018MAX图片
                Glide.with(holder.itemView.getContext()).load(R.mipmap.ic_cnc_3018max).into(holder.ivImage);
            } else if (staModelConfig.getMachineName().contains("3018PRO")) {
                // 设置CNC雕刻机 3018PRO图片
                Glide.with(holder.itemView.getContext()).load(R.mipmap.ic_cnc_3018pro).into(holder.ivImage);
            } else {
                // 设置CNC雕刻机 3020PLUS图片
                Glide.with(holder.itemView.getContext()).load(R.mipmap.ic_cnc_3020plus).into(holder.ivImage);
            }
        }

        // 机器名称
        holder.tvName.setText(staModelConfig.getMachineName());
        // 模式
        holder.tvMode.setText("模式：" + staModelConfig.getMode());
        // Wi-Fi
        holder.tvSsid.setText("WIFI：" + staModelConfig.getConfigSSID());

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(staModelConfig);
            }
        });

    }

    @Override
    public int getItemCount() {
        return staModelConfigList.size(); // 返回配置列表的大小
    }

    // 自定义 ViewHolder 类，管理每个 RecyclerView 项目中的视图
    public static class StaModelConfigViewHolder extends RecyclerView.ViewHolder {

        // 机器图片
        ImageView ivImage;
        TextView  tvName, tvMode, tvSsid;

        // 构造函数，绑定布局中的视图
        public StaModelConfigViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvName = itemView.findViewById(R.id.tv_name);
            tvMode = itemView.findViewById(R.id.tv_mode);
            tvSsid = itemView.findViewById(R.id.tv_ssid);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(StaModelConfig config);
    }
}
