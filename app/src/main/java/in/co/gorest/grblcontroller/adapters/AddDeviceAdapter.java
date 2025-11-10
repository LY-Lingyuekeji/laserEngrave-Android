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

public class AddDeviceAdapter extends RecyclerView.Adapter<AddDeviceAdapter.AddDeviceViewHolder> {

    // 用于日志记录的标签
    private static final String TAG = AddDeviceAdapter.class.getSimpleName();
    private Context context; // 上下文
    private List<Device> deviceList; // 存储设备的列表

    // 构造函数，传入设备数据列表
    public AddDeviceAdapter(Context context, List<Device> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public AddDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载每个设备的布局文件
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_device_item, parent, false);
        return new AddDeviceViewHolder(view); // 返回 ViewHolder 实例
    }

    @Override
    public void onBindViewHolder(@NonNull AddDeviceViewHolder holder, int position) {
        // 获取当前设备数据
        Device device = deviceList.get(position);
        holder.nameTextView.setText(device.getName()); // 设置设备名称
        holder.travelTextView.setText(device.getTravel()); // 设置设备行程

        if (device.getName().equals("Laser-T4")) {
            Glide.with(context).load(R.mipmap.ic_laser_t4).into(holder.deviceImage);
        } else if (device.getName().equals("Laser-T2020")) {
            Glide.with(context).load(R.mipmap.ic_laser_t2020).into(holder.deviceImage);
        } else if (device.getName().equals("CNC-3018PRO")) {
            Glide.with(context).load(R.mipmap.ic_cnc_3018pro).into(holder.deviceImage);
        } else if (device.getName().equals("CNC-3018MAX")) {
            Glide.with(context).load(R.mipmap.ic_cnc_3018max).into(holder.deviceImage);
        } else if (device.getName().equals("CNC-3020PLUS")) {
            Glide.with(context).load(R.mipmap.ic_cnc_3020plus).into(holder.deviceImage);
        } else {
            Glide.with(context).load(R.mipmap.ic_unknow_404).into(holder.deviceImage);
        }


        // 设置点击事件，点击整个 item 时跳转到详细页面
        holder.itemView.setOnClickListener(v -> {
            showModuleDialog(device.getName());
        });

    }

    @Override
    public int getItemCount() {
        return deviceList.size(); // 返回设备列表的大小
    }

    // 自定义 ViewHolder 类，管理每个 RecyclerView 项目中的视图
    public static class AddDeviceViewHolder extends RecyclerView.ViewHolder {

        ImageView deviceImage;
        TextView nameTextView, travelTextView; // 设备名称和行程的 TextView

        // 构造函数，绑定布局中的视图
        public AddDeviceViewHolder(View itemView) {
            super(itemView);
            deviceImage = itemView.findViewById(R.id.iv_device_iamge); // 绑定设备图片 ImageView
            nameTextView = itemView.findViewById(R.id.tv_device_name); // 绑定设备名称 TextView
            travelTextView = itemView.findViewById(R.id.tv_device_travel); // 绑定设备行程 TextView
        }
    }

    // 显示自定义 Dialog
    private void showModuleDialog(String deviceName) {
        // 检查 context 是否是 Activity 类型
        if (context instanceof Activity) {
            Activity activity = (Activity) context;  // 将 context 强制转换为 Activity

            // 确保在主线程中创建和显示 Dialog
            activity.runOnUiThread(() -> {
                // 创建 Dialog
                Dialog dialog = new Dialog(activity, R.style.CustomDialog);
                dialog.setContentView(R.layout.dialog_module);

                // 设置窗口背景为透明，以显示圆角效果
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                }

                // 设置可取消（点击空白处取消）
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);  // 点击外部空白区域取消 Dialog

                // STA 连接
                LinearLayout llModuleSta = dialog.findViewById(R.id.ll_module_sta);
                llModuleSta.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 跳转
                        Intent intent = new Intent(context, STAModelAddActivity.class);
                        intent.putExtra("deviceName", deviceName);
                        // 跳转
                        context.startActivity(intent);
                        // 关闭页面
                        ((Activity) context).finish();
                        // 隐藏弹窗
                        dialog.dismiss();
                    }
                });


                // AP 连接
                LinearLayout llModuleAp = dialog.findViewById(R.id.ll_module_ap);
                llModuleAp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ApModelAddActivity.class);
                        intent.putExtra("connectTarget", "");
                        // 跳转
                        context.startActivity(intent);
                        // 关闭页面
                        ((Activity) context).finish();
                        // 隐藏弹窗
                        dialog.dismiss();
                    }
                });

                // 设置关闭按钮
                ImageView ivCancel = dialog.findViewById(R.id.iv_cancel);
                ivCancel.setOnClickListener(v -> dialog.dismiss());  // 点击关闭按钮时关闭 Dialog

                // 显示 Dialog
                dialog.show();
            });
        } else {
            // 如果 context 不是 Activity 类型，显示错误日志
            Log.e("AddDeviceAdapter", "Context is not an instance of Activity");
        }
    }
}
