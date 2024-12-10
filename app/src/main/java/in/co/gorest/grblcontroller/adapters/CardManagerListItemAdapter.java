package in.co.gorest.grblcontroller.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.model.EngraveListItem;

public class CardManagerListItemAdapter extends RecyclerView.Adapter<CardManagerListItemAdapter.ViewHolder> {

    private Context mContext;
    private List<EngraveListItem> mListItems = new ArrayList<>();

    public CardManagerListItemAdapter(Context context, List<EngraveListItem> listItems) {
        mContext = context;
        mListItems = listItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_manager_list_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EngraveListItem item = mListItems.get(position);
        // 设置图标
        Glide.with(mContext).load(item.getIconResId()).into(holder.ivLogo);
        // 设置内容
        holder.tvTitle.setText(item.getText());
    }

    @Override
    public int getItemCount() {
        return mListItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        // 图标
        ImageView ivLogo;
        // 内容
        TextView tvTitle;
        // 排序
        ImageView ivRank;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 图标
            ivLogo = itemView.findViewById(R.id.iv_logo);
            // 内容
            tvTitle = itemView.findViewById(R.id.tv_title);
            // 排序
            ivRank = itemView.findViewById(R.id.iv_rank);
        }
    }
}
