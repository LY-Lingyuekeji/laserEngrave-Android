package in.co.gorest.grblcontroller.base;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_custom, null);
        builder.setView(dialogView);

        // 查找控件
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogContent = dialogView.findViewById(R.id.dialog_content);
        @SuppressLint("MissingInflatedId") TextView btnCancel = dialogView.findViewById(R.id.dialog_cancel);
        @SuppressLint("MissingInflatedId") TextView btnOk = dialogView.findViewById(R.id.dialog_ok);

        // 设置标题、内容和按钮文本
        dialogTitle.setText(title);
        dialogContent.setText(content);
        btnCancel.setText(negativeText);
        btnOk.setText(positiveText);

        // 创建 AlertDialog
        AlertDialog alertDialog = builder.create();

        // 设置按钮点击事件
        btnCancel.setOnClickListener(v -> {
            if (negativeListener != null) {
                negativeListener.onClick(v);
            }
            alertDialog.dismiss();
        });

        btnOk.setOnClickListener(v -> {
            if (positiveListener != null) {
                positiveListener.onClick(v);
            }
            alertDialog.dismiss();
        });

        // 显示对话框
        alertDialog.show();
    }

}
