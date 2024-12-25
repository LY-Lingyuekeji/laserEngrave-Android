package in.co.gorest.grblcontroller.adapters;

import android.annotation.SuppressLint;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import in.co.gorest.grblcontroller.R;

public class WifiNetworkAdapter extends RecyclerView.Adapter<WifiNetworkAdapter.ViewHolder> {

    private final List<ScanResult> mWifiNetworks;
    private final OnItemClickListener mListener;

    public WifiNetworkAdapter(List<ScanResult> wifiNetworks, OnItemClickListener listener) {
        mWifiNetworks = wifiNetworks;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wifi_network, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanResult wifiNetwork = mWifiNetworks.get(position);
        holder.networkName.setText(wifiNetwork.SSID);
        holder.networkCapabilities.setText(wifiNetwork.capabilities);

        holder.itemView.setOnClickListener(view -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                mListener.onItemClick(wifiNetwork);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mWifiNetworks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView networkName;
        TextView networkCapabilities;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            networkName = itemView.findViewById(R.id.network_name);
            networkCapabilities = itemView.findViewById(R.id.network_capabilities);
        }
    }

    public void updateDevices(List<ScanResult> wifiNetworks) {
        mWifiNetworks.clear();
        mWifiNetworks.addAll(wifiNetworks);
        notifyDataSetChanged();
    }

    public void addDevice(ScanResult wifiNetwork) {
        if (!mWifiNetworks.contains(wifiNetwork)) {
            mWifiNetworks.add(wifiNetwork);
            notifyItemInserted(mWifiNetworks.size() - 1);
        }
    }



    public interface OnItemClickListener {
        void onItemClick(ScanResult wifiNetwork);
    }
}
