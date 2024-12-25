package in.co.gorest.grblcontroller.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.model.EngraveListItem;

public class EngraveListItemAdapter extends RecyclerView.Adapter<EngraveListItemAdapter.ViewHolder> {

    private Context mContext;
    private List<EngraveListItem> mListItems = new ArrayList<>();
    private final OnItemClickListener mListener;

    public EngraveListItemAdapter(Context context, List<EngraveListItem> listItems, OnItemClickListener listener) {
        mContext = context;
        mListItems = listItems;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_engrave_list_item, parent, false);
        return new ViewHolder(view, mListener);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EngraveListItem item = mListItems.get(position);
        Glide.with(mContext).load(item.getIconResId()).into(holder.ivEngraveListItemLogo);
        holder.ivEngraveListItemTitle.setText(item.getText());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEngraveListItemLogo;
        TextView ivEngraveListItemTitle;

        ViewHolder(@NonNull View itemView,OnItemClickListener listener) {
            super(itemView);
            ivEngraveListItemLogo = itemView.findViewById(R.id.iv_engrave_list_item_logo);
            ivEngraveListItemTitle = itemView.findViewById(R.id.iv_engrave_list_item_title);
        }
    }


    public interface OnItemClickListener {
        void onItemClick(EngraveListItem item);
    }
}
