package in.co.gorest.grblcontroller.adapters;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import in.co.gorest.grblcontroller.R;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    private final List<BluetoothDevice> mDevices;
    private final OnItemClickListener mListener;

    public BluetoothDeviceAdapter(List<BluetoothDevice> devices,OnItemClickListener listener) {
        mDevices = devices;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bluetooth_device, parent, false);
        return new ViewHolder(view, mListener);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = mDevices.get(position);
        holder.deviceName.setText(device.getName());
        holder.deviceAddress.setText(device.getAddress());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(device);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        BluetoothDevice boundDevice;

        ViewHolder(@NonNull View itemView,OnItemClickListener listener) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceAddress = itemView.findViewById(R.id.device_address);
        }
    }

    public void updateDevices(List<BluetoothDevice> devices) {
        mDevices.clear();
        mDevices.addAll(devices);
        notifyDataSetChanged();
    }

    public void addDevice(BluetoothDevice device) {
        if (!mDevices.contains(device)) {
            mDevices.add(device);
            notifyItemInserted(mDevices.size() - 1);
        }
    }



    public interface OnItemClickListener {
        void onItemClick(BluetoothDevice device);
    }
}
