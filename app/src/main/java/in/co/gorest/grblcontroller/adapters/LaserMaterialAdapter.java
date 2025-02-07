package in.co.gorest.grblcontroller.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.model.Material;

public class LaserMaterialAdapter extends RecyclerView.Adapter<LaserMaterialAdapter.LaserMaterialViewHolder> {

    private Context context;
    private List<Material> materialList;
    private int selectedPosition = 0;
    private OnItemSelectedListener listener;  // 回调接口


    // 构造器
    public LaserMaterialAdapter(Context context, List<Material> materialList, OnItemSelectedListener listener) {
        this.context = context;
        this.materialList = materialList;
        this.listener = listener;  // 设置回调接口
    }

    @NonNull
    @Override
    public LaserMaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 为每个item加载布局
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_laser_material, parent, false);



        return new LaserMaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LaserMaterialViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Material material = materialList.get(position);
        holder.nameTextView.setText(material.getName());
        holder.imageView.setImageResource(material.getImageResId());

        // 如果当前项被选中，显示边框
        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.selected_item_border); // 这里你可以使用一个自定义的边框背景
        } else {
            holder.itemView.setBackgroundResource(0); // 没有选中时去掉边框
        }

        // 设置点击事件，切换选中状态
        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position;  // 设置选中项
            notifyDataSetChanged();       // 更新 RecyclerView
            if (listener != null) {
                listener.onItemSelected(material.getName()); // 调用回调，传递选中项的名称
            }
        });

        // 默认选中第一个项时，自动调用回调方法
        if (selectedPosition == 0 && position == 0 && listener != null) {
            listener.onItemSelected(material.getName());
        }
    }

    @Override
    public int getItemCount() {
        return materialList.size();
    }

    // ViewHolder类
    public static class LaserMaterialViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        ShapeableImageView imageView;

        public LaserMaterialViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_laser_material_name);
            imageView = itemView.findViewById(R.id.iv_laser_material_image);
        }
    }


    public interface OnItemSelectedListener {
        void onItemSelected(String materialName); // 回调方法，传递选中的材料名称
    }
}
