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
import com.google.gson.Gson;

import java.util.List;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.ApModelAddActivity;
import in.co.gorest.grblcontroller.activity.PreViewActivity;
import in.co.gorest.grblcontroller.model.Device;
import in.co.gorest.grblcontroller.model.EngraveHistoryRecord;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context; // 上下文
    private List<EngraveHistoryRecord> historyRecordList; // 存储设备的列表

    // 构造函数，传入设备数据列表
    public HistoryAdapter(Context context, List<EngraveHistoryRecord> historyRecordList) {
        this.context = context;
        this.historyRecordList = historyRecordList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载每个设备的布局文件
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_engrave_history, parent, false);
        return new HistoryViewHolder(view); // 返回 ViewHolder 实例
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        EngraveHistoryRecord record = historyRecordList.get(position);
        holder.tvTime.setText(record.getTimestamp());
        holder.tvMachine.setText(record.getMachineName());
        Glide.with(holder.itemView.getContext()).load(record.getImagePath()).into(holder.ivImage);

        holder.itemView.setOnClickListener(v -> {
           Intent intent = new Intent(context, PreViewActivity.class);
            intent.putExtra("engraveHistoryJson", new com.google.gson.Gson().toJson(record));
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return historyRecordList.size(); // 返回设备列表的大小
    }

    // 自定义 ViewHolder 类，管理每个 RecyclerView 项目中的视图
    public static class HistoryViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImage;
        TextView tvTime, tvMachine;

        // 构造函数，绑定布局中的视图
        public HistoryViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvMachine = itemView.findViewById(R.id.tv_machine);
        }
    }
}
