package in.co.gorest.grblcontroller.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.model.Material;

public class MaterialLibraryAdapter extends RecyclerView.Adapter<MaterialLibraryAdapter.LaserMaterialViewHolder> {

    private Context context;
    private List<Material> materialList;

    // 构造器
    public MaterialLibraryAdapter(Context context, List<Material> materialList) {
        this.context = context;
        this.materialList = materialList;
    }

    @NonNull
    @Override
    public LaserMaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 为每个item加载布局
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_material_library, parent, false);
        return new LaserMaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LaserMaterialViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Material material = materialList.get(position);
        holder.nameTextView.setText(material.getName());
        holder.imageView.setImageResource(material.getImageResId());

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
            nameTextView = itemView.findViewById(R.id.tv_material_library_name);
            imageView = itemView.findViewById(R.id.iv_material_library_image);
        }
    }
}
