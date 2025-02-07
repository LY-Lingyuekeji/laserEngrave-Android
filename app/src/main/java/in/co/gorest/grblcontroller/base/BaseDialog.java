package in.co.gorest.grblcontroller.base;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import in.co.gorest.grblcontroller.R;

public class BaseDialog {

    // 私有构造方法，防止外部实例化
    private BaseDialog() {}


    /**
     * 显示自定义弹窗
     *
     * @param context           上下文
     * @param title             标题
     * @param content           内容
     * @param positiveText      确定按钮文本
     * @param negativeText      取消按钮文本
     * @param positiveListener  确定按钮点击事件
     * @param negativeListener  取消按钮点击事件
     */
    public static void showCustomDialog(Context context, String title, String content, String positiveText, String negativeText,
                                        View.OnClickListener positiveListener, View.OnClickListener negativeListener) {

        // 创建一个Dialog
        Dialog dialog = new Dialog(context, R.style.CustomDialog);

        // 自定义布局
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_custom, null);

        // 设置窗口背景为透明，以显示圆角效果
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 获取布局中的视图元素
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        TextView dialogContent = view.findViewById(R.id.dialog_content);
        TextView btnCancel = view.findViewById(R.id.dialog_cancel);
        TextView btnOk = view.findViewById(R.id.dialog_ok);

        // 设置标题、内容和按钮文本
        dialogTitle.setText(title);
        dialogContent.setText(content);
        btnCancel.setText(negativeText);
        btnOk.setText(positiveText);


        // 设置按钮点击事件
        btnCancel.setOnClickListener(v -> {
            if (negativeListener != null) {
                negativeListener.onClick(v);
            }
            dialog.dismiss();
        });

        btnOk.setOnClickListener(v -> {
            if (positiveListener != null) {
                positiveListener.onClick(v);
            }
            dialog.dismiss();
        });

        // 设置Dialog的视图
        dialog.setContentView(view);

        // 设置不可取消，点击外部不消失
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // 设置Dialog的宽高
        if (dialog.getWindow() != null) {
            // 设置弹窗宽度为屏幕的80%，高度自适应
            dialog.getWindow().setLayout((int) (context.getResources().getDisplayMetrics().widthPixels * 0.8),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // 显示对话框
        dialog.show();
    }

}
