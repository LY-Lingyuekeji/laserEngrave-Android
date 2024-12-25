package in.co.gorest.grblcontroller.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.base.BaseDialog;
import in.co.gorest.grblcontroller.util.NettyClient;

public class RemoteFileAdapter extends RecyclerView.Adapter<RemoteFileAdapter.ViewHolder> {

    // 存储数据
    private ArrayList<String> dataList;

    public RemoteFileAdapter(ArrayList<String> data) {
        this.dataList = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_remote_file, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 绑定数据到视图
        String data = dataList.get(position);
        String file = data.split(",")[0].split(":")[1].trim();
        holder.itemRemoteFileName.setText(file);  // 显示 File
        // 处理文件大小并显示
        String size = data.split(",")[1].split(":")[1].trim();
        holder.itemRemoteFileSize.setText(formatSize(Long.parseLong(size)));  // 显示 Size


        // 为"更多"图标设置点击事件
        holder.itemRemoteFileMore.setOnClickListener(v -> showPopupMenu(v, position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView itemRemoteFileName;
        private TextView itemRemoteFileSize;
        private ImageView itemRemoteFileMore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemRemoteFileName = itemView.findViewById(R.id.item_remote_file_name);
            itemRemoteFileSize = itemView.findViewById(R.id.item_remote_file_size);
            itemRemoteFileMore = itemView.findViewById(R.id.item_remote_file_more);
        }
    }

    /**
     * 格式化文件大小为合适的单位
     *
     * @param size 字节大小
     * @return 格式化后的文件大小
     */
    private String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    private void showPopupMenu(View view, int position) {
        // 创建 PopupMenu
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);

        // 获取 menuInflater 并加载自定义菜单
        popupMenu.getMenuInflater().inflate(R.menu.menu_remote_file_options, popupMenu.getMenu());

        // 设置菜单项点击事件
        popupMenu.setOnMenuItemClickListener(item -> {
            String fileName = dataList.get(position).split(",")[0].split(":")[1].trim();
            switch (item.getItemId()) {
                case R.id.option_rename:
                    // 执行重命名操作
                    Toast.makeText(view.getContext(), "功能调试中", Toast.LENGTH_SHORT).show();


//                    // 创建AlertDialog并设置自定义视图
//                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
//                    LayoutInflater inflater = LayoutInflater.from(view.getContext());
//                    View dialogView = inflater.inflate(R.layout.dialog_rename_remote_file, null);
//                    builder.setView(dialogView);
//
//                    // 获取输入框
//                    EditText etNewFileName = dialogView.findViewById(R.id.dialog_rename_remote_file_new_file_name);
//                    // 取消按钮
//                    TextView tvCancel = dialogView.findViewById(R.id.dialog_rename_remote_file_cancel);
//                    // 确认按钮
//                    TextView tvConfirm = dialogView.findViewById(R.id.dialog_rename_remote_file_confirm);
//
//                    // 显示弹窗
//                    AlertDialog alertDialog = builder.create();
//                    alertDialog.show();
//
//
//                    // 取消按钮点击事件
//                    tvCancel.setOnClickListener(v -> {
//                        // 关闭弹窗
//                        alertDialog.dismiss();
//                    });
//
//                    // 确定按钮点击事件
//                    tvConfirm.setOnClickListener(v -> {
//                        String newFileName = etNewFileName.getText().toString().trim();
//                        if (!newFileName.isEmpty()) {
//                            Log.d("123", "fileName=" + fileName.substring(1, fileName.length()));
//                            // 执行重命名操作
//                            NettyClient.getInstance(null).sendMsgToServer(("$SD/Rename=" + fileName.substring(1, fileName.length()) + ">" + newFileName +  ".nc\r\n").getBytes(StandardCharsets.UTF_8), null);
//                            // 更新数据源并通知适配器更新
//                            dataList.set(position, "File: /" + newFileName + ".nc, Size: " + dataList.get(position).split(",")[1].split(":")[1].trim());
//                            notifyItemChanged(position);  // 更新单个item
//                            alertDialog.dismiss();
//                        } else {
//                            Toast.makeText(view.getContext(), "文件名不能为空", Toast.LENGTH_SHORT).show();
//                        }
//                    });
                    return true;
                case R.id.option_delete:

                    BaseDialog.showCustomDialog(view.getContext(),
                            "温馨提示", "是否删除当前文件？",
                            "确定", "取消",
                            v -> {
                                // 执行删除操作
                                NettyClient.getInstance(null).sendMsgToServer(("$SD/Delete=" + fileName + "\r\n").getBytes(StandardCharsets.UTF_8), null);
                                // 从数据源中移除被删除的文件
                                dataList.remove(position);
                                // 通知适配器更新列表，移除指定项
                                notifyItemRemoved(position);
                                // 重新更新后续项的索引，避免删除后顺序混乱
                                notifyItemRangeChanged(position, dataList.size() - position);
                            },
                            v -> {

                            });

                    return true;
                default:
                    return false;
            }
        });

        // 显示弹窗
        popupMenu.show();
    }
}
