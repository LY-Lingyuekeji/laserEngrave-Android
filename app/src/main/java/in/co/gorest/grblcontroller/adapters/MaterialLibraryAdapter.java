package in.co.gorest.grblcontroller.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.events.MaterialSelectedEvent;
import in.co.gorest.grblcontroller.fragment.ParameterBottomSheetFragment;
import in.co.gorest.grblcontroller.model.Material;

public class MaterialLibraryAdapter extends RecyclerView.Adapter<MaterialLibraryAdapter.LaserMaterialViewHolder> {


    // 用于日志记录的标签
    private final static String TAG = MaterialLibraryAdapter.class.getSimpleName();
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
        // 点击
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"MaterialName=" + holder.nameTextView.getText().toString());

                EventBus.getDefault().post(new MaterialSelectedEvent(holder.nameTextView.getText().toString()));

                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    activity.finish();
                }
            }
        });

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
