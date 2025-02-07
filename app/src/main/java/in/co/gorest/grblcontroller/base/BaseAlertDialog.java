package in.co.gorest.grblcontroller.base;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import in.co.gorest.grblcontroller.R;

public class BaseAlertDialog {
    private Context context;

    public BaseAlertDialog(Context context) {
        this.context = context;
    }

    /**
     * 显示自定义弹窗
     *
     * @param title   弹窗标题
     * @param message 弹窗内容
     * @param listener 确认按钮点击事件
     */
    public void show(String title, String message, View.OnClickListener listener) {
        // 创建一个Dialog
        Dialog dialog = new Dialog(context, R.style.CustomDialog);

        // 自定义布局
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_base_alert, null);

        // 设置窗口背景为透明，以显示圆角效果
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 获取布局中的视图元素
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvMessage = view.findViewById(R.id.tv_message);
        TextView tvConfirm = view.findViewById(R.id.tv_confirm);
        // 设置标题和内容
        tvTitle.setText(title);
        tvMessage.setText(message);

        // 设置确认按钮的点击事件
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 调用传入的监听器
                if (listener != null) {
                    listener.onClick(v);
                }
                // 点击确认按钮时关闭弹窗
                dialog.dismiss();
            }
        });

        // 设置Dialog的视图
        dialog.setContentView(view);

        // 设置不可取消，点击外部不消失
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // 显示弹窗
        dialog.show();
    }
}
