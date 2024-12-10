package in.co.gorest.grblcontroller.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;

import java.util.List;

import in.co.gorest.grblcontroller.R;

import in.co.gorest.grblcontroller.model.PictureBean;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.ViewHolder> {

    private Context context;

    private final List<PictureBean> datas;
    private View.OnClickListener onItemClickListener;

    public MaterialAdapter(Context context,List<PictureBean> datas) {
        this.context = context;
        this.datas = datas;
    }


    public void setItemClickListener(View.OnClickListener clickListener) {
        onItemClickListener = clickListener;
    }

    @NonNull
    @Override
    public MaterialAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.material_item, parent, false);
        return new MaterialAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialAdapter.ViewHolder holder, int position) {
        Glide.with(context)
                .load(datas.get(position).getUrl())
                .into(holder.ivMaterial);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView ivMaterial;

        public ViewHolder(View itemView){
            super(itemView);

            ivMaterial = itemView.findViewById(R.id.item_material);
            itemView.setTag(this);
            itemView.setOnClickListener(onItemClickListener);
        }

    }

}
