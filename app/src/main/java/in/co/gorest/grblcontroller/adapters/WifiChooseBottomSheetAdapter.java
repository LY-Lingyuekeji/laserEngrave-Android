package in.co.gorest.grblcontroller.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import in.co.gorest.grblcontroller.R;

public class WifiChooseBottomSheetAdapter extends RecyclerView.Adapter<WifiChooseBottomSheetAdapter.ViewHolder> {

    private List<String> wifiNetworks;
    private OnItemClickListener mListener;

    public WifiChooseBottomSheetAdapter(List<String> wifiNetworks, OnItemClickListener listener) {
        this.wifiNetworks = wifiNetworks;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wifi_choose_bottom_sheet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String network = wifiNetworks.get(position);
        holder.wifiName.setText(network);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(holder.wifiName.getText().toString());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return wifiNetworks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView wifiName;

        ViewHolder(View itemView) {
            super(itemView);
            wifiName = itemView.findViewById(R.id.wifiName);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String ssid);
    }
}
